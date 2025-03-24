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

import java.util.Arrays;

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

	public int[] inserted_item_ids;

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

		// This should force through the change on all rendered players, including the user.
		// However, certain players seem to get stuck on the configuration of the plugin when they were 'discovered'.
		if (client.getTopLevelWorldView() != null){
			for (Player player : client.getTopLevelWorldView().players()){
				if (player != null){
					(player.getPlayerComposition()).setHash();
				}
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

		// This is an array of item IDs for new items which have been inserted into the 'old item space' of IDs below 11685,
		// rather than having been appended to the end of the item ID list. We sort the array and then exploit this fact to
		// minimise the number of comparisons we make.
		inserted_item_ids = new int[] {3066, 3068, 3070, 3072, // Bronze javelin heads
										3074, 3076, 3078, 3082, // Iron javelin heads
										3084, 3086, 3088, 3090, // Steel javelin heads
										3092, 3242, 3244, 3246, // Mithril javelin heads
										3248, 3905, 3907, 3909, // Adamant javelin heads
										3911, 3921, 3923, 3925, // Dragon javelin heads
										3963, 3965, 3967, // Amethyst javelin heads
										3927, 3929, 3931, 3933, // Javelin shaft
										3250, 3252, 3254, 3256, // Lava scale shard
										599, 1589, 2420, 2512, // Buchu seed
										1649, 1650, 1651, // Bullet arrow
										1652, 1653, 6564, // Glistening tear
										2425, // Vorkath's head
										3272, // cave kraken
										3902, 4000, 4076, 4177, // Amethyst broad bolts
										3951, 4762, 4767, 4768, // Amethyst bolt tips
										4763, 4764, 4765, 4766, // Amethyst arrow (p)
										4769, 4770, 4771, 4772, // Amethyst arrow
										3955, 3957, 3959, 3961, // Amylase crystal
										3973, 3975, 3977, 3979, // Grape seed
										3981, // Wilderness sword
										3983, // Western banner
										3985, 3987, 3989, 3991, // Platinum token
										3993, 3995, 3997, 3999, // Zulrah's scales
										4028, // Ancient gorilla greegree
										4036, 4280, 4282, 4296, // Broad bolts
										4312, 4626, 4706, 5069, // Unfinished broad bolts
										4449, 4451, 4453, 4455, // Broad arrowheads
										5093, // Morytania legs
										5095, // Explorer's ring
										5349, // Smoke devil
										6205, 6207, 6210, 6381, // Barbed arrow
										1668, 1691, 6566, // Dragonstone dragon bolts (e)
										7938, // Dark essence fragments
										8465, 8467, 8469, 8471, // Dragon bolts (unf)
										8473, 8475, 8477, 8479, // Dragon bolts
										8481, 8483, 8485, 8487, // Dragon bolts (p)
										8489, 8491, 8493, 8495, // Opal dragon bolts
										8651, 8653, 8655, 8657, // Jade dragon bolts
										8659, 8661, 8663, 8665, // Pearl dragon bolts
										8667, 8669, 8671, 8673, // Topaz dragon bolts
										8675, 8677, 8679, 8681, // Sapphire dragon bolts
										8683, 8685, 8687, 8689, // Emerald dragon bolts
										8691, 8693, 8695, 8697, // Ruby dragon bolts
										8699, 8701, 8703, 8705, // Diamond dragon bolts
										8707, 8709, 8711, 8713, // Dragonstone dragon bolts
										8715, 8717, 8719, 8721, // Onyx dragon bolts
										8723, 8725, 8727, 8729, // Opal dragon bolts (e)
										8731, 8733, 8735, 8737, // Jade dragon bolts (e)
										8739, 8741, 8743, 8745, // Peal dragon bolts (e)
										8747, 8749, 8751, 8753, // Topaz dragon bolts (e)
										8755, 8757, 8759, 8761, // Sapphire dragon bolts (e)
										8763, 8765, 8767, 8769, // Emerald dragon bolts (e)
										8771, 8773, 8775, 8777, // Ruby dragon bolts (e)
										1687, 1688, 1689, 1690, // Diamond dragon bolts (e)
										1669, 1670, 1671, 1672, // Onyx dragon bolts (e)
										9815, 9816, 10165, 10166, 10601, 10602, 10666, 10669, // Spirit flakes
										10573, 10575, 10577, 10579, // Noxifer seed
										10574, 10576, 10578, 10580, // Golpar seed
										// Ids 9906-9919 correspond to inserted items from the 2013 Halloween event. But the items have all been removed and so we don't actually compare against them.
		};
		Arrays.sort(inserted_item_ids);

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

			boolean changes_player_visual = change.getKey().equals("hide_god_wars")
											|| change.getKey().equals("hide_varrock_armour")
											|| change.getKey().equals("hide_agility_cape")
											|| change.getKey().equals("hide_land_of_the_goblins") // The goblin mails are wearable.
											|| change.getKey().equals("hide_crystal_shield_and_bow");

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
				boolean valid_god_wars_item = ((id >= 11787) && (id < 11791)) || ((id >= 11793) && (id < 11847)); // These checks exclude the Armadyl crossbow (11785/6) and the Staff of the dead (11791/2).

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

			if (!config.hide_seed_nests()){

				// If we enter here, we're allowing the new seed nests.
				boolean is_a_new_seed_nest = (id >= 22798) && (id <= 22801);

				if (is_a_new_seed_nest){
					return false;
				}

			}

			if (!config.hide_agility_cape()){

				// If we enter here, we're allowing the new agility cape(s).
				boolean is_a_new_agility_cape = (id == 13340) || (id == 13341) || (id == 14234) || (id == 14235);

				if (is_a_new_agility_cape){
					return false;
				}

			}

			if (!config.hide_land_of_the_goblins()){

				// If we enter here, we're allowing the 'Land of the Goblins' quest.
				boolean is_from_lotg = (id >= 26567) && (id <= 26593);

				if (is_from_lotg){
					return false;
				}

			}

			if (!config.hide_crystal_shield_and_bow()){

				// If we enter here, we're allowing the new crystal shield and bow.
				boolean is_new_crystal_equip = (id == 16890) || (id == 23991) || (id == 23992) || (id == 23993) || (id == 23994) || (id == 24127) || (id == 24128) // shield
												|| (id == 16888) || (id == 23983)  || (id == 23984)  || (id == 23985)  || (id == 23986)  || (id == 24123)  || (id == 24124) ; // bow

				if (is_new_crystal_equip){
					return false;
				}

			}

			if (!config.hide_path_of_glouphrie()){

				// If we enter here, we're allowing the quest 'The Path of Glouphrie'.
				boolean is_from_pog = (id >= 28573) && (id <= 28590);

				if (is_from_pog){
					return false;
				}

			}

			return true; // The default for id > 11685

		} else {

			// If we enter here, we're looking for new items which have been inserted at lower item IDs.
			// We have stored these IDs in the (sorted) array "inserted_item_ids".

			if (id <= inserted_item_ids[inserted_item_ids.length - 1]){ // Exploit fact that inserted_item_ids is in ascending order.

				for (int inserted_id : inserted_item_ids){

					if (id < inserted_id){

						break; // Exploit fact that inserted_item_ids is in ascending order.

					} else {

						if (id == inserted_id){
							return true;
						}

					}

				}

			}

			return false; // The default for id <= 11685

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
