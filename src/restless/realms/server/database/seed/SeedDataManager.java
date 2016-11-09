package restless.realms.server.database.seed;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SeedDataManager implements PersistanceProvider {
    private static final Log log = LogFactory.getLog(SeedDataManager.class);
    
    @Autowired
    private HibernateTemplate hibernateTemplate;
    
    public SeedDataManager() {
    }
    
    @PostConstruct
    public void applyIfNeeded() {
        if(isSeedDataNeeded()) {
            log.debug("Applying seed data.");
            new SpreadsheetSeedDataImporter(this).apply();
            new StaticSeedDataImporter(this).apply();
        }
    }

    public void reload() {
        //sell all inventory
        //wipe non-account stuff
        //load everything
        //evict caches
        //reload archetypes
    }
    
    private boolean isSeedDataNeeded() {
        List<?> accounts = hibernateTemplate.find("from Account where email = ?", "kyle@restlessinteractive.com");
        return accounts.size() == 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(Class<?> entityClass, Serializable id) {
        T ret = (T)hibernateTemplate.get(entityClass, id);
        if(ret == null) {
            throw new IllegalArgumentException("Unable to find referenced " + entityClass.getSimpleName() + " \"" + id + "\".");
        }
        return ret;
    }

    @Override
    public void save(Serializable id, Object o) {
        try {
            hibernateTemplate.save(o);
        } catch(InvalidStateException e) {
            StringBuilder message = new StringBuilder("Invalid values:");
            for(InvalidValue invalidValue : e.getInvalidValues()) {
                message.append(invalidValue.getMessage());
            }
            message.append(".");
            throw new IllegalStateException(message.toString(), e);
        }
    }
    
    @Override
    public void verifyUnique(String key, Serializable value) {
        // no op, db handles it
    }

    @Override
    public void registerException(String key, Exception e) {
        log.error("Unable to import \"" + key + "\".", e);
        throw new RuntimeException(e);
    }
}
