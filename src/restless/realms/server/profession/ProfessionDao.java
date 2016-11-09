package restless.realms.server.profession;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.playercharacter.PlayerCharacter;

@Repository
public class ProfessionDao extends AbstractDao<PlayerCharacter> {
    private Map<String, Profession> professionsCache;
    private Map<String, SkillTree> skillTreeCache;
    
    public ProfessionDao() {
    }
    
    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        professionsCache = new LinkedHashMap<String, Profession>();
        skillTreeCache = new LinkedHashMap<String, SkillTree>();
        
        List<Profession> list = template.findByNamedQuery("profession.getAll");
        for(Profession profession : list) {
            professionsCache.put(profession.getId(), profession);
            SkillTree skillTree = template.get(SkillTree.class, profession.getId());
            if(skillTree == null) {
                throw new IllegalStateException("Profession \"" + profession.getId() + "\" has no skill tree.");
            }
            skillTreeCache.put(profession.getId(), skillTree);
        }
    }

    public Profession getProfession(String id) {
        Profession ret = professionsCache.get(id);
        if(ret == null) {
            throw new IllegalArgumentException("Invalid profession \"" + id + "\"");
        }
        return ret ;
    }
    
    public SkillTree getSkillTree(String id) {
        SkillTree ret = skillTreeCache.get(id);
        if(ret == null) {
            throw new IllegalArgumentException("Invalid skill tree \"" + id + "\"");
        }
        return ret ;
    }
    
    protected Class<?> getManagedClass() {
        return Profession.class;
    }
}