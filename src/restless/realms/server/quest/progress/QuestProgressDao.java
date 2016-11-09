package restless.realms.server.quest.progress;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.inventory.Inventory;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.item.Item;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.quest.Quest;
import restless.realms.server.quest.QuestDao;
import restless.realms.server.quest.QuestDetails;
import restless.realms.server.quest.progress.QuestProgress.QuestStatus;
import restless.realms.server.treasure.Treasure;
import restless.realms.server.treasure.TreasureDao;

@Repository
public class QuestProgressDao extends AbstractDao<QuestProgress> {
    @Autowired
    private PlayerCharacterDao playerCharacterDao;
    
    @Autowired
    private QuestDao questDao;
    
    @Autowired
    private InventoryDao inventoryDao;
    
    @Autowired
    private TreasureDao treasureDao;

    @Autowired
    private StatisticsDao statisticsDao;
    
    @SuppressWarnings("unchecked")
    public List<QuestProgress> getActiveQuests(String playerCharacterName) {
        List<QuestProgress> ret = template.findByNamedQuery("questProgress.getActiveQuests", playerCharacterName);
        return ret ;
    }
    
    @SuppressWarnings("unchecked")
    public List<QuestProgress> getQuestProgresses(String playerCharacterName) {
        return template.findByNamedQuery("questProgress.getByPlayerCharacter", playerCharacterName);
    }

