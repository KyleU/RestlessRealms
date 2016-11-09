package restless.realms.server.treasure;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.validator.Length;
import org.hibernate.validator.Range;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasStringId;

@Entity
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TreasureTable implements HasStringId {
    private String id;
    private int minCurrency;
    private int maxCurrency;
    private int minTokens;
    private int maxTokens;
    private List<ItemChance> itemChances;
    
    public TreasureTable() {
        
    }
    
    public TreasureTable(String id, int minCurrency, int maxCurrency, int minTokens, int maxTokens) {
        super();
        this.id = id;
        this.minCurrency = minCurrency;
        this.maxCurrency = maxCurrency;
        this.minTokens = minTokens;
        this.maxTokens = maxTokens;
        itemChances = new ArrayList<ItemChance>();
    }

    @Id
    @Length(max=FieldLengths.STRING_ID)
    @Override
    public String getId() {
        return id;
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Range(min=0)
    public int getMinCurrency() {
        return minCurrency;
    }
    public void setMinCurrency(int minCurrency) {
        this.minCurrency = minCurrency;
    }

    @Range(min=0)
    public int getMaxCurrency() {
        return maxCurrency;
    }
    public void setMaxCurrency(int maxCurrency) {
        this.maxCurrency = maxCurrency;
    }
    
    @Range(min=0)
    public int getMinTokens() {
        return minTokens;
    }
    public void setMinTokens(int minTokens) {
        this.minTokens = minTokens;
    }

    @Range(min=0)
    public int getMaxTokens() {
        return maxTokens;
    }
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    @CollectionOfElements(fetch=FetchType.EAGER)
    @JoinTable(
            joinColumns = @JoinColumn( name="treasureTableId")
    )
    public List<ItemChance> getItemChances() {
        return itemChances;
    }
    public void setItemChances(List<ItemChance> itemChances) {
        this.itemChances = itemChances;
    }
}
