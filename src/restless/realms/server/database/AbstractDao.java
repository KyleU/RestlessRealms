package restless.realms.server.database;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;

import restless.realms.server.database.seed.SeedDataManager;
import restless.realms.server.exception.TooManyResultsException;


public abstract class AbstractDao<T> {
    @Autowired
    protected HibernateTemplate template;
    
    @Autowired
    //exists only to handle dependency ordering
    protected SeedDataManager seedDataManager;

    
    protected abstract Class<?> getManagedClass();
    
    @SuppressWarnings("unchecked")
    protected T get(Serializable id) {
        return (T)template.get(getManagedClass(), id);
    }
    
    @SuppressWarnings("unchecked")
    protected T uniqueResult(List<?> list) {
        if(list.size() == 0) {
            return null;
        }
        if(list.size() > 1) {
            throw new TooManyResultsException();
        }
        return (T)list.get(0);
    }
}
