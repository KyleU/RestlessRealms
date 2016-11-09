package restless.realms.server.treasure;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.item.Item;
import restless.realms.server.item.ItemDao;
import restless.realms.server.util.RandomUtils;

@Repository
public class TreasureDao extends AbstractDao<TreasureTable> {
    @Autowired
    private ItemDao itemDao;
    
    public TreasureTable getTable(String id) {
        return get(id);
    }
    
    public Treasure create(String treasureTableId) {
        TreasureTable table = get(treasureTableId);
        return generateTreasure(table);
    }
    
    private Treasure generateTreasure(TreasureTable table) {
        Treasure t = new Treasure();
        int currency = RandomUtils.getInt(table.getMinCurrency(), table.getMaxCurrency());
        t.setCurrency(currency);
        int tokens = RandomUtils.getInt(table.getMinTokens(), table.getMaxTokens());
        t.setTokens(tokens);
        List<Item> items = new ArrayList<Item>();
        for(ItemChance chance : table.getItemChances()) {
            if(RandomUtils.tenthPercentageCheck(chance.getTenthPercentChance())) {
                items.add(itemDao.get(chance.getItemId()));
            }
        }
        t.setItems(items);
        return t;
    }

    @Override
    protected Class<?> getManagedClass() {
        return TreasureTable.class;
    }
}
