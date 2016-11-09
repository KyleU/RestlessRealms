package restless.realms.server.database.seed.importer;

import restless.realms.server.adventure.AdventureArchetype;
import restless.realms.server.database.seed.PersistanceProvider;

import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class AdventureImporter extends DataImporter {
    public AdventureImporter(PersistanceProvider persistanceProvider) {
        super(persistanceProvider);
    }
    
    @Override
    public void nextRow(CustomElementCollection e) {
        AdventureArchetype a = new AdventureArchetype(
                e.getValue("id"), 
                e.getValue("name"),
                getInt(e.getValue("minLevel"), 0),
                getInt(e.getValue("maxLevel"), 0),
                e.getValue("mapCode"),
                getInt(e, "mapX"),
                getInt(e, "mapY"),
                getInt(e, "mapWidth"),
                getInt(e, "mapHeight"),
                e.getValue("description")
        );
        persistanceProvider.save(a.getId(), a);
    }
}
