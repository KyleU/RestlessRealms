package restless.realms.server.perk;

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
    @NamedQuery(name="perkset.get", query="select p from Perkset p where p.playerName = ?")
})
public class Perkset implements HasPlayerNameId {
	private String playerName;
	private List<Perk> perks;
	
	public Perkset() {
	}


    public Perkset(String playerName, List<Perk> perks) {
        this.playerName = playerName;
        this.perks = perks;
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
	@ForeignKey(name="Perkset_Player_FK", inverseName = "Perkset_Perk_FK")
    @JoinTable(
            name="Perkset_Perk",
            joinColumns = @JoinColumn( name="playerName"),
            inverseJoinColumns = @JoinColumn( name="perkId")
    )
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @IndexColumn(name="orderIndex")
	public List<Perk> getPerks() {
		return perks;
	}
	public void setPerks(List<Perk> perks) {
		this.perks = perks;
	}
}