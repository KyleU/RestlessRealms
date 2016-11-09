package restless.realms.server.database.seed.importer;

import restless.realms.server.database.seed.PersistanceProvider;
import restless.realms.server.tip.Tip;
import restless.realms.server.util.ScaleOptions;

import com.google.gdata.data.spreadsheet.CustomElementCollection;

public class TipImporter extends DataImporter {
    public TipImporter(PersistanceProvider persistanceProvider) {
        super(persistanceProvider);
    }

    @Override
    public void nextRow(CustomElementCollection e) {
        Tip t = new Tip(
            getInt(e, "id"), 
            e.getValue("content"),
            getInt(e.getValue("minLevel"), 0),
            getInt(e.getValue("maxLevel"), ScaleOptions.MAX_LEVEL)
        );
        persistanceProvider.save(t.getId(), t);
    }
}
