package restless.realms.server.playercharacter.party;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.util.ScaleOptions;

@Repository
public class PartyDao extends AbstractDao<Party> {
    private static final Log log = LogFactory.getLog(PartyDao.class);
        
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Party startParty(String playerName) {
        Party ret = getByPlayer(playerName);
        if(ret != null) {
            throw new IllegalStateException("You are already a member of " + ret.getLeader() + "'s party.");
        }
        ret = new Party();
        ret.setLeader(playerName);
        ret.setMembers(new ArrayList<String>(1));
        ret.getMembers().add(playerName);
        log.info("Created party.");
        return ret;
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Party joinParty(String playerName, Integer id) {
        Party ret = getByPlayer(playerName);
        if(ret != null) {
            throw new IllegalStateException("You are already a member of " + ret.getLeader() + "'s party.");
        }
        ret = get(id);
        if(ret == null) {
            throw new IllegalStateException("That party does not exist.");
        }
        if(ret.getMembers().size() == ScaleOptions.MAX_PARTY_SIZE) {
            throw new IllegalStateException("That party is already full.");
        }
        ret.getMembers().add(playerName);
        log.info("Joining " + ret.getLeader() + "'s party.");
        return ret;
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void leaveParty(String playerName) {
        Party ret = getByPlayer(playerName);
        if(ret == null) {
            throw new IllegalStateException("You're not in a party.");
        }
        ret.getMembers().remove(playerName);
        log.info("Leaving " + ret.getLeader() + "'s party.");
    }

    public Party getByPlayer(String playerName) {
        return uniqueResult(template.findByNamedQuery("party.getByPlayer", playerName));
    }
    
    @Override
    protected Class<?> getManagedClass() {
        return Party.class;
    }
}
