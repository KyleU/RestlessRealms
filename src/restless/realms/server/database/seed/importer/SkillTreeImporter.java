package restless.realms.server.database.seed.importer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import restless.realms.server.database.seed.PersistanceProvider;
import restless.realms.server.profession.Profession;
import restless.realms.server.profession.SkillTree;
import restless.realms.server.skill.Skill;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class SkillTreeImporter extends DataImporter {
    Map<String, StringBuilder> skillTrees;
    
    public SkillTreeImporter(PersistanceProvider persistanceProvider) {
        super(persistanceProvider);
        skillTrees = new LinkedHashMap<String, StringBuilder>();
    }

    @Override
    public void nextRow(CustomElementCollection e) {
        int level = 0;
        for(String tag : e.getTags()) {
            if(tag.equals("level")) {
                if(level > 0 && level <= getInt(e, tag)) {
                    throw new IllegalArgumentException("Level " + e.getValue(tag) + " is defined after level " + level + ".");
                }
                level = getInt(e, tag);
                if(level < 2) {
                    throw new IllegalArgumentException("Level " + level + " skills should not be defined here.");
                }
            } else {
                Profession p = persistanceProvider.getObject(Profession.class, tag);
                if(p == null) {
                    throw new IllegalArgumentException("Invalid profession \"" + tag + "\".");
                }
                Iterable<String> split = Splitter.on(",").trimResults().split(e.getValue(tag));
                for(String skillId : split) {
                    persistanceProvider.getObject(Skill.class, Integer.parseInt(skillId));
                }
                String skills = Joiner.on(",").join(split);
                if(!skillTrees.containsKey(tag)) {
                    skillTrees.put(tag, new StringBuilder());
                } else {
                    skillTrees.get(tag).append("/");
                }
                skillTrees.get(tag).append(level + ":" + skills);
            }
        }
        
    }
    
    @Override
    public void complete() {
        for(Entry<String, StringBuilder> entry : skillTrees.entrySet()) {
            SkillTree skillTree = new SkillTree();
            skillTree.setProfession(entry.getKey());
            skillTree.setTree(entry.getValue().toString());
            persistanceProvider.save(entry.getKey(), skillTree);
        }
    }
}
