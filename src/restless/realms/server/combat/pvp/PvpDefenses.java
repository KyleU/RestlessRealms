package restless.realms.server.combat.pvp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Length;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasPlayerNameId;

import com.google.common.base.Splitter;

@Entity
@NamedQueries({
    @NamedQuery(name="pvpDefenses.getAllWithBodyguards", query="select p from PvpDefenses p where length(p.enemiesString) > 0"),
    @NamedQuery(name="leaderboard.getByDuelScore", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'duelscore' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByPvpOffensiveWins", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'pvp-offensive-win' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByPvpOffensiveLosses", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'pvp-offensive-loss' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByPvpDefensiveWins", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'pvp-defensive-win' group by s.key.playerName order by sum(s.amount) desc"),
    @NamedQuery(name="leaderboard.getByPvpDefensiveLosses", query="select s.key.playerName as name, sum(s.amount) as value from Statistic s where s.key.statistic = 'pvp-defensive-loss' group by s.key.playerName order by sum(s.amount) desc"),
})
public class PvpDefenses implements HasPlayerNameId {
    private static final String[] emptyStringArray = new String[0];
    private static Splitter splitter = Splitter.on(',').trimResults().omitEmptyStrings();

    private String playerName;
    private String skillsString;
    private int[] skills;
    private String enemiesString;
    private String[] enemies;
    
    @Id
    @Length(max=FieldLengths.PLAYER_NAME)
    @Override
    public String getPlayerName() {
        return playerName;
    }
    @Override
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    @Basic
    public String getSkillsString() {
        return skillsString;
    }
    public void setSkillsString(String skillsString) {
        this.skillsString = skillsString;
        String[] skillStrings = split(skillsString);
        this.skills = new int[skillStrings.length];
        for(int i = 0; i < skillStrings.length; i++) {
            String skillString = skillStrings[i];
            this.skills[i] = Integer.parseInt(skillString);
        }
    }
    
    @Transient
    public int[] getSkills() {
        return skills;
    }
    
    @Basic
    public String getEnemiesString() {
        return enemiesString;
    }
    public void setEnemiesString(String enemiesString) {
        this.enemiesString = enemiesString;
        enemies = split(enemiesString);
    }
    
    @Transient
    public String[] getEnemies() {
        return enemies;
    }

    private static String[] split(String string) {
        if(string == null || string.length() == 0) {
            return emptyStringArray;
        }
        List<String> ret = new ArrayList<String>();
        Iterator<String> iterator = splitter.split(string).iterator();
        while(iterator.hasNext()) {
            ret.add(iterator.next());
        }
        return ret.toArray(emptyStringArray);
    }
}