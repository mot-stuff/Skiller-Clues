package com.example;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.cluescrolls.ClueScrollPlugin;
import net.runelite.client.plugins.cluescrolls.clues.ClueScroll;
import net.runelite.client.plugins.cluescrolls.clues.CoordinateClue;
import net.runelite.client.plugins.cluescrolls.clues.CrypticClue;
import net.runelite.client.plugins.cluescrolls.clues.EmoteClue;
import net.runelite.client.plugins.cluescrolls.clues.LocationClueScroll;
import net.runelite.client.plugins.cluescrolls.clues.SkillChallengeClue;
import net.runelite.client.plugins.cluescrolls.clues.item.AllRequirementsCollection;
import net.runelite.client.plugins.cluescrolls.clues.item.AnyRequirementCollection;
import net.runelite.client.plugins.cluescrolls.clues.item.ItemRequirement;
import net.runelite.client.plugins.cluescrolls.clues.item.RangeItemRequirement;
import net.runelite.client.plugins.cluescrolls.clues.item.SingleItemRequirement;

@Singleton
public class Level3CluesService
{
	private static final Set<Integer> MORYTANIA_REGION_IDS = new HashSet<>(Arrays.asList(
		14388, 14389, 14390, 14391,
		14644, 14645, 14646, 14647,
		14899, 14900, 14901, 14902,
		15155, 15156, 15157, 15158
	));

	private static final Set<String> COMBAT_CHALLENGE_KEYWORDS = new HashSet<>(Arrays.asList(
		"kill", "slay", "defeat", "fight", "combat", "attack", "destroy", "eliminate"
	));

	private static final Set<Integer> QUEST_LOCKED_CLUE_ITEM_IDS = new HashSet<>();
	private static final Set<WorldPoint> QUEST_LOCKED_CLUE_LOCATIONS = new HashSet<>();
	private static final Set<String> QUEST_LOCKED_CLUE_TEXTS = new HashSet<>();

	static
	{
		populateQuestLockedClues();
	}

	private static void populateQuestLockedClues()
	{
		QUEST_LOCKED_CLUE_ITEM_IDS.clear();
		QUEST_LOCKED_CLUE_LOCATIONS.clear();
		QUEST_LOCKED_CLUE_TEXTS.clear();

		QUEST_LOCKED_CLUE_ITEM_IDS.addAll(Arrays.asList(
		));

		QUEST_LOCKED_CLUE_LOCATIONS.addAll(Arrays.asList(
		));

		QUEST_LOCKED_CLUE_TEXTS.addAll(Arrays.asList(
		));
	}

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	public boolean isClueGoodForSkiller(ClueScroll clue, ClueScrollPlugin clueScrollPlugin)
	{
		if (clue == null)
		{
			return true;
		}

		if (isInMorytania(clue, clueScrollPlugin))
		{
			return false;
		}

		if (requiresInaccessibleQuest(clue, clueScrollPlugin))
		{
			return false;
		}

		if (clue instanceof SkillChallengeClue)
		{
			SkillChallengeClue skillChallenge = (SkillChallengeClue) clue;
			String challenge = skillChallenge.getChallenge();
			if (challenge != null && requiresCombat(challenge))
			{
				return false;
			}
			String rawChallenge = skillChallenge.getRawChallenge();
			if (rawChallenge != null && requiresCombat(rawChallenge))
			{
				return false;
			}
			ItemRequirement[] requirements = skillChallenge.getItemRequirements();
			if (hasCombatRequirements(requirements))
			{
				return false;
			}
		}

		if (clue instanceof EmoteClue)
		{
			EmoteClue emoteClue = (EmoteClue) clue;
			ItemRequirement[] requirements = emoteClue.getItemRequirements();
			if (requirements != null && requirements.length > 0 && hasCombatRequirements(requirements))
			{
				return false;
			}
		}

		return true;
	}

	private boolean isInMorytania(ClueScroll clue, ClueScrollPlugin clueScrollPlugin)
	{
		if (clue instanceof LocationClueScroll)
		{
			LocationClueScroll locationClue = (LocationClueScroll) clue;
			WorldPoint location = locationClue.getLocation(clueScrollPlugin);
			if (location != null)
			{
				int regionId = location.getRegionID();
				return MORYTANIA_REGION_IDS.contains(regionId);
			}
		}
		return false;
	}

