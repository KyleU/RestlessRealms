package restless.realms.server.database.seed.importer;

import restless.realms.server.database.seed.PersistanceProvider;
import restless.realms.server.item.Item;
import restless.realms.server.treasure.ItemChance;
import restless.realms.server.treasure.TreasureTable;
import restless.realms.server.util.ScaleOptions;

import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class TreasureTableImporter extends DataImporter {
    private TreasureTable treasureTable = null;
    
    public TreasureTableImporter(PersistanceProvider persistanceProvider) {
        super(persistanceProvider);
    }
    
    @Override
    public void nextRow(CustomElementCollection e) {
        if(e.getValue("id") != null) {
            if(treasureTable != null) {
                persistanceProvider.save(treasureTable.getId(), treasureTable);
            }
            treasureTable = new TreasureTable(
                    e.getValue("id"),
                    getInt(e.getValue("minCurrency"), 0) * ScaleOptions.ECONOMY,
                    getInt(e.getValue("maxCurrency"), 0) * ScaleOptions.ECONOMY,
                    getInt(e.getValue("minTokens"), 0),
                    getInt(e.getValue("maxTokens"), 0)
            );
        }
        if(e.getValue("itemId") != null && e.getValue("itemId").trim().length() > 0) {
            Item i = persistanceProvider.getObject(Item.class, getInt(e, "itemId"));
            String chance = e.getValue("itemChance");
            if(chance == null) {
                chance = "100";
            }
            int tenthPercentChance = (int)(Double.parseDouble(chance) * 10);
            ItemChance itemChance = new ItemChance(
                    i.getId(),
                    tenthPercentChance
            );
            treasureTable.getItemChances().add(itemChance);
        }
    }
    
    @Override
    public void complete() {
        persistanceProvider.save(treasureTable.getId(), treasureTable);
    }
}
