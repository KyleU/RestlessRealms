package restless.realms.server.playercharacter.statistics;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.hibernate.annotations.Index;
import org.hibernate.validator.Length;

import restless.realms.server.database.FieldLengths;

@Embeddable
public class StatisticKey implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String playerName;
    private String statistic;
    
    public StatisticKey() {
    }
    
    public StatisticKey(String playerName, String statistic) {
        super();
        this.playerName = playerName;
        this.statistic = statistic;
    }

    @Index(name="StatisticPlayerNameIndex")
    @Length(max=FieldLengths.PLAYER_NAME)
    public String getPlayerName() {
        return playerName;
    }
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Length(max=FieldLengths.STATISTIC_KEY)
    @Index(name="StatisticKeyIndex")
    public String getStatistic() {
        return statistic;
    }
    public void setStatistic(String statistic) {
        this.statistic = statistic;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((playerName == null) ? 0 : playerName.hashCode());
        result = prime * result + ((statistic == null) ? 0 : statistic.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        StatisticKey other = (StatisticKey)obj;
        if(playerName == null) {
            if(other.playerName != null)
                return false;
        } else if(!playerName.equals(other.playerName))
            return false;
        if(statistic == null) {
            if(other.statistic != null)
                return false;
        } else if(!statistic.equals(other.statistic))
            return false;
        return true;
    }
}
