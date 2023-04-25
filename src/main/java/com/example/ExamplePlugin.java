package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.GameStateChanged;
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
public class ExamplePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

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

		// This seems to be a good 'face on' viewing angle. It was taken from the Cabbage Cape.
		new_item.setXan2d(504);
		new_item.setYan2d(0);
		new_item.setZan2d(0);

	}

	@Subscribe
	public void onPostItemComposition(PostItemComposition item_change)
	{
		ItemComposition new_item = item_change.getItemComposition();
		int id = new_item.getId();

		if (id > 11685){ // This seems to be the item ID cutoff from King's Ransom to the Nightmare Zone according to https://www.osrsbox.com/tools/item-search/.

			hide_inv_sprite(new_item);

			if (config.hide_name()) {

				new_item.setName("Non-07 Item");

			}

		}

	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
