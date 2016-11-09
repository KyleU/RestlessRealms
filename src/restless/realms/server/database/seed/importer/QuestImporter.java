package restless.realms.server.database.seed.importer;

import restless.realms.server.adventure.AdventureArchetype;
import restless.realms.server.database.seed.PersistanceProvider;
import restless.realms.server.item.Item;
import restless.realms.server.mob.MobArchetype;
import restless.realms.server.profession.Profession;
import restless.realms.server.quest.Quest;
import restless.realms.server.treasure.ItemChance;
import restless.realms.server.treasure.TreasureTable;
import restless.realms.server.util.ScaleOptions;

import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class QuestImporter extends DataImporter {
    public QuestImporter(PersistanceProvider persistanceProvider) {
        super(persistanceProvider);
    }
    
    @Override
    public void nextRow(CustomElementCollection e) {
        Integer qualificationItem = null;
        if(e.getValue("qualificationItem") != null) {
            qualificationItem = getInt(e, "qualificationItem");
            persistanceProvider.getObject(Item.class, qualificationItem);
        }
        
        String qualificationProfession = null;
        if(e.getValue("qualificationProfession") != null) {
            qualificationProfession = e.getValue("qualificationProfession");
            persistanceProvider.getObject(Profession.class, qualificationProfession);
        }
        
        String completionAdventure = e.getValue("completionAdventure");
        persistanceProvider.getObject(AdventureArchetype.class, completionAdventure);

        Integer completionItem = null;
        if(e.getValue("completionItem") != null) {
            completionItem = getInt(e, "completionItem");
            persistanceProvider.getObject(Item.class, completionItem);
        }
        
        String completionMobArchetype = null;
        if(e.getValue("completionKill") != null) {
            completionMobArchetype = e.getValue("completionKill");
            persistanceProvider.getObject(MobArchetype.class, e.getValue("completionKill"));
        }
        
        int completionQuantity = getInt(e, "completionQuantity");
        
        String rewardTreasureTable = e.getValue("id");
        TreasureTable table = persistanceProvider.getObject(TreasureTable.class, rewardTreasureTable);
        if(table.getMinCurrency() != table.getMaxCurrency()) {
            throw new IllegalArgumentException("Quest treasure tables must have the same min and max currency.");
        }
        for(ItemChance chance : table.getItemChances()) {
            if(chance.getTenthPercentChance() != 1000) {
                throw new IllegalArgumentException("Quest treasure table items must have a 100% chance.");
            }
        }
        
        if(completionItem != null && completionMobArchetype != null) {
            throw new IllegalStateException("Quest " + e.getValue("id") + " has both a completion item and mob.");
        }
        
        int suggestedLevel = getInt(e, "suggestedLevel");
        int qualificationLevel = suggestedLevel - 2;
        if(qualificationLevel < 1) {
            qualificationLevel = 1;
        }
        
        int rewardXp = getInt(e, "rewardXp");
        rewardXp = ScaleOptions.QUEST_XP_MULTIPLIER * rewardXp;
        Quest q = new Quest(
                e.getValue("id"), 
                e.getValue("name"),
                suggestedLevel,
                qualificationLevel,
                qualificationItem,
                qualificationProfession,
                getBoolean(e.getValue("repeatable")),
                completionAdventure,
                completionItem,
                completionMobArchetype,
                completionQuantity,
                rewardXp,
                e.getValue("rewardSkill") == null ? null : getInt(e, "rewardSkill"),
                e.getValue("progressText"),
                e.getValue("introText"),
                e.getValue("completionText")
        );
        persistanceProvider.save(q.getId(), q);
        persistanceProvider.verifyUnique("Quest name", q.getName());
    }
}
