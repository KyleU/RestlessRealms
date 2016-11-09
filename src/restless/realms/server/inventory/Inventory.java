package restless.realms.server.inventory;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Length;
import org.hibernate.validator.Range;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.item.Item;
import restless.realms.server.util.HasPlayerNameId;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate=true)
@NamedQueries({
    @NamedQuery(name="inventory.getCurrency", query="select i.currency from Inventory i where i.playerName = ?"),
    @NamedQuery(name="inventory.getTokens", query="select i.tokens from Inventory i where i.playerName = ?"),
    @NamedQuery(name="leaderboard.getByGold", query="select i.playerName as name, i.currency as value from Inventory i order by i.currency desc"),
})
public class Inventory implements HasPlayerNameId {
	private String playerName;
    private int currency;
    private int tokens;
		
	private List<Item> items;
	
	public Inventory() {
	}


    public Inventory(String playerName, int currency, List<Item> items) {
        this.playerName = playerName;
        this.currency = currency;
        this.items = items;
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

    @Range(min=0)
    public int getCurrency() {
        return currency;
    }
    public void setCurrency(int currency) {
        this.currency = currency;
    }

    @Range(min=0)
    public int getTokens() {
        return tokens;
    }
    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

	@ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(
        joinColumns=@JoinColumn(name="inventoryPlayerName"),
        inverseJoinColumns=@JoinColumn(name="itemId")
    )
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @IndexColumn(name="orderIndex")
	public List<Item> getItems() {
		return items;
	}
	public void setItems(List<Item> items) {
		this.items = items;
	}
}