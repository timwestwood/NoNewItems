package com.NoNewItems;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class NoNewItemsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(NoNewItemsPlugin.class);
		RuneLite.main(args);
	}
}