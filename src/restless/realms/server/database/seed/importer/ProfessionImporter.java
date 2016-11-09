package restless.realms.server.database.seed.importer;

import java.util.ArrayList;
import java.util.List;

import restless.realms.server.database.seed.PersistanceProvider;
import restless.realms.server.item.Item;
import restless.realms.server.profession.Profession;
import restless.realms.server.skill.Skill;
import restless.realms.server.util.ScaleOptions;

import com.google.common.base.Splitter;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.util.common.base.Joiner;

public class ProfessionImporter extends DataImporter {
    public ProfessionImporter(PersistanceProvider persistanceProvider) {
        super(persistanceProvider);
    }

    @Override
    public void nextRow(CustomElementCollection e) {
        Splitter splitter = Splitter.on(",").trimResults();
        Joiner joiner = Joiner.on(",");
        Iterable<String> split = splitter.split(e.getValue("initialSkills"));
        for(String skillId : split) {
            persistanceProvider.getObject(Skill.class, Integer.parseInt(skillId));
        }
        String skills = joiner.join(split);
        
        split = splitter.split(e.getValue("initialEquipment"));
        List<Integer> initialEquipmentIds = new ArrayList<Integer>();
        for(String itemId : split) {
            int id = Integer.parseInt(itemId);
            if(id > 0) {
                initialEquipmentIds.add(id);
                persistanceProvider.getObject(Item.class, id);
            }
        }
        String equipment = joiner.join(split);
        
        split = splitter.split(e.getValue("initialItems"));
        List<Integer> initialItemIds = new ArrayList<Integer>();
        for(String itemId : split) {
            int id = Integer.parseInt(itemId);
            initialItemIds.add(id);
            persistanceProvider.getObject(Item.class, id);
        }
        String items = joiner.join(split);
        
        for(Integer i : initialEquipmentIds) {
            if(!initialItemIds.contains(i)) {
                throw new IllegalArgumentException("All initial equipment for \"" + e.getValue("id") + "\" must also be in initial items.");
            }
        }
        
        split = splitter.split(e.getValue("initialQuickslots"));
        String quickslots = joiner.join(split);
        
        Profession p = new Profession(
                e.getValue("id"), 
                e.getValue("name"),
                getInt(e, "minLevel"),
                getInt(e, "initialHitpoints") * ScaleOptions.COMBAT,
                getInt(e, "hitpointsPerLevel") * ScaleOptions.COMBAT,
                getInt(e, "initialMana") * ScaleOptions.COMBAT,
                getInt(e, "manaPerLevel") * ScaleOptions.COMBAT,
                skills,
                equipment,
                items,
                quickslots
        );
        persistanceProvider.save(p.getId(), p);
    }
}
