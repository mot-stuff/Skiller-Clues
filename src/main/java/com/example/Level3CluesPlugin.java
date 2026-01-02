package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.game.ItemManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.cluescrolls.ClueScrollPlugin;
import net.runelite.client.plugins.cluescrolls.clues.ClueScroll;
import net.runelite.client.plugins.cluescrolls.clues.MapClue;
import net.runelite.client.plugins.cluescrolls.clues.MusicClue;
import net.runelite.client.plugins.cluescrolls.clues.CoordinateClue;
import net.runelite.client.plugins.cluescrolls.clues.AnagramClue;
import net.runelite.client.plugins.cluescrolls.clues.CipherClue;
import net.runelite.client.plugins.cluescrolls.clues.CrypticClue;
import net.runelite.client.plugins.cluescrolls.clues.EmoteClue;
import net.runelite.client.plugins.cluescrolls.clues.FairyRingClue;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@PluginDescriptor(
	name = "Level 3 Clues",
	description = "Indicates whether clues are good or bad for level 3 skillers",
	tags = {"clues", "skiller", "level3"}
)
@PluginDependency(ClueScrollPlugin.class)
@Slf4j
public class Level3CluesPlugin extends Plugin
{
	@Inject
	private ClueScrollPlugin clueScrollPlugin;

	@Inject
	private Level3CluesConfig config;

	public Level3CluesConfig getConfig()
	{
		return config;
	}

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Level3CluesOverlay overlay;

	@Inject
	private Level3CluesWorldOverlay worldOverlay;

	@Inject
	private ItemManager itemManager;

	@Inject
	private Level3CluesService service;

	@Getter
	private final Map<Integer, ClueScroll> trackedClues = new ConcurrentHashMap<>();

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuOption() == null)
		{
			return;
		}

		if (event.getMenuOption().equals("Read"))
		{
			int itemId = event.getItemId();
			ItemComposition itemComposition = itemManager.getItemComposition(itemId);
			
			if (itemComposition != null && (itemComposition.getName().startsWith("Clue scroll")
				|| itemComposition.getName().startsWith("Challenge scroll")
				|| itemComposition.getName().startsWith("Treasure scroll")))
			{
				ClueScroll clue = findClueScroll(itemId);
				if (clue != null)
				{
					trackedClues.put(itemId, clue);
				}
			}
		}
	}


	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		overlayManager.add(worldOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		overlayManager.remove(worldOverlay);
	}

	@Provides
	Level3CluesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(Level3CluesConfig.class);
	}

	public ClueScrollPlugin getClueScrollPlugin()
	{
		return clueScrollPlugin;
	}

	private ClueScroll findClueScroll(int itemId)
	{
		if (itemId == ItemID.TRAIL_CLUE_BEGINNER || itemId == ItemID.TRAIL_CLUE_MASTER)
		{
			return null;
		}

		ClueScroll clue = MapClue.forItemId(itemId);
		if (clue != null)
		{
			return clue;
		}

		clue = MusicClue.forItemId(itemId);
		if (clue != null)
		{
			return clue;
		}

		clue = CoordinateClue.forItemId(itemId);
		if (clue != null)
		{
			return clue;
		}

		clue = AnagramClue.forItemId(itemId);
		if (clue != null)
		{
			return clue;
		}

		clue = CipherClue.forItemId(itemId);
		if (clue != null)
		{
			return clue;
		}

		clue = CrypticClue.forItemId(itemId);
		if (clue != null)
		{
			return clue;
		}

		clue = EmoteClue.forItemId(itemId);
		if (clue != null)
		{
			return clue;
		}

		clue = FairyRingClue.forItemId(itemId);
		if (clue != null)
		{
			return clue;
		}

		return null;
	}
}

