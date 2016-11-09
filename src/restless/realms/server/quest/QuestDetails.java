package restless.realms.server.quest;

import restless.realms.server.item.Item;
import restless.realms.server.quest.progress.QuestProgress;
import restless.realms.server.skill.Skill;
import restless.realms.server.treasure.Treasure;

public class QuestDetails {
    private final Quest quest;
    private final QuestProgress progress;

    private Item qualificationItem;
    private Item completionItem;
    private String completionMobName;
    
    private Skill rewardSkill;

    private Treasure treasure;

    public QuestDetails(Quest quest, QuestProgress progress) {
        this.quest = quest;
        this.progress = progress;
    }

    public Quest getQuest() {
        return quest;
    }
    
    public QuestProgress getProgress() {
        return progress;
    }

    public Item getQualificationItem() {
        return qualificationItem;
    }

    public void setQualificationItem(Item qualificationItem) {
        this.qualificationItem = qualificationItem;
    }

    public Item getCompletionItem() {
        return completionItem;
    }

    public void setCompletionItem(Item completionItem) {
        this.completionItem = completionItem;
    }

    public String getCompletionMobName() {
        return completionMobName;
    }

    public void setCompletionMobName(String completionMobName) {
        this.completionMobName = completionMobName;
    }

    public Skill getRewardSkill() {
        return rewardSkill;
    }

    public void setRewardSkill(Skill rewardSkill) {
        this.rewardSkill = rewardSkill;
    }

    
    public Treasure getTreasure() {
        return treasure;
    }
    public void setTreasure(Treasure treasure) {
        this.treasure = treasure;
    }
}
