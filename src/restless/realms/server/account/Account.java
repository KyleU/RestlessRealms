package restless.realms.server.account;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Range;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasIntegerId;
import restless.realms.server.util.ScaleOptions;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate=true)
@NamedQueries({
    @NamedQuery(name="account.getAll", query="select a from Account a"),
    @NamedQuery(name="account.getByEmail", query="select a from Account a where a.email = ?", cacheable=true),
    @NamedQuery(name="account.getByIdentifier", query="select a from Account a where a.identifier = ?", cacheable=true)
})
public class Account implements HasIntegerId {
    private Integer id;
    
    private String provider;
    private String identifier;
    
    private String email;
    private String name;
    private String additionalInfo;
    private int timezone;
    private String locale;
    
    private boolean admin;
    private boolean enabled;
    
    private int adventurePoints;

    private Date created;
    private Date lastSignedIn;
    
    public Account() {
        admin = false;
        enabled = true;
        adventurePoints = ScaleOptions.STARTING_APS;
        created = new Date();
    }
    
    public Account(String provider, String identifier, String email, String name, String additionalInfo, int timezone, String locale, boolean admin, boolean enabled, Date created, Date lastSignedIn) {
        super();
        this.provider = provider;
        this.identifier = identifier;
        this.email = email;
        this.name = name;
        this.additionalInfo = additionalInfo;
        this.timezone = timezone;
        this.locale = locale;
        this.admin = admin;
        this.enabled = enabled;
        this.created = created;
        this.lastSignedIn = lastSignedIn;
        this.adventurePoints = 10;
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

    @Length(max=30)
    public String getProvider() {
        return provider;
    }
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    @Column(unique=true)
    @Length(max=250)
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Column(unique=true, nullable=true)
    @Length(max=FieldLengths.ACCOUNT_EMAIL)
    @Email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    
    @NotEmpty
    @Index(name="account_name")
    @Length(max=FieldLengths.ACCOUNT_NAME)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Length(max=FieldLengths.ACCOUNT_ADDITIONAL_INFO)
    public String getAdditionalInfo() {
        return additionalInfo;
    }
    public void setAdditionalInfo(String location) {
        this.additionalInfo = location;
    }

    @Range(min=-24, max=24)
    public int getTimezone() {
        return timezone;
    }
    public void setTimezone(int timezone) {
        this.timezone = timezone;
    }

    @NotEmpty
    @Length(min=1, max=10)
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Basic
    public boolean isAdmin() {
        return admin;
    }
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getAdventurePoints() {
        return adventurePoints;
    }
    public void setAdventurePoints(int adventurePoints) {
        this.adventurePoints = adventurePoints;
    }
    
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
}