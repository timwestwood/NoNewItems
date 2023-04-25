package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface ExampleConfig extends Config
{

	@ConfigItem(
			position = 1,
			keyName = "hide_name",
			name = "Hide names?",
			description = "Should the names of hidden items be replaced with 'Non-07 Item'?"
	)
	default boolean hide_name() { return true; }

}
