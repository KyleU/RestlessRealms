package restless.realms.test.server.combat;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import restless.realms.server.adventure.Adventure;
import restless.realms.server.adventure.AdventureDao;
import restless.realms.server.combat.Combat;
import restless.realms.server.combat.CombatDao;
import restless.realms.server.combat.CombatParticipant;
import restless.realms.server.combat.CombatRound;
import restless.realms.server.combat.CombatState;
import restless.realms.server.combat.Combat.CombatType;
import restless.realms.server.combat.CombatParticipant.Role;
import restless.realms.server.combat.CombatParticipant.Type;
import restless.realms.server.session.Session;
import restless.realms.server.util.JsonUtils;
import restless.realms.test.server.IntegrationTestCase;

public class CombatTest extends IntegrationTestCase {
    @Autowired
    private CombatDao combatDao;
    
    @Autowired
    private AdventureDao adventureDao;
    
    @Test
    public void testSerialization() {
        Combat c = getTestCombat();
        String s = JsonUtils.toString(c);
        Assert.assertNotNull(s);
    }
    
    @Test
    public void testPersistance() {
        Session s = getTestSession();
        Adventure adventure = adventureDao.startAdventure(s, "sandbox");
        Assert.assertNotNull(adventure);
        Assert.assertTrue(adventure.getId() > 0);
        Assert.assertTrue(adventure.getActiveRoomIndex() == 0);
        adventureDao.move(adventure.getId(), 2);
        adventureDao.move(adventure.getId(), 1);
        CombatState combatState = combatDao.getCombatState(s);
        Assert.assertNotNull(combatState);
        Assert.assertNotNull(combatState.getCombat());
        Assert.assertTrue(combatState.getCombat().getId() > 0);
    }


    public static Combat getTestCombat() {
        Combat combat = new Combat();
        
        combat.setId(0);
        combat.setCombatType(CombatType.PvE);
        ArrayList<CombatParticipant> participants = new ArrayList<CombatParticipant>();
        participants.add(new CombatParticipant(Role.ALLY, "Test User", Type.PLAYER));
        participants.add(new CombatParticipant(Role.ENEMY, "Goblin", Type.MOB));
        combat.setParticipants(participants);
        
        combat.setRounds(new ArrayList<CombatRound>());
        
        return combat;
    }
}
