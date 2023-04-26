package com.NoNewItems;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.PostItemComposition;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
		name = "07 Items Only",
		description = "Avoid using items that were not originally part of Old School RuneScape.",
		tags = {"07", "07 only", "no updates", "no changes", "items"},
		loadWhenOutdated = true,
		enabledByDefault = false
)
public class NoNewItemsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private NoNewItemsConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.info("07 items only started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("07 items only stopped!");
	}

	public void hide_inv_sprite(ItemComposition new_item){

		new_item.setInventoryModel(7669); // The bank filler sprite.

		new_item.setXan2d(512);
		new_item.setYan2d(0);
		new_item.setZan2d(0);

	}

	public void hide_item(ItemComposition new_item){

		hide_inv_sprite(new_item);

		if (config.hide_name()){

			new_item.setName("Non-07 Item");

		}

	}

	@Subscribe
	public void onPostItemComposition(PostItemComposition item_change)
	{
		ItemComposition new_item = item_change.getItemComposition();
		int id = new_item.getId();

		if (id > 11685){ // This seems to be the item ID cutoff from King's Ransom to the Nightmare Zone according to https://www.osrsbox.com/tools/item-search/.

			if (!config.hide_god_wars()){

				// If we enter here, then we are allowing items from the God Wars dungeon which appeared in the dungeon's original 2007 release to the main game.
				boolean valid_god_wars_item = ((id >= 11787) && (id < 11791)) || ((id >= 11793) && (id < 11844)); // These checks exclude the Armadyl crossbow (11785/6) and the Staff of the dead (11791/2).

				if (valid_god_wars_item){
					return; // Leave without hiding the item.
				}

			}

			if (!config.hide_varrock_armour()){

				// If we enter here, then we are allowing the Varrock armour 1 from the easy Varrock diary.
				if ((id == 13104) || (id == 18642)){
					return; // Leave without hiding the item.
				}

			}

			hide_item(new_item);

		}

	}

	@Provides
	NoNewItemsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NoNewItemsConfig.class);
	}
}
