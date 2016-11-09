package restless.realms.server.database.seed;

import java.io.Serializable;

public interface PersistanceProvider {
    public <T> T getObject(Class<?> entityClass, Serializable id);
    public void save(Serializable id, Object o);
    public void verifyUnique(String key, Serializable value);
    public void registerException(String key, Exception e);
}
