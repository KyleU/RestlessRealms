package restless.realms.server.session;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasStringId;

@Entity
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@org.hibernate.annotations.Entity(dynamicUpdate=true)
public class Session implements HasStringId {
    private String id;
    private Integer accountId;
    private String characterName;
    private Date started;
    private boolean active;
    
    public Session() {
    }

    public Session(String id, Integer accountId, String characterName, Date started, boolean active) {
        super();
        this.id = id;
        this.accountId = accountId;
        this.characterName = characterName;
        this.started = started;
        this.active = active;
    }

    @Id
    @Length(min=36, max=36)
    @Override
    public String getId() {
        return id;
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }

    public Integer getAccountId() {
        return accountId;
    }
    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    @Length(max=FieldLengths.PLAYER_NAME)
    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
}
