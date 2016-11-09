package restless.realms.server.treasure;

import java.util.List;

import restless.realms.server.item.Item;

public class Treasure {
    private int currency;
    private int tokens;
    private List<Item> items;
    
    public Treasure() {
    }
    
    public Treasure(int currency, List<Item> items) {
        super();
        this.currency = currency;
        this.items = items;
    }

    public int getCurrency() {
        return currency;
    }
    public void setCurrency(int currency) {
        this.currency = currency;
    }

    public int getTokens() {
        return tokens;
    }
    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public List<Item> getItems() {
        return items;
    }
    public void setItems(List<Item> items) {
        this.items = items;
    }
}