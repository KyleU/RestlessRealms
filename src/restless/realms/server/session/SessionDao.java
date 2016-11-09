package restless.realms.server.session;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.database.AbstractDao;

@Repository("sessionDao")
public class SessionDao extends AbstractDao<Session> {
    private static final Log log = LogFactory.getLog(SessionDao.class);
    
    public Session get(String id) {
        return super.get(id);
    }
    
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Session create(int accountId) {
        //TODO invalidate other sessions?
        String id = UUID.randomUUID().toString();
        log.debug("Creating new session \"" + id + "\".");
        Session s = new Session();
        s.setId(id);
        s.setAccountId(accountId);
        s.setStarted(new Date());
        s.setActive(true);
        template.save(s);
        
        return s;
    }
    
    @Transactional
    public void signout(String id) {
        Session s = get(id);
        s.setActive(false);
    }

    @Transactional
    public void setCharacterName(String id, String characterName) {
        Session s = get(id);
        s.setCharacterName(characterName);
    }

    @Override
    protected Class<?> getManagedClass() {
        return Session.class;
    }
}
