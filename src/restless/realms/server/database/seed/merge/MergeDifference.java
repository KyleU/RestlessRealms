package restless.realms.server.database.seed.merge;

import java.io.Serializable;

public class MergeDifference {
    private final Class<?> type;
    private final Serializable id;
    private final String propertyName;
    
    private final Object currentValue;
    private final Object newValue;
    
    public MergeDifference(Class<?> type, Serializable id, String propertyName, Object currentValue, Object newValue) {
        super();
        this.type = type;
        this.id = id;
        this.propertyName = propertyName;
        
        this.currentValue = currentValue;
        this.newValue = newValue;
    }

    public Class<?> getType() {
        return type;
    }

    public Serializable getId() {
        return id;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Object getCurrentValue() {
        return currentValue;
    }

    public Object getNewValue() {
        return newValue;
    }
}
