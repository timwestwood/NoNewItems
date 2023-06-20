package com.NoNewItems;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("NoNewItems")
public interface NoNewItemsConfig extends Config
{

	@ConfigSection(
			name = "General",
			description = "General settings for new items.",
			position = 0
	)
	String general_section = "General";

	@ConfigItem(
			position = 1,
			keyName = "hide_name",
			name = "Replace names",
			description = "If enabled, the names of new items will be replaced.",
			section = general_section
	)
	default boolean hide_name() { return true; }

	@ConfigItem(
			position = 2,
			keyName = "new_name",
			name = "Replacement name",
			description = "The name that will be displayed in-game for new items if 'Replace names' is enabled.",
			section = general_section
	)
	default String new_name() { return "NEW ITEM"; }





	@ConfigSection(
			name = "Game Updates",
			description = "Settings concerning items that were not present in the backup used to create OSRS.",
			position = 1
	)
	String update_section = "Game Updates";

	@ConfigItem(
			position = 1,
			keyName = "hide_god_wars",
			name = "Hide God Wars dungeon items",
			description = "If enabled, all items from the God Wars dungeon (i.e. even those which were added to the main game in 2007 but weren't included in the initial OSRS launch) will be considered new.",
			section = update_section
	)
	default boolean hide_god_wars() { return true; }

	@ConfigItem(
			position = 2,
			keyName = "hide_varrock_armour",
			name = "Hide Varrock armour 1",
			description = "If enabled, the Varrock armour 1 will be considered new, despite the Varrock diary having originally been released in 2007 and the easy diary requiring no new content.",
			section = update_section
	)
	default boolean hide_varrock_armour() { return true; }

	@ConfigItem(
			position = 3,
			keyName = "hide_coin_pouches",
			name = "Hide coin pouches",
			description = "If enabled, coin pouches from pickpocketing will be considered new.",
			section = update_section
	)
	default boolean hide_coin_pouches() { return true; }

	@ConfigItem(
			position = 4,
			keyName = "hide_bonds",
			name = "Hide bonds",
			description = "If enabled, Old School bonds will be considered new.",
			section = update_section
	)
	default boolean hide_bonds() { return true; }

	@ConfigItem(
			position = 5,
			keyName = "hide_reward_caskets",
			name = "Hide reward caskets",
			description = "If enabled, reward caskets from easy, medium and hard clues will be considered new.",
			section = update_section
	)
	default boolean hide_reward_caskets() { return true; }

	@ConfigItem(
			position = 6,
			keyName = "hide_seed_nests",
			name = "Hide seed nests",
			description = "If enabled, the updated bird nests containing seeds (from Wyson or otherwise) will be considered new.",
			section = update_section
	)
	default boolean hide_seed_nests() { return true; }

}