    public QuestProgress getProgress(String playerName, String questId) {
        return uniqueResult(template.findByNamedQuery("questProgress.get", playerName, questId));
    }
    
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public QuestProgress accept(String playerName, String questId) {
        PlayerCharacter pc = playerCharacterDao.get(playerName);
        List<QuestProgress> activeQuests = getActiveQuests(playerName);
        Quest quest = questDao.get(questId);
        if(!questDao.isAvailable(quest, pc, activeQuests)) {
            throw new IllegalArgumentException("You are not eligible for the quest \"" + quest.getName() + "\".");
        }
        
        QuestProgress progress = getProgress(playerName, questId);
        if(progress == null) {
            progress = new QuestProgress();
            progress.setPlayerCharacter(playerName);
            progress.setQuest(questId);
            progress.setCurrentStatus(QuestStatus.ACTIVE);
            template.save(progress);
        } else {
            if(progress.getCurrentStatus() == QuestStatus.ACTIVE) {
                throw new IllegalArgumentException("You have already accepted quest \"" + quest.getName() + "\".");
            } else if(progress.getCurrentStatus() == QuestStatus.ABANDONED) {
                // cool.
            } else if(progress.getCurrentStatus() == QuestStatus.COMPLETE) {
                if(!quest.isRepeatable()) {
                    throw new IllegalArgumentException("You have already completed quest \"" + quest.getName() + "\".");
                }
            } else {
                throw new IllegalArgumentException("Invalid quest status of \"" + progress.getCurrentStatus() + "\" for quest \"" + quest.getName() + "\".");
            }
        }

        progress.setCurrentStatus(QuestStatus.ACTIVE);
        int currentProgress = 0;
        if(quest.getCompletionItem() != null) {
            currentProgress = inventoryDao.getCount(playerName, quest.getCompletionItem());
            if(currentProgress > quest.getCompletionQuantity()) {
                currentProgress = quest.getCompletionQuantity();
            }
        }
        progress.setCurrentProgress(currentProgress);
        
        return progress;
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Map<String, Object> complete(String playerCharacterName, String questId) {
        PlayerCharacter playerCharacter = playerCharacterDao.get(playerCharacterName);
        Inventory inventory = inventoryDao.get(playerCharacterName);
        QuestProgress progress = getProgress(playerCharacterName, questId);
        QuestDetails questDetails = questDao.getDetails(questId, playerCharacter);
        Quest quest = questDetails.getQuest();
        
        if(progress == null || progress.getCurrentStatus() != QuestStatus.ACTIVE) {
            throw new IllegalArgumentException("Player " + playerCharacterName + " is not on quest \"" + questId + "\".");
        }
        
        if(quest.getCompletionItem() != null) {
            if(quest.getCompletionMobArchetype() != null) {
                throw new IllegalStateException("Both item and mob required for quest \"" + quest.getId() + "\", which is invalid.");
            }
            if(quest.getCompletionQuantity() != progress.getCurrentProgress()) {
                throw new IllegalStateException("You need " + quest.getCompletionQuantity() + " items, but only have " + progress.getCurrentProgress() + ".");
            }
            List<Item> itemsToRemove = new ArrayList<Item>();
            List<Item> items = inventory.getItems();
            for(Item item : items) {
                if(item.getId().equals(quest.getCompletionItem())) {
                    itemsToRemove.add(item);
                    if(itemsToRemove.size() == quest.getCompletionQuantity()) {
                        break;
                    }
                }
            }
            if(itemsToRemove.size() != quest.getCompletionQuantity()) {
                throw new IllegalStateException("You only have " + itemsToRemove.size() + " of the required item.");
            }
            items.removeAll(itemsToRemove);
        } else if(quest.getCompletionMobArchetype() != null) {
            if(quest.getCompletionQuantity() != progress.getCurrentProgress()) {
               throw new IllegalStateException("You need " + quest.getCompletionQuantity() + " kills, but only have " + progress.getCurrentProgress() + ".");
            }      
        } else {
            throw new IllegalStateException("No possible completion for quest \"" + quest.getId() + "\".");
        }
        
        int completions = progress.getCompletions();
        progress.setCompletions(completions + 1);
        progress.setCurrentStatus(QuestStatus.COMPLETE);
        progress.setCurrentProgress(0);
        
        if(quest.getRewardSkill() != null) {
            throw new IllegalArgumentException("Skill rewards not supported yet.");
        }
        if(quest.getRewardXp() > 0) {
            playerCharacterDao.addXp(playerCharacter, quest.getRewardXp());
        }

        Treasure treasure = treasureDao.create(questId);

        if(treasure.getCurrency() > 0) {
            inventoryDao.addCurrency(playerCharacter.getName(), treasure.getCurrency());
        }
        if(treasure.getTokens() > 0) {
            inventoryDao.addTokens(playerCharacter.getName(), treasure.getTokens());
        }
        List<Item> items = treasure.getItems();
        for(Item item : items) {
            if(item.getRequiredProfession() == null) {
                inventory.getItems().add(item);
            } else if(item.getRequiredProfession().equals(playerCharacter.getProfession())) {
                inventory.getItems().add(item);
            } else {
                //comment out for class-specific rewards. QuestDao.details() too.
                inventory.getItems().add(item);
            }
        }

        Map<String, Object> pc = playerCharacter.getClientRepresentation();
        pc.put("level", playerCharacter.getLevel());
        pc.put("xp", playerCharacter.getXp());

        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("pc", pc);
        ret.put("inventory", inventory);
        ret.put("questDetails", questDetails);
        ret.put("progress", progress);
        return ret;
    }
    
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Map<String, Object> abandon(String playerCharacterName, String questId) {
        PlayerCharacter playerCharacter = playerCharacterDao.get(playerCharacterName);
        Inventory inventory = inventoryDao.get(playerCharacterName);
        QuestProgress progress = getProgress(playerCharacterName, questId);
        QuestDetails questDetails = questDao.getDetails(questId, playerCharacter);
        Quest quest = questDetails.getQuest();
        
        if(progress == null || progress.getCurrentStatus() != QuestStatus.ACTIVE) {
            throw new IllegalArgumentException("Player " + playerCharacterName + " is not on quest \"" + questId + "\".");
        }
        
        if(quest.getCompletionItem() != null) {
            List<Item> itemsToRemove = new ArrayList<Item>();
            List<Item> items = inventory.getItems();
            for(Item item : items) {
                if(item.getId().equals(quest.getCompletionItem())) {
                    itemsToRemove.add(item);
                    if(itemsToRemove.size() == quest.getCompletionQuantity()) {
                        break;
                    }
                }
            }
            items.removeAll(itemsToRemove);
        }
        
        progress.setCurrentStatus(QuestStatus.ABANDONED);
        progress.setCurrentProgress(0);
        
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("pc", playerCharacter.getClientRepresentation());
        ret.put("inventory", inventory);
        ret.put("questDetails", questDetails);
        ret.put("progress", progress);
        return ret;
    }
    
    @Transactional
    public void incrementQuestProgressIfNeeded(String playerCharacterName, String killArchetype) {
        List<QuestProgress> progresses = getQuestProgresses(playerCharacterName);
        for(QuestProgress questProgress : progresses) {
            if(questProgress.getCurrentStatus() == QuestStatus.ACTIVE) {
                Quest q = questDao.get(questProgress.getQuest());
                if(q.getCompletionMobArchetype() != null) {
                    if(q.getCompletionMobArchetype().equals(killArchetype)) {
                        int currentProgress = questProgress.getCurrentProgress();
                        if(currentProgress < q.getCompletionQuantity()) {
                            questProgress.setCurrentProgress(currentProgress + 1);
                        }
                    }
                }
            }
        }
    }
    
    public void incrementQuestProgressIfNeeded(String playerCharacterName, Item item) {
        List<QuestProgress> progresses = getQuestProgresses(playerCharacterName);
        for(QuestProgress questProgress : progresses) {
            if(questProgress.getCurrentStatus() == QuestStatus.ACTIVE) {
                Quest q = questDao.get(questProgress.getQuest());
                if(q.getCompletionItem() != null) {
                    if(q.getCompletionItem().equals(item.getId())) {
                        int currentProgress = questProgress.getCurrentProgress();
                        if(currentProgress < q.getCompletionQuantity()) {
                            questProgress.setCurrentProgress(currentProgress + 1);
                        }
                    }
                }
            }
        }
    }
    
    public void decrementQuestProgressIfNeeded(String playerCharacterName, Item item) {
        List<QuestProgress> progresses = getQuestProgresses(playerCharacterName);
        for(QuestProgress questProgress : progresses) {
            if(questProgress.getCurrentStatus() == QuestStatus.ACTIVE) {
                Quest q = questDao.get(questProgress.getQuest());
                if(q.getCompletionItem() != null) {
                    if(q.getCompletionItem().equals(item.getId())) {
                        int currentProgress = questProgress.getCurrentProgress();
                        if(currentProgress > 0) {
                            questProgress.setCurrentProgress(currentProgress - 1);
                        }
                    }
                }
            }
        }
    }
    
	public boolean needsQuestItem(String playerCharacterName, Item item) {
	    //token id for magic alternate currency
	    if(item != null && item.getId().equals(100000)) {
	        return true;
	    }
	    
		boolean ret = false;
		List<QuestProgress> activeQuests = getActiveQuests(playerCharacterName);
		for (QuestProgress questProgress : activeQuests) {
			Quest quest = questDao.get(questProgress.getQuest());
			if(quest.getCompletionItem() != null) {
				if(quest.getCompletionItem().equals(item.getId())) {
					if(questProgress.getCurrentProgress() < quest.getCompletionQuantity()) {
						ret = true;
					}
					break;
				}
			}
		}
		return ret;
	}
	
    public List<Map<String, String>> getJournalPages(String characterName) {
        List<Map<String, String>> ret = new ArrayList<Map<String, String>>();

        Map<String, String> entry;
        
        QuestProgress progress = getProgress(characterName, "firststep");
        if(progress != null && progress.getCompletions() > 0) {
            entry = new LinkedHashMap<String, String>();
            entry.put("name", "Page 1");
            entry.put("journal", questDao.get("firststep").getCompletionText());
            ret.add(entry);
        }

        progress = getProgress(characterName, "secondstep");
        if(progress != null && progress.getCompletions() > 0) {
            entry = new LinkedHashMap<String, String>();
            entry.put("name", "Page 2");
            entry.put("journal", questDao.get("secondstep").getCompletionText());
            ret.add(entry);
        }

        progress = getProgress(characterName, "thirdstep");
        if(progress != null && progress.getCompletions() > 0) {
            entry = new LinkedHashMap<String, String>();
            entry.put("name", "Page 3");
            entry.put("journal", questDao.get("thirdstep").getCompletionText());
            ret.add(entry);
        }

        progress = getProgress(characterName, "fourthstep");
        if(progress != null && progress.getCompletions() > 0) {
            entry = new LinkedHashMap<String, String>();
            entry.put("name", "Page 4");
            entry.put("journal", questDao.get("fourthstep").getCompletionText());
            ret.add(entry);
        }

        progress = getProgress(characterName, "fifthstep");
        if(progress != null && progress.getCompletions() > 0) {
            entry = new LinkedHashMap<String, String>();
            entry.put("name", "Page 5");
            entry.put("journal", questDao.get("fifthstep").getCompletionText());
            ret.add(entry);
        }

        statisticsDao.increment(characterName, "journal-view");

        return ret;
    }

    @Override
    protected Class<?> getManagedClass() {
        return QuestProgress.class;
    }

}