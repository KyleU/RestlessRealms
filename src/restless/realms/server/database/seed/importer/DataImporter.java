package restless.realms.server.database.seed.importer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import restless.realms.server.database.seed.PersistanceProvider;

import com.google.gdata.data.spreadsheet.CustomElementCollection;

public abstract class DataImporter {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("M/d/yyyy HH:mm:ss");
    
    protected PersistanceProvider persistanceProvider;

    public DataImporter(PersistanceProvider persistanceProvider) {
        this.persistanceProvider = persistanceProvider;
    }
    
    public abstract void nextRow(CustomElementCollection e);
    
    public void complete() {
        
    }
    
    protected boolean getBoolean(String value) {
        return Boolean.parseBoolean(value);
    }

    protected int getInt(String value, int defaultValue) {
        if(value == null) {
            value = Integer.toString(defaultValue);
        }
        int ret = Integer.parseInt(value.trim());
        return ret ;
    }

    protected int getInt(CustomElementCollection e, String fieldName) {
        String value = e.getValue(fieldName);
        if(value == null) {
            throw new RuntimeException("Required int value for \"" + fieldName + "\" is null.");
        }
        int ret = Integer.parseInt(value.trim());
        return ret ;
    }

    protected long getLong(CustomElementCollection e, String fieldName) {
        String value = e.getValue(fieldName);
        if(value == null) {
            throw new RuntimeException("Required long value for \"" + fieldName + "\" is null.");
        }
        long ret = Long.parseLong(value.trim());
        return ret ;
    }

    protected Date getDate(String value) {
        try {
            return dateFormat.parse(value);
        } catch(ParseException e) {
            throw new RuntimeException(e);
        }
    }

    protected Date getDateTime(String value) {
        try {
            return dateTimeFormat.parse(value);
        } catch(ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
