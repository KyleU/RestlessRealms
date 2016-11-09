package restless.realms.server.playercharacter.statistics;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.achievement.AchievementDao;
import restless.realms.server.database.AbstractDao;

@Repository
public class StatisticsDao extends AbstractDao<Statistic> {
    @Autowired
    private AchievementDao achievementDao;
    
    public int increment(String playerName, String statisticKey) {
        return increment(playerName, statisticKey, 1);
    }

    @Transactional
    public int increment(String playerName, String statisticKey, int amount) {
        StatisticKey key = new StatisticKey(playerName, statisticKey);
        Statistic stat = get(key);
        if(stat == null) {
            stat = new Statistic();
            stat.setKey(key);
            stat.setAmount(0);
            template.save(stat);
        }
        int oldAmount = stat.getAmount();
        int newAmount = stat.getAmount() + amount;
        if(newAmount < 0) {
            newAmount = 0;
        }
//        log.debug("Setting " + playerName + "\'s \"" + statisticKey + "\" statistic +" + amount + ", to " + newAmount + ".");
        achievementDao.statIncremented(playerName, statisticKey, oldAmount, newAmount);
        stat.setAmount(newAmount);
        template.update(stat);
        return newAmount;
    }
    
    public int get(String playerName, String statisticKey) {
        StatisticKey key = new StatisticKey(playerName, statisticKey);
        Statistic stat = get(key);
        int ret = 0;
        if(stat != null) {
            ret = stat.getAmount();
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public List<Statistic> getPlayerStats(String name) {
        List<Statistic> playerStats = template.findByNamedQuery("statistic.getByPlayer", name);
        return playerStats;
    }

    @Override
    protected Class<?> getManagedClass() {
        return Statistic.class;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Long> getTotals() {
        Map<String, Long> ret = new LinkedHashMap<String, Long>();
        List<Object[]> totals = template.findByNamedQuery("statistic.getTotals");
        for(Object[] strings : totals) {
            ret.put(strings[0].toString(), Long.parseLong(strings[1].toString()));
        }
        return ret;
    }
}
