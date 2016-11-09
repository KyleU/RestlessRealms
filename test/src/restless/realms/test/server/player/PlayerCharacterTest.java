package restless.realms.test.server.player;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.test.server.IntegrationTestCase;

public class PlayerCharacterTest extends IntegrationTestCase {
	@Autowired
	private PlayerCharacterDao playerDao;
	
    @Test
    public void testGet() {
        PlayerCharacter p = playerDao.get("Test User");
        Assert.assertEquals("Test User", p.getName());
    }
    
    @Test
    public void testSave() {
        PlayerCharacter p = playerDao.get("Test User");
        p.setMaxHitpoints(90);
        p.setXp(5);
        PlayerCharacter p2 = playerDao.get("Test User"); 
        Assert.assertEquals(90, p.getMaxHitpoints());
        Assert.assertEquals(90, p2.getMaxHitpoints());
        Assert.assertEquals(5, p.getXp());
        Assert.assertEquals(5, p2.getXp());
    }

    @Test
    public void testTransactionalityOfTests() {
        PlayerCharacter p = playerDao.get("Test User");
        Assert.assertEquals(300, p.getMaxHitpoints());
        Assert.assertEquals(0, p.getXp());
    }

    public static PlayerCharacter getTestPlayerCharacter() {
        PlayerCharacter playerCharacter = new PlayerCharacter("Test User", 0);
        playerCharacter.setProfession("warrior");
        playerCharacter.setMaxHitpoints(100);
        playerCharacter.setMaxMana(50);
        return playerCharacter;

    }
    
}
