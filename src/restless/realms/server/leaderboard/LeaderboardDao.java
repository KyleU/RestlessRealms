package restless.realms.server.leaderboard;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;

@Repository
public class LeaderboardDao extends AbstractDao<Leaderboard> {
    private static final int ROW_LIMIT = 50;
    
    private HibernateTemplate rowLimitedTemplate;

    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private PlayerCharacterDao playerCharacterDao;
    
    private Cache cache;

    public List<Leaderboard> generateAll() {
        if(cache != null) {
            cache.removeAll();
        }

        List<Leaderboard> ret = new ArrayList<Leaderboard>();
        for(LeaderboardType type : LeaderboardType.values()) {
            ret.add(get(type));
        }
        return ret;
    }
    
    public Leaderboard get(LeaderboardType type) {
        if(cache == null) {
            cache = cacheManager.getCache("restless.realms.server.leaderboard.Leaderboard");
        }
        
        Leaderboard leaderboard = (Leaderboard)(cache.get(type) == null ? null : cache.get(type).getValue());
        if(leaderboard == null) {
            leaderboard = generateLeaderboard(type);
            cache.put(new Element(type, leaderboard));
        }
        return leaderboard ;
    }
    
    @SuppressWarnings("unchecked")
    private Leaderboard generateLeaderboard(LeaderboardType type) {
        Leaderboard ret = new Leaderboard(type);
        List<Object[]> runRowLimitedQuery = (List<Object[]>)runRowLimitedQuery("leaderboard." + type.getQueryName());
        for(Object[] objectArray : runRowLimitedQuery) {
            String characterName = (String)objectArray[0];
            Object value = objectArray[1];
            
            PlayerCharacter player = playerCharacterDao.get(characterName);
            ret.add(new LeaderboardEntry(player, value));
        }
        return ret;
    }

    private List<?> runRowLimitedQuery(String queryKey, Object... params) {
        if(rowLimitedTemplate == null) {
            rowLimitedTemplate = new HibernateTemplate(template.getSessionFactory());
            rowLimitedTemplate.setMaxResults(ROW_LIMIT);
        }
        List<?> ret = rowLimitedTemplate.findByNamedQuery(queryKey, params);
        return ret;
    }
    
    @Override
    protected Class<?> getManagedClass() {
        return Leaderboard.class;
    }
}
