package restless.realms.server.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.profession.ProfessionDao;
import restless.realms.server.profession.SkillTree;
import restless.realms.server.util.ScaleOptions;

@Repository
public class SkillDao extends AbstractDao<Skill> {
    private static final Log log = LogFactory.getLog(SkillDao.class);
    
    @Autowired
    private PlayerCharacterDao playerDao;
    
    @Autowired
    private ProfessionDao professionDao;

    @Autowired
    private InventoryDao inventoryDao;
    
    @Autowired
    private StatisticsDao statisticsDao;
    
    public Skill get(Integer id) {
        return super.get(id);
    }
    
    @SuppressWarnings("unchecked")
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public List<Skill> getSkillset(String playerName) {
        List<Skillset> result = template.findByNamedQuery("skillset.get", playerName);
        Skillset skillset = null;
        if(result.size() == 1) {
            skillset = result.get(0);
        } else if(result.size() == 0) {
            PlayerCharacter p = playerDao.get(playerName);
            if(p == null) {
                IllegalStateException e = new IllegalStateException("Player does not exist!");
                log.error("No player found.", e);
                throw e;
            }
            log.debug("Creating skillset for player \"" + p.getName() + "\"");
            skillset = new Skillset();
            skillset.setPlayerName(playerName);
            List<Skill> startingSkills = new ArrayList<Skill>();
            List<Integer> initialSkillIds = professionDao.getProfession(p.getProfession()).getInitialSkillIds();
            for(Integer skillId : initialSkillIds) {
                Skill skill = get(skillId);
                if(skill == null) {
                    throw new IllegalStateException("Initial skills for profession \"" + p.getProfession() + "\" reference invalid skill \"" + skillId + "\"");
                }
                startingSkills.add(skill);
            }
            skillset.setSkills(startingSkills);

            if(p.getLevel() > 1) {
                SkillTree skillTree = professionDao.getSkillTree(p.getProfession());
                Set<Entry<Integer, List<Integer>>> skillIdsByLevel = skillTree.getSkillIdsByLevel().entrySet();
                for(Entry<Integer, List<Integer>> entry : skillIdsByLevel) {
                    if(entry.getKey() <= p.getLevel()) {
                        for(Integer skillId : entry.getValue()) {
                            Skill s = get(skillId);
                            startingSkills.add(s);
                        }
                    }
                }
            }
            template.save(skillset);
        }
        
        return skillset.getSkills();
	}
    
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public List<Skill> train(String playerName, Integer skillId, boolean isFree) {
        PlayerCharacter p = playerDao.get(playerName);
        SkillTree skillTree = professionDao.getSkillTree(p.getProfession());
        int level = skillTree.getLevel(skillId);
        int price = level * ScaleOptions.SKILL_COST_PER_LEVEL * ScaleOptions.ECONOMY;
        List<Skill> skillset = getSkillset(playerName);
        Skill s = get(skillId);
        if(s == null) {
            throw new IllegalStateException("Invalid skill \"" + skillId + "\".");
        }
        for(Skill skill : skillset) {
            if(skill.getId().equals(s.getId())) {
                throw new IllegalStateException("You already know \"" + s.getName() + "\".");
            }
        }
        if(p.getLevel() < level) {
            throw new IllegalStateException("You must be level " + level + " to learn " + s.getName() + ".");
        }

        int currency = inventoryDao.getCurrency(playerName);
        if(currency < price ) {
            throw new IllegalStateException("You cannot afford this skill, as you only have " + currency + " gold.");
        }

        inventoryDao.removeCurrency(playerName, price);

        statisticsDao.increment(playerName, "skill-train");
        statisticsDao.increment(playerName, "gold-spend", price);

        skillset.add(s);
        return skillset;
    }

    public void validateAction(PlayerCharacter character, Integer skillId) {
        Skill s = null;
        List<Skill> skillset = getSkillset(character.getName());
        for(Skill skill : skillset) {
            if(skill.getId().equals(skillId)) {
                s = skill;
                break;
            }
        }
        if(s == null) {
            log.error("Character \"" + character.getName() + "\" does not have skill " + skillId + ".");
            throw new IllegalStateException("You do not own skill " + skillId + ".");
        }
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void give(String characterName, Integer skillId) {
        List<Skill> skillset = getSkillset(characterName);
        Skill skill = get(skillId);
        log.info("Giving skill \"" + skill.getName() + " \" to player \"" + characterName + "\".");
        skillset.add(skill);
    }

    @Override
	protected Class<?> getManagedClass() {
		return Skill.class;
	}
}