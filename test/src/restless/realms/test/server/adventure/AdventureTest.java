package restless.realms.test.server.adventure;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import restless.realms.server.adventure.Adventure;
import restless.realms.server.adventure.AdventureDao;
import restless.realms.server.adventure.AdventureFactory;
import restless.realms.server.combat.CombatDao;
import restless.realms.server.combat.CombatState;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.session.Session;
import restless.realms.test.server.IntegrationTestCase;

public class AdventureTest extends IntegrationTestCase {
    @Autowired 
    AdventureDao adventureDao;
    
    @Autowired 
    AdventureFactory adventureFactory;
    
    @Autowired 
    CombatDao combatDao;
    
    @Autowired
    PlayerCharacterDao playerDao;
    
    @Test
    public void testPersistance() {
        Session s = getTestSession();
        Adventure adventure = adventureDao.startAdventure(s, "sandbox");
        Assert.assertNotNull(adventure);
        Assert.assertTrue(adventure.getId() > 0);
    }

    @Test
    public void testMovement() {
        Session s = getTestSession();
        Adventure adventure = adventureDao.startAdventure(s, "sandbox");
        Assert.assertNotNull(adventure);
        Assert.assertTrue(adventure.getId() > 0);
        Assert.assertTrue(adventure.getActiveRoomIndex() == 0);
        adventureDao.move(adventure.getId(), 2);
        adventureDao.move(adventure.getId(), 1);
        Assert.assertEquals(1, adventure.getActiveRoomIndex());
        CombatState combatState = combatDao.getCombatState(s);
        Assert.assertNotNull(combatState);
        try {
            adventureDao.move(adventure.getId(), 2);
            Assert.fail();
        } catch(IllegalStateException e) {
        }
        try {
            adventureDao.move(adventure.getId(), 0);
            Assert.fail();
        } catch(IllegalStateException e) {
        }
    }
}
