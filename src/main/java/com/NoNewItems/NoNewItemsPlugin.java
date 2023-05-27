package com.NoNewItems;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.PostItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
		name = "No New Items",
		description = "Avoid using items that were not originally part of Old School RuneScape.",
		tags = {"07", "07 only", "no updates", "no changes", "items", "no new items"}
		// Don't define loadWhenOutdated or enabledByDefault
)
public class NoNewItemsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private NoNewItemsConfig config;

	@Inject
	private ClientThread clientThread;

	public short[] all_colours = new short[65536];
	public short[] new_colours = new short[65536];

	public short[] all_textures = new short[94]; // Seemingly there are only 94 textures.
	public short[] new_textures = new short[94];

	public void reset(boolean show_reset_message){

		if (show_reset_message){
			clientThread.invokeLater(this::reset_message);
		}

		// Resetting these caches will force through the visual change in inventories
		// (including shops, worn equipment etc.), in the GE and on the floor. Unfortunately it doesn't
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
		// However, certain players seem to get stuck on the configuration of the plugin when they were 'discovered'.
		Player[] cached_players = client.getCachedPlayers();
		for (Player other : cached_players){
			if (other != null){
				(other.getPlayerComposition()).setHash();
			}
		}

	}

	public void reset_message(){
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "\n", null);
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "\nNo New Items Plugin: Please note that you may need to log out and back in for visual changes to some other players to fully take effect.\n", null);
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "\n", null);
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

		reset(true);

	}

	@Override
	protected void shutDown() throws Exception {

		log.info("No New Items stopped!");

		reset(true);

	}

	@Subscribe
	public void onConfigChanged(ConfigChanged change) {
		if (change.getGroup().equals("NoNewItems")){

			boolean changes_player_visual = change.getKey().equals("hide_god_wars") || change.getKey().equals("hide_varrock_armour");

			reset(changes_player_visual);

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

	public boolean is_a_new_item(int id){

		if (id > 11685){ // This seems to be the item ID cutoff from King's Ransom to the Nightmare Zone according to https://www.osrsbox.com/tools/item-search/.

			if (!config.hide_god_wars()){

				// If we enter here, then we are allowing items from the God Wars dungeon which appeared in the dungeon's original 2007 release to the main game.
				boolean valid_god_wars_item = ((id >= 11787) && (id < 11791)) || ((id >= 11793) && (id < 11844)); // These checks exclude the Armadyl crossbow (11785/6) and the Staff of the dead (11791/2).

				if (valid_god_wars_item){
					return false;
				}

			}

			if (!config.hide_varrock_armour()){

				// If we enter here, then we are allowing the Varrock armour 1 from the easy Varrock diary.
				if ((id == 13104) || (id == 18642)){
					return false;
				}

			}

			if (!config.hide_coin_pouches()){

				// If we enter here, we're allowing coin pouches from pickpocketing.
				if ((id == 24703) || ((id >= 22521) && (id <= 22538))){
					return false;
				}

			}

			if (!config.hide_bonds()){

				// If we enter here, we're allowing bonds for buying membership etc.
				boolean is_a_bond = (id == 13190) || (id == 13191) || (id == 13192) || (id == 15430) || (id == 15431);

				if (is_a_bond){
					return false;
				}

			}

			if (!config.hide_reward_caskets()){

				// If we enter here, we're allowing the new reward caskets from permitted tiers of clue.
				boolean is_a_valid_clue_reward = (id == 20544) || (id == 20545) || (id == 20546);

				if (is_a_valid_clue_reward){
					return false;
				}

			}

			return true;

		} else {

			// If we enter here, we're looking for new items which have been inserted at lower item IDs.

			if ((id == 3066) || (id == 3068) || (id == 3070) || (id == 3072)){ // Bronze javelin heads
				return true;
			}
			if ((id == 3074) || (id == 3076) || (id == 3078) || (id == 3082)){ // Iron javelin heads
				return true;
			}
			if ((id == 3084) || (id == 3086) || (id == 3088) || (id == 3090)){ // Steel javelin heads
				return true;
			}
			if ((id == 3092) || (id == 3242) || (id == 3244) || (id == 3246)){ // Mithril javelin heads
				return true;
			}
			if ((id == 3248) || (id == 3905) || (id == 3907) || (id == 3909)){ // Adamant javelin heads
				return true;
			}
			if ((id == 3911) || (id == 3913) || (id == 3915) || (id == 3917)){ // Rune javelin heads
				return true;
			}
			if ((id == 3919) || (id == 3921) || (id == 3923) || (id == 3925)){ // Dragon javelin heads
				return true;
			}
			if ((id == 3963) || (id == 3965) || (id == 3967)){ // Amethyst javelin heads
				return true;
			}
			if ((id == 3250) || (id == 3252) || (id == 3254) || (id == 3256)){ // Lava scale shard
				return true;
			}

			return false;

		}

	}

	@Subscribe
	public void onPostItemComposition(PostItemComposition item_change){

		ItemComposition new_item = item_change.getItemComposition();

		if (is_a_new_item(new_item.getId())){
			hide_item(new_item);
		}

	}

	@Provides
	NoNewItemsConfig provideConfig(ConfigManager configManager){
		return configManager.getConfig(NoNewItemsConfig.class);
	}
}
