package restless.realms.server.skill;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Length;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasPlayerNameId;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate=true)
@NamedQueries({
    @NamedQuery(name="skillset.get", query="select s from Skillset s where s.playerName = ?")
})
public class Skillset implements HasPlayerNameId {
	private String playerName;
	private List<Skill> skills;
	
	public Skillset() {
	}


    public Skillset(String playerName, List<Skill> skills) {
        this.playerName = playerName;
        this.skills = skills;
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
	
	@ManyToMany(fetch=FetchType.EAGER)
	@ForeignKey(name="Skillset_Player_FK", inverseName = "Skillset_Skill_FK")
    @JoinTable(
            name="Skillset_Skill",
            joinColumns = @JoinColumn( name="playerName"),
            inverseJoinColumns = @JoinColumn( name="skillId")
    )
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @IndexColumn(name="orderIndex")
	public List<Skill> getSkills() {
		return skills;
	}
	public void setSkills(List<Skill> skills) {
		this.skills = skills;
	}
}