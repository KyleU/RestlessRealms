package restless.realms.test.server.adventure;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.account.AccountDao;
import restless.realms.server.adventure.Adventure;
import restless.realms.server.adventure.AdventureController;
import restless.realms.server.adventure.AdventureDao;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.session.Session;
import restless.realms.test.server.IntegrationTestCase;

public class AdventureControllerTest extends IntegrationTestCase {
    @Autowired 
    private AdventureController adventureController;    

    @Autowired 
    private AccountDao accountDao;

    @Autowired 
    private AdventureDao adventureDao;
    
    @Autowired
    private PlayerCharacterDao playerCharacterDao;
    
    @Test
    public void testStart() {
        startAdventure();
    }

    @Test
    public void testAbandon() {
        Session session = getTestSession();
        Assert.assertNull(playerCharacterDao.get(session).getActiveAdventureId());
        adventureDao.startAdventure(session, "sandbox");
        Integer id = playerCharacterDao.get(session).getActiveAdventureId();
        Assert.assertNotNull(id);
        ModelAndView ret = adventureController.abandon(getMockRequest(session));
        Assert.assertTrue(ret.getModel().get("json").toString().contains("OK"));
        Adventure adventure = adventureDao.getAdventure(id);
        Assert.assertEquals(Adventure.Status.ABANDONED, adventure.getStatus());
    }

    @Test
    public void testResume() {
        Session session = getTestSession();
        startAdventure();
        Assert.assertEquals(19, accountDao.get(session.getAccountId()).getAdventurePoints());
        ModelAndView ret = adventureController.resume(getMockRequest(session));
        Assert.assertEquals(19, accountDao.get(session.getAccountId()).getAdventurePoints());
        Assert.assertTrue(ret.getModel().get("json").toString().contains("OK"));
    }

    @Test
    public void testMove() {
        Session session = getTestSession();
        startAdventure();
        ModelAndView ret = adventureController.move(getMockRequest(session), 2);
        Assert.assertTrue(ret.getModel().get("json").toString().contains("OK"));
    }

    private Adventure startAdventure() {
        Session session = getTestSession();
        Assert.assertNull(playerCharacterDao.get(session).getActiveAdventureId());
        ModelAndView ret = adventureController.start(getMockRequest(session), "sandbox");
        Assert.assertNotNull(ret.getModel().get("json"));
        Integer id = playerCharacterDao.get(session).getActiveAdventureId();
        Assert.assertNotNull(id);
        Adventure adventure = adventureDao.getAdventure(id);
        Assert.assertEquals(Adventure.Status.ACTIVE, adventure.getStatus());
        return adventure;
    }
}
