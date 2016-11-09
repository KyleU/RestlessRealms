package restless.realms.server.admin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.playercharacter.statistics.Statistic;
import restless.realms.server.playercharacter.statistics.StatisticsDao;

@Repository
public class AdminDao extends AbstractDao<Object> {
    
    @Autowired
    private HibernateTemplate template;
    
    @Autowired
    private StatisticsDao statisticsDao;
    
    public Map<String, Object> stats() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("Accounts", getCount("Account") + " (" + template.find("select count(distinct accountId) as awesomeAccounts from PlayerCharacter where level > 1").get(0) + ")");
        result.put("Warriors", template.find("select count(*) from PlayerCharacter where profession = 'warrior'").get(0) + " (" + template.find("select count(*) from PlayerCharacter where profession = 'warrior' and level > 1").get(0) + ")");
        result.put("Wizards", template.find("select count(*) from PlayerCharacter where profession = 'wizard'").get(0) + " (" + template.find("select count(*) from PlayerCharacter where profession = 'wizard' and level > 1").get(0) + ")");
        result.put("Clerics", template.find("select count(*) from PlayerCharacter where profession = 'cleric'").get(0) + " (" + template.find("select count(*) from PlayerCharacter where profession = 'cleric' and level > 1").get(0) + ")");
        result.put("Adventures", getCount("Adventure"));
        result.put("Combats", getCount("Combat"));
        result.put("Items", getCount("Item"));
        result.put("Skills", getCount("Skill"));
        result.put("Mob Types", getCount("MobArchetype"));
        
        result.put("Providers", "-----");
        @SuppressWarnings("unchecked")
        List<Object[]> providerCounts = template.find("select a.provider, count(a.provider) from Account a group by a.provider");
        for(Object[] objects : providerCounts) {
            result.put(objects[0].toString(), objects[1]);
        }
        
        result.put("Statistics", "-----");
        Map<String, Long> totalStats = statisticsDao.getTotals();
        for(Entry<String, Long> statistic : totalStats.entrySet()) {
            result.put(statistic.getKey(), statistic.getValue());
        }
        return result;
    }
    
    public Map<String, Object> stats(String name) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        
        List<Statistic> playerStats = statisticsDao.getPlayerStats(name);
        for(Statistic statistic : playerStats) {
            result.put(statistic.getKey().getStatistic(), statistic.getAmount());
        }
        
        return result;
    }
    
    private Object getCount(String type) {
        return template.find("select count(x) from " + type + " x").get(0);
    }
    
    @Override
    protected Class<?> getManagedClass() {
        return Object.class;
    }
}
