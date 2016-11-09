package restless.realms.server.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.item.Item;
import restless.realms.server.item.ItemDao;
import restless.realms.server.mob.MobArchetype;
import restless.realms.server.mob.MobDao;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.quest.progress.QuestProgress;
import restless.realms.server.quest.progress.QuestProgress.QuestStatus;
import restless.realms.server.quest.progress.QuestProgressDao;
import restless.realms.server.skill.Skill;
import restless.realms.server.skill.SkillDao;
import restless.realms.server.treasure.ItemChance;
import restless.realms.server.treasure.Treasure;
import restless.realms.server.treasure.TreasureDao;
import restless.realms.server.treasure.TreasureTable;

@Service
public class QuestDao extends AbstractDao<Quest> {
    @Autowired
    private PlayerCharacterDao playerCharacterDao;
    
    @Autowired
    private InventoryDao inventoryDao;
    
    @Autowired
    private ItemDao itemDao;
    
    @Autowired
    private SkillDao skillDao;
    
    @Autowired
    private MobDao mobDao;
    
    @Autowired
    private TreasureDao treasureDao;

    @Autowired
    private QuestProgressDao questProgressDao;
    
    List<Quest> quests;
    private Map<String, Quest> questsById;
    
    public QuestDao() {
    }
    
    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        quests = template.findByNamedQuery("quest.getAll");
        questsById = new HashMap<String, Quest>();
        
        for(Quest quest : quests) {
            questsById.put(quest.getId(), quest);
        }
    }
    
    public Quest get(String id) {
        return questsById.get(id);
    }

    public List<Quest> getAvailableQuests(String playerCharacterName) {
        PlayerCharacter pc = playerCharacterDao.get(playerCharacterName);
        List<QuestProgress> questProgresses = questProgressDao.getQuestProgresses(playerCharacterName);

        List<Quest> ret = new ArrayList<Quest>();
        for(Quest quest : quests) {
            if(isAvailable(quest, pc, questProgresses)) {
                ret.add(quest);
            }
        }
        return ret;
    }

    public boolean isAvailable(Quest quest, PlayerCharacter pc, List<QuestProgress> questProgresses) {
        boolean available = false;
        
        if(pc.getLevel() >= quest.getQualificationLevel()) {
            if(quest.getQualificationProfession() == null) {
                available = true;
            } else {
                if(pc.getProfession().equals(quest.getQualificationProfession())) {
                    available = true;
                }
            }
        }
        
        if(available) {
            if(quest.getQualificationLevel() == 0) {
                available = false;
            }
            if(pc.getLevel() > (quest.getQualificationLevel() + 8)) {
                available = false;
            }
        }
        
        if(available && quest.getQualificationItem() != null) {
            int count = inventoryDao.getCount(pc.getName(), quest.getQualificationItem());
            available = (count > 0);
        }
        
        if(available) {
            for(QuestProgress questProgress : questProgresses) {
                if(quest.getId().equals(questProgress.getQuest())) {
                    if(questProgress.getCurrentStatus() == QuestStatus.ACTIVE) {
                        available = false;
                    } else if(questProgress.getCurrentStatus() == QuestStatus.ABANDONED) {
                        available = true;
                    } else {
                        //COMPLETED
                        available = quest.isRepeatable();
                    }
                    break;
                }
            }
        }
        
        return available;
    }

    public QuestDetails getDetails(String questId, PlayerCharacter pc) {
        Quest q = get(questId);
        QuestProgress progress = questProgressDao.getProgress(pc.getName(), questId);

        QuestDetails ret = new QuestDetails(q, progress);
        
        if(q.getQualificationItem() != null) {
            Item item = itemDao.get(q.getQualificationItem());
            ret.setQualificationItem(item);
        }

        if(q.getCompletionItem() != null) {
            Item item = itemDao.get(q.getCompletionItem());
            ret.setCompletionItem(item);
        }
        if(q.getCompletionMobArchetype() != null) {
            MobArchetype mobArchetype = mobDao.getArchetype(q.getCompletionMobArchetype());
            ret.setCompletionMobName(mobArchetype.getName());
        }
        if(q.getRewardSkill() != null) {
            Skill skill = skillDao.get(q.getRewardSkill());
            ret.setRewardSkill(skill);
        }
        
        TreasureTable table = treasureDao.getTable(questId);
        Treasure reward = new Treasure();
        reward.setCurrency(table.getMinCurrency());
        reward.setItems(new ArrayList<Item>());
        for(ItemChance chance : table.getItemChances()) {
            Item item = itemDao.get(chance.getItemId());
            if(item.getRequiredProfession() == null) {
                reward.getItems().add(item);
            } else if(item.getRequiredProfession().equals(pc.getProfession())) {
                reward.getItems().add(item);
            } else {
                //comment out for class-specific rewards. QuestProgressDao.complete() too.
                reward.getItems().add(item);
            }
        }
        ret.setTreasure(reward);
        
        return ret;
    }
    
    @Override
    protected Class<?> getManagedClass() {
        return Quest.class;
    }
}