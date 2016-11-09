package restless.realms.server.account;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Service;

import restless.realms.server.combat.pvp.PvpCombatDao;

@Service
public class AdventurePointsScheduler {
    private static final Log log = LogFactory.getLog(AdventurePointsScheduler.class);

    //also in spring.xml
    private static final String CRON_EXPRESSION = "0 0 0/8 * * ?";
    //private static final String CRON_EXPRESSION = "0 * * * * ?";
    private static CronSequenceGenerator csg = new CronSequenceGenerator(CRON_EXPRESSION, TimeZone.getDefault());

    @Autowired
    private HibernateTemplate template;

    @Autowired
    private PvpCombatDao pvpCombatDao;
    
    public void fire() {
        incrementAdventurePoints();
        chargeUpkeepCosts();
    }
    
    private void incrementAdventurePoints() {
        log.info("Incrementing adventure points @ " + new Date().toString());
        int affectedAccounts = template.bulkUpdate("update Account set adventurePoints = adventurePoints + 1 where adventurePoints < 5");
        log.info("Incremented adventure points for " + affectedAccounts + " accounts.");
    }

    private void chargeUpkeepCosts() {
        log.info("Incrementing upkeep costs @ " + new Date().toString());
        pvpCombatDao.chargeUpkeepCosts();
    }
    
    public static Date getNext() {
        Date now = new Date();
        Date next = csg.next(now);
        return next;
    }
}
