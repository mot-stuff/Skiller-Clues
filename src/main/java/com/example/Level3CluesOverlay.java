package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.plugins.cluescrolls.ClueScrollPlugin;
import net.runelite.client.plugins.cluescrolls.clues.ClueScroll;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

public class Level3CluesOverlay extends OverlayPanel
{
	private final Level3CluesPlugin plugin;
	private final Level3CluesService service;

	@Inject
	private Level3CluesOverlay(Level3CluesPlugin plugin, Level3CluesService service)
	{
		super(plugin);
		this.plugin = plugin;
		this.service = service;
		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(PRIORITY_HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.getConfig().showIndicator())
		{
			return null;
		}

		ClueScrollPlugin clueScrollPlugin = plugin.getClueScrollPlugin();
		if (clueScrollPlugin == null)
		{
			return null;
		}

		ClueScroll clue = clueScrollPlugin.getClue();
		if (clue == null)
		{
			return null;
		}

		boolean isGood = service.isClueGoodForSkiller(clue, clueScrollPlugin);

		panelComponent.getChildren().add(LineComponent.builder()
			.left("lvl 3: " + (isGood ? "Good" : "Skip"))
			.leftColor(isGood ? Color.GREEN : Color.RED)
			.build());

		return super.render(graphics);
	}
}


