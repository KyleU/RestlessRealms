package restless.realms.server.treasure;

import javax.persistence.Basic;
import javax.persistence.Embeddable;

import org.hibernate.validator.Range;

@Embeddable
public class ItemChance {
    private Integer itemId;
    private int tenthPercentChance;
    
    public ItemChance() {
    }

    public ItemChance(Integer itemId, int tenthPercentChance) {
        super();
        this.itemId = itemId;
        this.tenthPercentChance = tenthPercentChance;
    }

    @Basic
    public Integer getItemId() {
        return itemId;
    }
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    @Range(min=1, max=1000)
    public int getTenthPercentChance() {
        return tenthPercentChance;
    }
    public void setTenthPercentChance(int percentChance) {
        this.tenthPercentChance = percentChance;
    }
}
