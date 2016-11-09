package restless.realms.server.action;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Valid;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.effect.Effect;
import restless.realms.server.item.IconInfo;
import restless.realms.server.util.HasIntegerId;

@MappedSuperclass
public abstract class Action implements HasIntegerId {
    private Integer id;
    private String name;
    private IconInfo icon;
    private String summary;
    private List<Effect> effects;

    public Action() {
    }

    public Action(Integer id, String name, IconInfo icon, String summary, List<Effect> effects) {
        super();
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.summary = summary;
        this.effects = effects;
    }
    
    @Id 
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @NotEmpty
    @Length(max=FieldLengths.ITEM_NAME)
    @Column(unique=true)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    
    @Embedded
    public IconInfo getIcon() {
        return icon;
    }
    public void setIcon(IconInfo icon) {
        this.icon = icon;
    }

    @Length(max=FieldLengths.SUMMARY)
    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable
    @Valid
    @IndexColumn(name="orderIndex")
    public List<Effect> getEffects() {
        return effects;
    }
    public void setEffects(List<Effect> effects) {
        this.effects = effects;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Action other = (Action)obj;
        if(id == null) {
            if(other.id != null)
                return false;
        } else if(!id.equals(other.id))
            return false;
        if(name == null) {
            if(other.name != null)
                return false;
        } else if(!name.equals(other.name))
            return false;
        return true;
    }
}
