package com.NoNewItems;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("07-items-only")
public interface NoNewItemsConfig extends Config
{

	@ConfigSection(
			name = "General",
			description = "General settings for forbidden items.",
			position = 0
	)
	String general_section = "General";

	@ConfigItem(
			position = 1,
			keyName = "hide_name",
			name = "Hide names",
			description = "If enabled, the names of forbidden items will be replaced with 'Non-07 Item'.",
			section = general_section
	)
	default boolean hide_name() { return true; }


	@ConfigSection(
			name = "Game Updates",
			description = "Settings concerning updates to the game which reintroduced content from 2007 that wasn't included in the backup used to create OSRS.",
			position = 1
	)
	String update_section = "Game Updates";

	@ConfigItem(
			position = 1,
			keyName = "hide_god_wars",
			name = "Hide God Wars dungeon items",
			description = "If enabled, all items from the God Wars dungeon (i.e. even those which were added to the main game in 2007 but weren't included in the initial OSRS launch) will be considered forbidden.",
			section = update_section
	)
	default boolean hide_god_wars() { return true; }

	@ConfigItem(
			position = 2,
			keyName = "hide_varrock_armour",
			name = "Hide Varrock armour 1",
			description = "If enabled, the Varrock armour 1 will be considered forbidden, despite the Varrock diary having originally been released in 2007 and the easy diary requiring no new content.",
			section = update_section
	)
	default boolean hide_varrock_armour() { return true; }

}
