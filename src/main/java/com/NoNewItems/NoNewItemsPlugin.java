package com.NoNewItems;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.PostItemComposition;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
		name = "No New Items",
		description = "Avoid using items that were not originally part of Old School RuneScape.",
		tags = {"07", "07 only", "no updates", "no changes", "items", "no new items"},
		loadWhenOutdated = true,
		enabledByDefault = true
)
public class NoNewItemsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private NoNewItemsConfig config;

	public short[] all_colours = new short[65536];
	public short[] new_colours = new short[65536];

	public short[] all_textures = new short[94]; // Seemingly there are only 94 textures.
	public short[] new_textures = new short[94];

	public void reset(){

		// Resetting these caches will force through the visual change in inventories
		// (including shops, worn equipment etc.) and on the floor. Unfortunately it doesn't
		// change the name shown by the Ground Items plugin.
		NodeCache cache = client.getItemModelCache();
		cache.reset();

		cache = client.getItemSpriteCache();
		cache.reset();

		cache = client.getItemCompositionCache();
		cache.reset();

		// This will force through appearance changes on the player model.
		Player me = client.getLocalPlayer();
		if (me != null){
			(me.getPlayerComposition()).setHash();
		}

		// This should force through the change on all other rendered players.
		// However, certain players seem to get stuck on the start-up configuration of the plugin.
		Player[] cached_players = client.getCachedPlayers();
		for (Player other : cached_players){
			if (other != null){
				(other.getPlayerComposition()).setHash();
			}
		}

	}

	@Override
	protected void startUp() throws Exception {

		log.info("No New Items started!");

		// Colours in game are represented by a short, which is obtained from an HSL by means of JagexColor.packHSL(int hue, int saturation, int luminance).
		// Each of these three ints presumably have a minimum value of 0, and they have respective maximums 63, 7 and 127 (see the constant field values in JagexColor).
		// So it requires 64*128*8 = 65536 values to represent all colours (which is precisely how many different values a short can represent), which will be the
		// short-represented integers -32768 to 32767. Note that we can simply try to store positive values and those above 32767 will be wrapped to the appropriate
		// negative value for us.
		for (int i = 0; i < 65536; i++){

			all_colours[i] = (short) i;
			new_colours[i] = JagexColor.packHSL(0, 7, 51); // Red

		}

		for (int i = 0; i < 94; i++){

			all_textures[i] = (short) i;
			new_textures[i] = (short) 56; // Red and pink lava. I think this is the best texture match I can get.

		}

		reset();

	}

	@Override
	protected void shutDown() throws Exception {

		log.info("No New Items stopped!");

		reset();

	}

	@Subscribe
	public void onConfigChanged(ConfigChanged change)
	{
		if (change.getGroup().equals("NoNewItems")){

			reset();

		}
	}

	public void hide_appearance(ItemComposition new_item){

		new_item.setColorToReplace(all_colours);
		new_item.setColorToReplaceWith(new_colours);

		new_item.setTextureToReplace(all_textures);
		new_item.setTextureToReplaceWith(new_textures);

	}

	public void hide_item(ItemComposition new_item){

		hide_appearance(new_item);

		if (config.hide_name()){

			new_item.setName(config.new_name());

		}

	}

	@Subscribe
	public void onPostItemComposition(PostItemComposition item_change) {
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

			if (!config.hide_coin_pouches()){

				// If we enter here, we're allowing coin pouches from pickpocketing.
				if ((id == 24703) || ((id >= 22521) && (id <= 22538))){
					return; // Leave without hiding the item.
				}

			}

			hide_item(new_item);

		}

	}

	@Provides
	NoNewItemsConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(NoNewItemsConfig.class);
	}
}