	private boolean requiresInaccessibleQuest(ClueScroll clue, ClueScrollPlugin clueScrollPlugin)
	{
		if (clue instanceof LocationClueScroll)
		{
			LocationClueScroll locationClue = (LocationClueScroll) clue;
			WorldPoint location = locationClue.getLocation(clueScrollPlugin);
			if (location != null && QUEST_LOCKED_CLUE_LOCATIONS.contains(location))
			{
				return true;
			}
		}

		if (clue instanceof EmoteClue)
		{
			EmoteClue emoteClue = (EmoteClue) clue;
			ItemRequirement[] requirements = emoteClue.getItemRequirements();
			if (requirements != null)
			{
				for (ItemRequirement req : requirements)
				{
					if (req instanceof SingleItemRequirement)
					{
						int itemId = getItemIdFromSingleRequirement((SingleItemRequirement) req, client);
						if (itemId != -1 && QUEST_LOCKED_CLUE_ITEM_IDS.contains(itemId))
						{
							return true;
						}
					}
				}
			}
			String text = emoteClue.getText();
			if (text != null)
			{
				String lowerText = text.toLowerCase();
				for (String questLockedText : QUEST_LOCKED_CLUE_TEXTS)
				{
					if (lowerText.contains(questLockedText.toLowerCase()))
					{
						return true;
					}
				}
			}
		}

		if (clue instanceof CoordinateClue)
		{
		}

		if (clue instanceof CrypticClue)
		{
			CrypticClue crypticClue = (CrypticClue) clue;
			String text = crypticClue.getText();
			if (text != null)
			{
				String lowerText = text.toLowerCase();
				for (String questLockedText : QUEST_LOCKED_CLUE_TEXTS)
				{
					if (lowerText.contains(questLockedText.toLowerCase()))
					{
						return true;
					}
				}
			}
		}

		if (clue instanceof SkillChallengeClue)
		{
			SkillChallengeClue skillChallenge = (SkillChallengeClue) clue;
			String challenge = skillChallenge.getChallenge();
			if (challenge != null)
			{
				String lowerText = challenge.toLowerCase();
				for (String questLockedText : QUEST_LOCKED_CLUE_TEXTS)
				{
					if (lowerText.contains(questLockedText.toLowerCase()))
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean requiresCombat(String challengeText)
	{
		String lowerText = challengeText.toLowerCase();
		return COMBAT_CHALLENGE_KEYWORDS.stream().anyMatch(lowerText::contains);
	}

	private boolean hasCombatRequirements(ItemRequirement[] requirements)
	{
		if (requirements == null || requirements.length == 0)
		{
			return false;
		}

		for (ItemRequirement requirement : requirements)
		{
			if (hasCombatRequirement(requirement))
			{
				return true;
			}
		}

		return false;
	}

	private boolean hasCombatRequirement(ItemRequirement requirement)
	{
		if (requirement instanceof SingleItemRequirement)
		{
			SingleItemRequirement singleReq = (SingleItemRequirement) requirement;
			String itemName = singleReq.getCollectiveName(client);
			if (itemName != null && !itemName.equals("N/A"))
			{
				return hasCombatItemName(itemName);
			}
			int itemId = getItemIdFromSingleRequirement(singleReq, client);
			if (itemId != -1)
			{
				return checkItemCombatRequirement(itemId);
			}
		}
		else if (requirement instanceof RangeItemRequirement)
		{
			RangeItemRequirement rangeReq = (RangeItemRequirement) requirement;
			String itemName = rangeReq.getCollectiveName(client);
			if (itemName != null && !itemName.equals("N/A"))
			{
				return hasCombatItemName(itemName);
			}
			int start = getStartItemId(rangeReq, client);
			int end = getEndItemId(rangeReq, client);
			return checkItemRangeCombatRequirement(start, end);
		}
		else if (requirement instanceof AnyRequirementCollection)
		{
			AnyRequirementCollection anyReq = (AnyRequirementCollection) requirement;
			String itemName = anyReq.getCollectiveName(client);
			if (itemName != null && !itemName.equals("N/A"))
			{
				return hasCombatItemName(itemName);
			}
			ItemRequirement[] subRequirements = getSubRequirements(anyReq, client);
			if (subRequirements == null || subRequirements.length == 0)
			{
				return false;
			}
			boolean allHaveCombatRequirements = true;
			for (ItemRequirement subReq : subRequirements)
			{
				if (!hasCombatRequirement(subReq))
				{
					allHaveCombatRequirements = false;
					break;
				}
			}
			return allHaveCombatRequirements;
		}
		else if (requirement instanceof AllRequirementsCollection)
		{
			AllRequirementsCollection allReq = (AllRequirementsCollection) requirement;
			String itemName = allReq.getCollectiveName(client);
			if (itemName != null && !itemName.equals("N/A"))
			{
				return hasCombatItemName(itemName);
			}
			ItemRequirement[] subRequirements = getSubRequirements(allReq, client);
			for (ItemRequirement subReq : subRequirements)
			{
				if (hasCombatRequirement(subReq))
				{
					return true;
				}
			}
		}

		return false;
	}

	private boolean hasCombatItemName(String itemName)
	{
		if (itemName == null)
		{
			return false;
		}

		String lowerName = itemName.toLowerCase();

		if (lowerName.contains("halberd") && (lowerName.contains("adamant") || lowerName.contains("rune") || 
		    lowerName.contains("dragon") || lowerName.contains("mithril") || lowerName.contains("steel")))
		{
			return true;
		}

		if (lowerName.contains("mystic") && (lowerName.contains("robe") || lowerName.contains("hat") || 
		    lowerName.contains("boots") || lowerName.contains("gloves")))
		{
			return true;
		}

		if ((lowerName.contains("rune") || lowerName.contains("adamant") || lowerName.contains("mithril") || 
		     lowerName.contains("steel") || lowerName.contains("dragon")) && 
		    (lowerName.contains("platebody") || lowerName.contains("platelegs") || lowerName.contains("plateskirt") ||
		     lowerName.contains("chainbody") || lowerName.contains("full helm") || lowerName.contains("kiteshield") ||
		     lowerName.contains("sq shield") || lowerName.contains("sword") || lowerName.contains("scimitar") ||
		     lowerName.contains("longsword") || lowerName.contains("battleaxe") || lowerName.contains("warhammer") ||
		     lowerName.contains("2h sword") || lowerName.contains("mace") || lowerName.contains("dagger")))
		{
			return true;
		}

		if (lowerName.contains("d'hide") || lowerName.contains("leather") || lowerName.contains("studded") ||
		    lowerName.contains("snakeskin") || lowerName.contains("karil") || lowerName.contains("armadyl"))
		{
			if (lowerName.contains("body") || lowerName.contains("chaps") || lowerName.contains("vambraces") ||
			    lowerName.contains("coif") || lowerName.contains("boots"))
			{
				return true;
			}
		}

		return false;
	}

	private int getItemIdFromSingleRequirement(SingleItemRequirement requirement, Client client)
	{
		String name = requirement.getCollectiveName(client);
		if (name == null || name.equals("N/A"))
		{
			return -1;
		}

		for (int itemId = 1; itemId < 100000; itemId++)
		{
			ItemComposition def = itemManager.getItemComposition(itemId);
			if (def != null && name.equals(def.getName()) && requirement.fulfilledBy(itemId))
			{
				return itemId;
			}
		}
		return -1;
	}

	private ItemRequirement[] getSubRequirements(AnyRequirementCollection collection, Client client)
	{
		java.util.List<ItemRequirement> requirements = new java.util.ArrayList<>();
		for (int itemId = 1; itemId < 100000; itemId++)
		{
			if (collection.fulfilledBy(itemId))
			{
				ItemComposition def = itemManager.getItemComposition(itemId);
				if (def != null && def.getName() != null && !def.getName().equals("null"))
				{
					requirements.add(new SingleItemRequirement(itemId));
				}
			}
		}
		return requirements.toArray(new ItemRequirement[0]);
	}

	private ItemRequirement[] getSubRequirements(AllRequirementsCollection collection, Client client)
	{
		java.util.List<ItemRequirement> requirements = new java.util.ArrayList<>();
		for (int itemId = 1; itemId < 100000; itemId++)
		{
			if (collection.fulfilledBy(itemId))
			{
				ItemComposition def = itemManager.getItemComposition(itemId);
				if (def != null && def.getName() != null && !def.getName().equals("null"))
				{
					requirements.add(new SingleItemRequirement(itemId));
				}
			}
		}
		return requirements.toArray(new ItemRequirement[0]);
	}

	private int getStartItemId(RangeItemRequirement requirement, Client client)
	{
		for (int itemId = 1; itemId < 100000; itemId++)
		{
			if (requirement.fulfilledBy(itemId))
			{
				ItemComposition def = itemManager.getItemComposition(itemId);
				if (def != null && def.getName() != null && !def.getName().equals("null"))
				{
					return itemId;
				}
			}
		}
		return -1;
	}

	private int getEndItemId(RangeItemRequirement requirement, Client client)
	{
		int lastItemId = -1;
		for (int itemId = 1; itemId < 100000; itemId++)
		{
			if (requirement.fulfilledBy(itemId))
			{
				ItemComposition def = itemManager.getItemComposition(itemId);
				if (def != null && def.getName() != null && !def.getName().equals("null"))
				{
					lastItemId = itemId;
				}
			}
		}
		return lastItemId;
	}

	private boolean checkItemCombatRequirement(int itemId)
	{
		if (itemId == -1)
		{
			return false;
		}

		ItemComposition itemDef = itemManager.getItemComposition(itemId);
		if (itemDef == null)
		{
			return false;
		}

		return hasCombatSkillRequirement(itemDef);
	}

	private boolean checkItemRangeCombatRequirement(int startItemId, int endItemId)
	{
		int maxCheck = Math.min(endItemId, startItemId + 100);
		for (int itemId = startItemId; itemId <= maxCheck; itemId++)
		{
			if (checkItemCombatRequirement(itemId))
			{
				return true;
			}
		}
		return false;
	}

	private boolean hasCombatSkillRequirement(ItemComposition itemDef)
	{
		if (itemDef == null)
		{
			return false;
		}

		return hasCombatItemName(itemDef.getName());
	}
}

