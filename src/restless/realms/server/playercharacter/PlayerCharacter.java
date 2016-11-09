package restless.realms.server.playercharacter;

import java.util.Date;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Length;
import org.hibernate.validator.Min;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.effect.EffectTarget;


@Entity
@org.hibernate.annotations.Entity(dynamicUpdate=true)
@NamedQueries({
    @NamedQuery(name="player.getProperName", query="select p.name from PlayerCharacter p where p.name = ?"),
    @NamedQuery(name="player.getByAccount", query="select p from PlayerCharacter p where p.enabled = 1 and p.accountId = ?"),
    @NamedQuery(name="player.getNamesByLevel", query="select p.name from PlayerCharacter p where p.level = ? and p.enabled = true order by name"),
    @NamedQuery(name="leaderboard.getByLevel", query="select pc.name as name, level as value from PlayerCharacter pc order by level desc, xp desc, name desc"),
})
public class PlayerCharacter extends EffectTarget {
    private String name;
    private Integer accountId;
    
    private char gender;
    private String profession;
    private int xp;
    private String quickslots;

    private Integer activeAdventureId;
    
    private Date created;
    private Date lastSignedIn;
    
    private boolean enabled;
    
    public PlayerCharacter() {
    }
    
    public PlayerCharacter(String name, Integer accountId) {
        super();
        this.name = name;
        this.accountId = accountId;
        this.enabled = true;
        this.level = 1;
        this.xp = 0;
        this.created = new Date();
    }
    
    @Transient
    @Override
    public Map<String, Object> getClientRepresentation() {
        Map<String, Object> ret = super.getClientRepresentation();
        ret.put("name", getName());
        ret.put("profession", getProfession());
        return ret;
    }

    @Id
    @NotEmpty
    @Length(max=FieldLengths.PLAYER_NAME)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    @Column(updatable=false)
    public Integer getAccountId() {
        return accountId;
    }
    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
    
    @Basic
    public char getGender() {
        return gender;
    }
    public void setGender(char gender) {
        if(gender != 'M' && gender != 'F') {
            throw new IllegalArgumentException("Invalid gender \"" + gender + "\".");
        }
        this.gender = gender;
    }
    
    @NotEmpty
    @Length(max=FieldLengths.STRING_ID)
    public String getProfession() {
        return profession;
    }
    public void setProfession(String profession) {
        this.profession = profession;
    }

    @Min(0)
    public int getXp() {
        return xp;
    }
    public void setXp(int xp) {
        this.xp = xp;
    }
    
    @NotEmpty
    @Length(max=FieldLengths.QUICKSLOTS)
    public String getQuickslots() {
        return quickslots;
    }
    public void setQuickslots(String quickslots) {
        this.quickslots = quickslots;
    }
    
    public Integer getActiveAdventureId() {
        return activeAdventureId;
    }
    public void setActiveAdventureId(Integer activeAdventureId) {
        this.activeAdventureId = activeAdventureId;
    }

    @NotNull
    @Temporal(TemporalType.DATE)
    public Date getCreated() {
        return created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastSignedIn() {
        return lastSignedIn;
    }
    public void setLastSignedIn(Date lastSignedIn) {
        this.lastSignedIn = lastSignedIn;
    }
    
    @Basic
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}