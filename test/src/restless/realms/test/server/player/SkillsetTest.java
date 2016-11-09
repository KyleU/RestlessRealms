package restless.realms.test.server.player;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.skill.Skill;
import restless.realms.server.skill.SkillDao;
import restless.realms.test.server.IntegrationTestCase;

public class SkillsetTest extends IntegrationTestCase {
    @Autowired
    private SkillDao skillDao;
    
    @Autowired
    private PlayerCharacterDao playerCharacterDao;
    
    @Test
    public void testGet() {
        List<Skill> skills = skillDao.getSkillset("Test User");
        Assert.assertEquals(3, skills.size());
        Assert.assertEquals("Initiative", skills.get(0).getName());
    }

    @Test
    public void testTrain() {
        PlayerCharacter pc = playerCharacterDao.get("Test User");
        pc.setLevel(3);
        Skill skill = skillDao.get(1003);
        List<Skill> skills = skillDao.getSkillset("Test User");
        Assert.assertEquals(3, skills.size());
        Assert.assertEquals(1001, skills.get(1).getId().intValue());

        skillDao.train("Test User", skill.getId(), false);
        
        List<Skill> newSkills = skillDao.getSkillset("Test User");
        Assert.assertEquals(3, newSkills.size());
        Assert.assertEquals(1003, newSkills.get(2).getId().intValue());
        Assert.assertTrue(pc.getQuickslots().contains("s1003"));
    }
}