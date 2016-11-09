package restless.realms.server.stash;

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
import org.hibernate.validator.Range;

import restless.realms.server.item.Item;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate=true)
public class Stash {
	public static final int NUM_ITEMS = 50;
	
    private Integer accountId;
    private int currency;
		
	private List<Item> items;
	
	public Stash() {
	}

    @Id
	public Integer getAccountId() {
		return accountId;
	}
	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

    @Range(min=0)
    public int getCurrency() {
        return currency;
    }
    public void setCurrency(int currency) {
        this.currency = currency;
    }

	@ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
        joinColumns=@JoinColumn(name="stashAccountId"),
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