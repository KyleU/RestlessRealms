package restless.realms.server.database.seed.importer;

import java.util.ArrayList;
import java.util.List;

import restless.realms.server.database.seed.PersistanceProvider;
import restless.realms.server.item.Item;
import restless.realms.server.shop.Shop;

import com.google.common.base.Splitter;
import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class ShopImporter extends DataImporter {
    public ShopImporter(PersistanceProvider persistanceProvider) {
        super(persistanceProvider);
    }
    
    @Override
    public void nextRow(CustomElementCollection e) {
        List<Item> shopItems = new ArrayList<Item>();
        Iterable<String> itemIds = Splitter.on(",").trimResults().split(e.getValue("items"));
        for(String itemId : itemIds) {
            Item item = persistanceProvider.getObject(Item.class, Integer.parseInt(itemId));
            if(item == null) {
                throw new IllegalArgumentException("Shop " + e.getValue("id") + " references missing item " + itemId + ".");
            }
            shopItems.add(item);
        }
        Shop s = new Shop(e.getValue("id"), e.getValue("name"), shopItems);
        persistanceProvider.save(s.getId(), s);
    }
}
