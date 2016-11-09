package restless.realms.server.database.seed.importer;

import restless.realms.server.adventure.map.AdventureMap;
import restless.realms.server.database.seed.PersistanceProvider;
import restless.realms.server.encounter.Encounter;
import restless.realms.server.treasure.TreasureTable;

import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class MapImporter extends DataImporter {
    private String adventureArchetype;
    private StringBuilder sb;
    private int i = 0;

    public MapImporter(PersistanceProvider persistanceProvider) {
        super(persistanceProvider);
        adventureArchetype = null;
        sb = new StringBuilder();
    }

    @Override
    public void nextRow(CustomElementCollection e) {
        if(e.getValue("adventure") != null) {
            if(adventureArchetype != null) {
                saveMap(adventureArchetype, sb.toString());
                sb = new StringBuilder();
            }
            adventureArchetype = e.getValue("adventure"); 
        }
        
        if(e.getValue("maprow") != null) {
            if(!e.getValue("mapRow").startsWith("-")) {
                sb.append(e.getValue("mapRow") + "/");
            }
        }
    }

    @Override
    public void complete() {
        saveMap(adventureArchetype, sb.toString());
    }

    private void saveMap(String adventure, String serializedForm) {
        boolean hasIntroRoom = false;
        boolean hasExitRoom = false;
        for(int i = 0; i < serializedForm.length(); i++) {
            char code = serializedForm.charAt(i);
            if(code >= 'a' && code <= 'z') {
                persistanceProvider.getObject(Encounter.class, adventure + "-" + code);
            } else if(code >= '1' && code <= '9') {
                persistanceProvider.getObject(TreasureTable.class, adventure + "-" + code);
            } else if(code == '!') {
                if(hasIntroRoom) {
                    throw new IllegalStateException("Multiple intro rooms in " + adventure + " map \"" + serializedForm + "\".");
                }
                hasIntroRoom = true;
            } else if(code == '?') {
                //puzzle
            } else if(code == '0') {
                //empty
            } else if(code == '%') {
                //shrine
            } else if(code == '.') {
                //nothing
            } else if(code == '/') {
                //line separator
            } else if(code == '#') {
                if(hasExitRoom) {
                    throw new IllegalStateException("Multiple exit rooms in " + adventure + " map \"" + serializedForm + "\".");
                }
                hasExitRoom = true;
            } else {
                throw new IllegalStateException("Unknown room code \"" + code + "\".");
            }
        }
        if(!hasIntroRoom) {
            throw new IllegalStateException("No introduction room in " + adventure + " map \"" + serializedForm + "\".");
        }
        if(!hasExitRoom) {
            throw new IllegalStateException("No exit room in " + adventure + " map \"" + serializedForm + "\".");
        }
        
        AdventureMap map = new AdventureMap();
        map.setAdventure(adventure);
        map.setSerializedForm(serializedForm);
        i++;
        map.setId(i);
        persistanceProvider.save(i, map);
    }
}
