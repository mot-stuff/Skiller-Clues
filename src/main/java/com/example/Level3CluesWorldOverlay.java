package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.ParamID;
import net.runelite.api.Perspective;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.cluescrolls.ClueScrollPlugin;
import net.runelite.client.plugins.cluescrolls.clues.ClueScroll;
import net.runelite.client.plugins.cluescrolls.clues.LocationClueScroll;
import net.runelite.client.plugins.cluescrolls.clues.LocationsClueScroll;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class Level3CluesWorldOverlay extends Overlay
{
	private final Level3CluesPlugin plugin;
	private final Level3CluesService service;
	private final Client client;

	@Inject
	private Level3CluesWorldOverlay(Level3CluesPlugin plugin, Level3CluesService service, Client client)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.plugin = plugin;
		this.service = service;
		this.client = client;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.getConfig().clueHighlights())
		{
			return null;
		}

		Map<Integer, ClueScroll> trackedClues = plugin.getTrackedClues();
		if (trackedClues.isEmpty())
		{
			return null;
		}

		ClueScrollPlugin clueScrollPlugin = plugin.getClueScrollPlugin();
		if (clueScrollPlugin == null)
		{
			return null;
		}

		Level3CluesConfig config = plugin.getConfig();

		Scene scene = client.getScene();
		Tile[][][] tiles = scene.getTiles();
		int plane = client.getPlane();

		for (int x = 0; x < 104; x++)
		{
			for (int y = 0; y < 104; y++)
			{
				Tile tile = tiles[plane][x][y];
				if (tile == null || tile.getGroundItems() == null)
				{
					continue;
				}

				for (TileItem item : tile.getGroundItems())
				{
					if (item == null)
					{
						continue;
					}

					ClueScroll trackedClue = trackedClues.get(item.getId());
					if (trackedClue != null)
					{
						ItemComposition itemComp = client.getItemDefinition(item.getId());
						if (itemComp != null && itemComp.getIntValue(ParamID.CLUE_SCROLL) != -1)
						{
							boolean isGood = service.isClueGoodForSkiller(trackedClue, clueScrollPlugin);
							Color tileColor = isGood ? config.goodClueTileColor() : config.badClueTileColor();
							Color textColor = isGood ? config.goodClueTextColor() : config.badClueTextColor();
							String text = isGood ? "Good Clue" : "Bad Clue";

							WorldPoint worldPoint = tile.getWorldLocation();
							LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
							
							if (localPoint != null && worldPoint.isInScene(client))
							{
								Polygon tilePoly = Perspective.getCanvasTilePoly(client, localPoint);
								if (tilePoly != null)
								{
									OverlayUtil.renderPolygon(graphics, tilePoly, tileColor);
								}

								net.runelite.api.Point textPoint = Perspective.localToCanvas(client, localPoint, client.getPlane(), 50);
								if (textPoint != null)
								{
									FontMetrics fontMetrics = graphics.getFontMetrics();
									int textWidth = fontMetrics.stringWidth(text);
									net.runelite.api.Point centeredTextPoint = new net.runelite.api.Point(
										textPoint.getX() - textWidth / 2,
										textPoint.getY()
									);
									OverlayUtil.renderTextLocation(graphics, centeredTextPoint, text, textColor);
								}
							}
						}
					}
				}
			}
		}

		return null;
	}
}

