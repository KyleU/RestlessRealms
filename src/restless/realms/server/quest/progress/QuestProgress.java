package restless.realms.server.quest.progress;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Range;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasIntegerId;

@Entity
@Table(name="QuestProgress", uniqueConstraints = {@UniqueConstraint(columnNames={"playerCharacter", "quest"})}
)
@NamedQueries({
    @NamedQuery(name="questProgress.get", query="select qp from QuestProgress qp where qp.playerCharacter = ? and qp.quest = ?"),
    @NamedQuery(name="questProgress.getByPlayerCharacter", query="select qp from QuestProgress qp where qp.playerCharacter = ?"),
    @NamedQuery(name="questProgress.getActiveQuests", query="select qp from QuestProgress qp where qp.playerCharacter = ? and qp.currentStatus = 'ACTIVE'")
})
public class QuestProgress implements HasIntegerId {
    public enum QuestStatus {
    	ACTIVE,
        ABANDONED,
    	COMPLETE
	}
	private Integer id;
    private String playerCharacter;
    private String quest;
    private QuestStatus currentStatus;
    private int currentProgress;
    private int completions;
    
    public QuestProgress() {
    }
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }
    
    @NotEmpty
    @Index(name="playerCharacterIndex")
    @Length(max=FieldLengths.PLAYER_NAME)
    public String getPlayerCharacter() {
        return playerCharacter;
    }
    public void setPlayerCharacter(String playerCharacter) {
        this.playerCharacter = playerCharacter;
    }
    
    @NotEmpty
    @Length(max=FieldLengths.QUEST_NAME)
    public String getQuest() {
        return quest;
    }
    public void setQuest(String quest) {
        this.quest = quest;
    }
    
    @NotNull
    @Enumerated(EnumType.STRING)
    public QuestStatus getCurrentStatus() {
        return currentStatus;
    }
    public void setCurrentStatus(QuestStatus currentStatus) {
        this.currentStatus = currentStatus;
    }
    
    @Range(min=0, max=100)
    public int getCurrentProgress() {
        return currentProgress;
    }
    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    @Basic
    public int getCompletions() {
        return completions;
    }
    public void setCompletions(int completions) {
        this.completions = completions;
    }
}