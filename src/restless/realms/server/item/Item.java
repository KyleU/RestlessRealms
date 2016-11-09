package restless.realms.server.item;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Range;

import restless.realms.server.action.Action;
import restless.realms.server.database.FieldLengths;
import restless.realms.server.effect.Effect;
import restless.realms.server.util.ScaleOptions;

@Entity
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
    @NamedQuery(name="item.getByName", query="select i from Item i where i.name = ?")
})
public class Item extends Action {
    private ItemType type;
    private int minLevel;
    private String requiredProfession;
    private int msrp;
    private int rarity;

	public Item() {
	}
	
    public Item(Integer id, String name, ItemType type, int minLevel, String requiredProfession, int msrp, int rarity, IconInfo icon, String summary, List<Effect> effects) {
        super(id, name, icon, summary, effects);
        this.type = type;
        this.minLevel = minLevel;
        this.requiredProfession = requiredProfession;
        this.msrp = msrp;
        this.rarity = rarity;
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length=FieldLengths.ENUM_LENGTH)
    public ItemType getType() {
		return type;
	}
	public void setType(ItemType type) {
		this.type = type;
	}
	
    @Range(min=0, max=4)
	public int getRarity() {
        return rarity;
    }
	public void setRarity(int rarity) {
        this.rarity = rarity;
    }

	@Range(min=1, max=ScaleOptions.MAX_LEVEL)
    public int getMinLevel() {
        return minLevel;
    }
    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }
    
    @Basic
    public String getRequiredProfession() {
        return requiredProfession;
    }
    public void setRequiredProfession(String requiredProfession) {
        this.requiredProfession = requiredProfession;
    }
    
    @Range(min=0, max=10000000)
    public int getMsrp() {
        return msrp;
    }
    public void setMsrp(int msrp) {
        this.msrp = msrp;
    }
}
