package restless.realms.server.achievement;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Length;
import org.hibernate.validator.Range;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasPlayerNameId;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate=true)
@NamedQueries({
    @NamedQuery(name="leaderboard.getByAchievementScore", query="select a.playerName, a.pointsTotal from AchievementSet a order by a.pointsTotal desc"),
    @NamedQuery(name="achievementSet.get", query="select a from AchievementSet a where a.playerName = ?"),
})
public class AchievementSet implements HasPlayerNameId {
	private String playerName;
	private int pointsTotal;
	private Set<String> achievementIds;
	
	public AchievementSet() {
	}


    public AchievementSet(String playerName, Set<String> achievementIds) {
        this.playerName = playerName;
        this.achievementIds = achievementIds;
    }

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
	
	@Range(min=0)
	public int getPointsTotal() {
        return pointsTotal;
    }
	public void setPointsTotal(int pointsTotal) {
        this.pointsTotal = pointsTotal;
    }
	
    @CollectionOfElements(fetch=FetchType.LAZY)
    @JoinTable(
        joinColumns = @JoinColumn(name="playerName")
    )
	public Set<String> getAchievementIds() {
		return achievementIds;
	}
	public void setAchievementIds(Set<String> achievementIds) {
		this.achievementIds = achievementIds;
	}
}