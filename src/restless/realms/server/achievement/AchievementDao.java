package restless.realms.server.achievement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.messaging.MessageType;
import restless.realms.server.messaging.MessagingUtils;

@Repository
public class AchievementDao extends AbstractDao<Achievement> {
    
    private int totalPossibleScore;
    private List<Achievement> allAchievements;
    private Map<String, List<Achievement>> achievementCache;
    
    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        allAchievements = Collections.unmodifiableList(template.findByNamedQuery("achievement.getAll"));
        totalPossibleScore = 0;
        achievementCache = new HashMap<String, List<Achievement>>();
        for(Achievement achievement : allAchievements) {
            totalPossibleScore += achievement.getPointValue();
            List<Achievement> val = achievementCache.get(achievement.getCompletionKey());
            if(val == null) {
                val = new ArrayList<Achievement>(1);
                achievementCache.put(achievement.getCompletionKey(), val);
            }
            val.add(achievement);
        }        
    }

    @Transactional
    public List<Achievement> getPlayerAchievements(String characterName) {
        AchievementSet set = template.get(AchievementSet.class, characterName);
        
        List<Achievement> ret = new ArrayList<Achievement>();
        if(set != null && set.getAchievementIds() != null) {
            for(Achievement achievement : allAchievements) {
                if(set.getAchievementIds().contains(achievement.getId())) {
                    ret.add(achievement);
                }
            }
        }
        return ret;
    }

    @Transactional
    public void statIncremented(String playerName, String statisticKey, int oldAmount, int newAmount) {
        List<Achievement> possibleAchievements = achievementCache.get(statisticKey);
        if(possibleAchievements != null) {
            for(Achievement possibleAchievement : possibleAchievements) {
                if(oldAmount < possibleAchievement.getCompletionQuantity()) {
                    if(newAmount >= possibleAchievement.getCompletionQuantity()) {
                        AchievementSet achievementSet = template.get(AchievementSet.class, playerName);
                        if(achievementSet == null) {
                            achievementSet = new AchievementSet();
                            achievementSet.setPlayerName(playerName);
                            achievementSet.setAchievementIds(new HashSet<String>(1));
                            template.save(achievementSet);
                        }
                        achievementSet.getAchievementIds().add(possibleAchievement.getId());
                        achievementSet.setPointsTotal(achievementSet.getPointsTotal() + possibleAchievement.getPointValue());
                        MessagingUtils.send(playerName, MessageType.ACHIEVEMENT, possibleAchievement);
                    }
                }
            }
        }
    }

    public Object getTotalPossibleScore() {
        return totalPossibleScore;
    }

    @Override
    protected Class<?> getManagedClass() {
        return Achievement.class;
    }
}
