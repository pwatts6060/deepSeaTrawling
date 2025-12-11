package com.deepseatrawling;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("deepseatrawling")
public interface DeepSeaTrawlingConfig extends Config
{
	@ConfigItem(
		keyName = "showGiantKrill",
		name = "Show Giant Krill Shoals",
		description = "Highlight Giant Krill Shoals"
	)
	default boolean showGiantKrill()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showHaddock",
			name = "Show Haddock Shoals",
			description = "Highlight Haddock Shoals"
	)
	default boolean showHaddock()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showYellowfin",
			name = "Show Yellowfin Shoals",
			description = "Highlight Yellowfin Shoals"
	)
	default boolean showYellowfin()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showHalibut",
			name = "Show Halibut Shoals",
			description = "Highlight Halibut Shoals"
	)
	default boolean showHalibut()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showBluefin",
			name = "Show Bluefin Shoals",
			description = "Highlight Bluefin Shoals"
	)
	default boolean showBluefin()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showMarlin",
			name = "Show Marlin Shoals",
			description = "Highlight Marlin Shoals"
	)
	default boolean showMarlin()
	{
		return true;
	}

	/*
	@ConfigItem(
			keyName = "showShimmering",
			name = "Show Shimmering Shoals",
			description = "Highlight Shimmering Shoals"
	)
	default boolean showShimmering()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showGlistening",
			name = "Show Glistening Shoals",
			description = "Highlight Glistening Shoals"
	)
	default boolean showGlistening()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showVibrant",
			name = "Show Vibrant Shoals",
			description = "Highlight Vibrant Shoals"
	)
	default boolean showVibrant()
	{
		return true;
	}
*/
	@ConfigItem(
			keyName = "dumpLogs",
			name = "Dump Logs",
			description = "Dump Logs"
	)
	default boolean dumpLogs() {
		return false;
	}
}
