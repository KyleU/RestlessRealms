package restless.realms.server.profession;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

import restless.realms.server.database.FieldLengths;

import com.google.common.base.Splitter;

@Entity
public class SkillTree {
    private String profession;
    private String tree;
    private Map<Integer, List<Integer>> skillIdsByLevel;
    
    @Id
    @NotEmpty
    @Length(max=FieldLengths.STRING_ID)
    public String getProfession() {
        return profession;
    }
    public void setProfession(String profession) {
        this.profession = profession;
    }
    
    //format "2:1000,1001/3:2000,2001/5:3000"
    @Length(max=1000)
    public String getTree() {
        return tree;
    }
    public void setTree(String tree) {
        this.tree = tree;
        parse();
    }
    
    @Transient
    public Map<Integer, List<Integer>> getSkillIdsByLevel() {
        return skillIdsByLevel;
    }
    
    private void parse() {
        skillIdsByLevel = new LinkedHashMap<Integer, List<Integer>>();
        Iterable<String> entries = Splitter.on("/").split(tree);
        for(String entry : entries) {
            Iterator<String> entryIterator = Splitter.on(':').split(entry).iterator();
            Integer level = Integer.parseInt(entryIterator.next());
            String skillIds = entryIterator.next();
            if(entryIterator.hasNext()) {
                throw new IllegalArgumentException("Invalid skill tree entry \"" + entry + "\"");
            }
            List<Integer> skills = new ArrayList<Integer>();
            Iterable<String> skillIdStrings = Splitter.on(",").trimResults().split(skillIds);
            for(String skillId : skillIdStrings) {
                skills.add(Integer.parseInt(skillId));
            }
            skillIdsByLevel.put(level, skills);
        }
    }
    
    public int getLevel(Integer skillId) {
        int ret = -1;
        for(Entry<Integer, List<Integer>> entry : skillIdsByLevel.entrySet()) {
            if(entry.getValue().contains(skillId)) {
                ret = entry.getKey();
                break;
            }
        }
        return ret;
    }
}
