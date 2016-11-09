package restless.realms.server.database.seed;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;

import restless.realms.server.item.Item;
import restless.realms.server.mob.MobArchetype;
import restless.realms.server.skill.Skill;

public class SeedDataValidator implements PersistanceProvider {
    private static final Log log = LogFactory.getLog(SeedDataValidator.class);
    
    private Map<Class<?>, Map<Serializable, Object>> uberCache;
    private Map<Class<?>, Set<Serializable>> referenceCache;
    Map<String, Exception> exceptions;
    private Map<String, Set<Serializable>> uniqueFieldValues;
    
    public SeedDataValidator() {
    }
    
    public void validate() {
        this.uberCache = new LinkedHashMap<Class<?>, Map<Serializable,Object>>();
        this.referenceCache = new LinkedHashMap<Class<?>, Set<Serializable>>();
        this.exceptions = new LinkedHashMap<String, Exception>();
        this.uniqueFieldValues = new HashMap<String, Set<Serializable>>();
        
        SpreadsheetSeedDataImporter importer = new SpreadsheetSeedDataImporter(this);
        importer.apply();
    }
    
    public void debugResults() {
        for(Class<?> c : uberCache.keySet()) {
            log.debug(c.getSimpleName() + ":");
            Map<Serializable, Object> cache = uberCache.get(c);
            log.debug(cache.entrySet().size());
        }
        
        log.debug("Unreferenced Items: " + getUnreferencedIds(Item.class));
        log.debug("Unreferenced Skills: " + getUnreferencedIds(Skill.class));
        log.debug("Unreferenced Mobs: " + getUnreferencedIds(MobArchetype.class));
        
        if(exceptions != null) {
            for(Entry<String, Exception> error : exceptions.entrySet()) {
                log.debug(error.getKey() + ": " + error.getValue().toString());
            }
        }
    }
    
    public static void main(String[] args) {
        SeedDataValidator validator = new SeedDataValidator();
        validator.validate();
        validator.debugResults();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(Class<?> entityClass, Serializable id) {
        T ret = null;
        Map<Serializable, Object> map = uberCache.get(entityClass);
        if(map != null) {
            ret = (T)map.get(id);
        }
        if(ret == null) {
            throw new IllegalArgumentException("Unable to find referenced " + entityClass.getSimpleName() + " \"" + id + "\".");
        }
        if(!referenceCache.containsKey(entityClass)) {
            referenceCache.put(entityClass, new HashSet<Serializable>());
        }
        referenceCache.get(entityClass).add(id);
        return ret;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void save(Serializable id, Object o) {
        if(!uberCache.containsKey(o.getClass())) {
            uberCache.put(o.getClass(), new LinkedHashMap<Serializable, Object>());
        }
        if(uberCache.get(o.getClass()).containsKey(id)) {
            throw new IllegalArgumentException("Duplicate id \"" + id + "\" for type " + o.getClass().getSimpleName() + ".");
        }
        
        @SuppressWarnings("rawtypes")
        ClassValidator validator = new ClassValidator(o.getClass());
        
        InvalidValue[] invalidValues = validator.getInvalidValues(o);
        if(invalidValues.length != 0) {
            StringBuilder ret = new StringBuilder();
            for(InvalidValue invalidValue : invalidValues) {
                ret.append("Invalid value \"");
                ret.append(invalidValue.getValue());
                ret.append("\" for property \"");
                ret.append(invalidValue.getPropertyName());
                ret.append("\" (" + invalidValue.getMessage() + "). ");
            }
            throw new IllegalStateException(ret.toString());
        }
        
        uberCache.get(o.getClass()).put(id, o);
    }

    @Override
    public void verifyUnique(String key, Serializable value) {
        if(!uniqueFieldValues.containsKey(key)) {
            uniqueFieldValues.put(key, new HashSet<Serializable>());
        }
        if(uniqueFieldValues.get(key).contains(value)) {
            throw new IllegalArgumentException(key + " \"" + value + "\" must be unique.");
        }
        uniqueFieldValues.get(key).add(value);
    }
    
    @Override
    public void registerException(String key, Exception e) {
        exceptions.put(key, e);
    }

    public Map<String, Exception> getExceptions() {
        return exceptions;
    }
    
    public Map<Class<?>, Map<Serializable, Object>> getUberCache() {
        return uberCache;
    }
    
    public Set<Serializable> getUnreferencedIds(Class<?> entityClass) {
        Set<Serializable> unreferenced = new LinkedHashSet<Serializable>();
        Set<Serializable> referenced = referenceCache.get(entityClass);
        if(referenced == null) {
            referenced = new HashSet<Serializable>();
        }
        if(uberCache.containsKey(entityClass)) {
            for(Serializable id : uberCache.get(entityClass).keySet()) {
                if(!referenced.contains(id)) {
                    unreferenced.add(id);
                }
            }
        }
        return unreferenced;
    }
}
