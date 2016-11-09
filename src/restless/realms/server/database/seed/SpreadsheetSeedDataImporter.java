package restless.realms.server.database.seed;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import restless.realms.server.database.seed.importer.AccountImporter;
import restless.realms.server.database.seed.importer.AchievementImporter;
import restless.realms.server.database.seed.importer.AdventureImporter;
import restless.realms.server.database.seed.importer.DataImporter;
import restless.realms.server.database.seed.importer.EncounterImporter;
import restless.realms.server.database.seed.importer.ItemImporter;
import restless.realms.server.database.seed.importer.MapImporter;
import restless.realms.server.database.seed.importer.MobImporter;
import restless.realms.server.database.seed.importer.PerkImporter;
import restless.realms.server.database.seed.importer.ProfessionImporter;
import restless.realms.server.database.seed.importer.QuestImporter;
import restless.realms.server.database.seed.importer.ShopImporter;
import restless.realms.server.database.seed.importer.SkillImporter;
import restless.realms.server.database.seed.importer.SkillTreeImporter;
import restless.realms.server.database.seed.importer.TipImporter;
import restless.realms.server.database.seed.importer.TreasureTableImporter;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;

public class SpreadsheetSeedDataImporter {
//    private static final String spreadsheetId = "0AtopThdjiSN1dGphUllGY0RlcjNSNC1nZnVpRmg3Snc";
    private static final String spreadsheetId = "0AizDja-HS6rydFQ0N04zWEotdFVnZE9jLV9YSlhDUUE";
    private static final Log log = LogFactory.getLog(SpreadsheetSeedDataImporter.class);
    private static final SpreadsheetService service = new SpreadsheetService("restless-realms-1");
    private static final String user = "restlessdata@gmail.com";
    private static final String pass = "omgWTFdragonz!";

    private PersistanceProvider persistanceProvider;
    
    public SpreadsheetSeedDataImporter(PersistanceProvider persistanceProvider) {
        this.persistanceProvider = persistanceProvider;
    }
    
    public void apply() {
        List<WorksheetEntry> worksheets = openSpreadsheet();
        for(WorksheetEntry worksheet : worksheets) {
            process(worksheet);
        }
    }
    
    private List<WorksheetEntry> openSpreadsheet() {
        try {
            service.setUserCredentials(user, pass);
    
            URL metafeedUrl = new URL("http://spreadsheets.google.com/feeds/worksheets/" + spreadsheetId + "/private/full");
            WorksheetFeed feed = service.getFeed(metafeedUrl, WorksheetFeed.class);
            return feed.getEntries();
        } catch(ServiceException e) {
            throw new RuntimeException(e);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void process(WorksheetEntry worksheet) {
        String title = worksheet.getTitle().getPlainText();

        DataImporter dataImporter = null;
        if(
                title.equals("Documentation") || 
                title.equals("Legend") ||
                title.equals("Task List") ||
                title.equals("Wish List") ||
                title.equals("Polish") ||
                title.equals("Puzzles") ||
                title.equals("Guide")
        ) {
            // no op
        } else if(title.equals("Items")) {
            dataImporter = new ItemImporter(persistanceProvider);
        } else if(title.equals("Skills")) {
            dataImporter = new SkillImporter(persistanceProvider);
        } else if(title.equals("Perks")) {
            dataImporter = new PerkImporter(persistanceProvider);
        } else if(title.equals("Classes")) {
            dataImporter = new ProfessionImporter(persistanceProvider);
        } else if(title.equals("Skill Trees")) {
            dataImporter = new SkillTreeImporter(persistanceProvider);
        } else if(title.equals("Accounts")) {
            dataImporter = new AccountImporter(persistanceProvider);
        } else if(title.equals("Shops")) {
            dataImporter = new ShopImporter(persistanceProvider);
        } else if(title.equals("Adventures")) {
            dataImporter = new AdventureImporter(persistanceProvider);
        } else if(title.equals("TreasureTables")) {
            dataImporter = new TreasureTableImporter(persistanceProvider);
        } else if(title.equals("Mobs")) {
            dataImporter = new MobImporter(persistanceProvider);
        } else if(title.equals("Encounters")) {
            dataImporter = new EncounterImporter(persistanceProvider);
        } else if(title.equals("Maps")) {
            dataImporter = new MapImporter(persistanceProvider);
        } else if(title.equals("Quests")) {
            dataImporter = new QuestImporter(persistanceProvider);
        } else if(title.equals("Achievements")) {
            dataImporter = new AchievementImporter(persistanceProvider);
        } else if(title.equals("Tips")) {
            dataImporter = new TipImporter(persistanceProvider);
        } else {
            log.warn("Ignoring worksheet \"" + title + "\".");
        }
        if(dataImporter != null) {
            log.debug(title + "...");
            ListFeed listFeed;
            try {
                listFeed = service.getFeed(worksheet.getListFeedUrl(), ListFeed.class);
            } catch(ServiceException e) {
                throw new RuntimeException(e);
            } catch(IOException e) {
                throw new RuntimeException(e);
            }

            int rowCount = 0;
            try {
                for(ListEntry listEntry : listFeed.getEntries()) {
                    rowCount++;
                    CustomElementCollection e = listEntry.getCustomElements();
                    Set<String> tags = e.getTags();
                    String firstVal = e.getValue(tags.iterator().next());
                    if(firstVal != null && firstVal.startsWith("###")) {
                        // comment row
                    } else{ 
                        dataImporter.nextRow(e);
                    }
                }
                dataImporter.complete();
            } catch (Exception e) {
                log.error("Import error for " + title + "-" + rowCount, e);
                persistanceProvider.registerException(title + "-" + rowCount, e);
            }
        }
    }
}