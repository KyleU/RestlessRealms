package restless.realms.server.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.item.Item;
import restless.realms.server.util.HasStringId;

@Entity
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Shop implements HasStringId {
    private String id;
    private String name;
    private List<Item> items;
    private Map<String, List<Item>> itemsForProfession;
    
    public Shop() {
        itemsForProfession = new HashMap<String, List<Item>>();
    }

    public Shop(String id, String name, List<Item> items) {
        this.id = id;
        this.name = name;
        this.items = items;
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
    
    @NotEmpty
    @Length(max=FieldLengths.SHOP_NAME)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
        joinColumns=@JoinColumn(name="shopId"),
        inverseJoinColumns=@JoinColumn(name="itemId")
    )
    @IndexColumn(name="orderIndex")
    public List<Item> getItems() {
        return items;
    }
    public void setItems(List<Item> items) {
        this.items = items;
    }
    
    public List<Item> getItemsForProfession(String professionId) {
        if(!itemsForProfession.containsKey(professionId)) {
            List<Item> ret = new ArrayList<Item>();
            for(Item item : items) {
                if(item.getRequiredProfession() == null || item.getRequiredProfession().equals(professionId)) {
                    ret.add(item);
                }
            }
            itemsForProfession.put(professionId, ret);
        }
        return itemsForProfession.get(professionId);
    }
}