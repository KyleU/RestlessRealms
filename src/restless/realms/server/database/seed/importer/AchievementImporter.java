package restless.realms.server.database.seed.importer;

import restless.realms.server.achievement.Achievement;
import restless.realms.server.database.seed.PersistanceProvider;
import restless.realms.server.item.IconInfo;

import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class AchievementImporter extends DataImporter {
    public AchievementImporter(PersistanceProvider persistanceProvider) {
        super(persistanceProvider);
    }
    
    @Override
    public void nextRow(CustomElementCollection e) {
        String stat = e.getValue("stat");
        
        IconInfo icon = null;
        try {
            icon = new IconInfo(getInt(e.getValue("iconCol"), 0), getInt(e.getValue("iconRow"), 0)); 
        } catch(Exception e2) {
            throw new RuntimeException(e.getValue("name"), e2);
        }
        
        Achievement a = new Achievement(
                e.getValue("id"), 
                e.getValue("name"),
                e.getValue("description"),
                getInt(e, "pointValue"),
                stat,
                getInt(e, "quantity"),
                icon,
                getInt(e, "orderIndex")
        );
        persistanceProvider.save(a.getId(), a);
        persistanceProvider.verifyUnique("AchievementName", a.getName());
    }
}
