package restless.realms.server.perk;

import java.util.List;

import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Length;
import org.hibernate.validator.Range;

import restless.realms.server.action.Action;
import restless.realms.server.database.FieldLengths;
import restless.realms.server.effect.Effect;
import restless.realms.server.item.IconInfo;
import restless.realms.server.util.ScaleOptions;

@Entity
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
    @NamedQuery(name="perk.getForSale", query="select p from Perk p where p.msrp > 0 order by minLevel, name", cacheable=true),
})
public class Perk extends Action {
    private String description;
    private int minLevel;
    private int msrp;

	public Perk() {
	}
	
	public Perk(Integer id, String name, int minLevel, int msrp, IconInfo icon, String summary, String description, List<Effect> effects) {
        super(id, name, icon, summary, effects);
        this.description = description;
        this.minLevel = minLevel;
        this.msrp = msrp;
    }

    @Length(max=FieldLengths.ACHIEVEMENT_DESCRIPTION)
	public String getDescription() {
        return description;
    }
	public void setDescription(String description) {
        this.description = description;
    }
	
	@Range(min=1, max=ScaleOptions.MAX_LEVEL)
    public int getMinLevel() {
        return minLevel;
    }
    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }
    
    @Range(min=0, max=1000000)
    public int getMsrp() {
        return msrp;
    }
    public void setMsrp(int msrp) {
        this.msrp = msrp;
    }
}
