package restless.realms.server.cache;

import java.io.InputStream;
import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.Timestamper;

// Hack to let Spring and Hibernate share a cache.
@SuppressWarnings("deprecation")
public class EhCacheProvider implements CacheProvider {
    private static CacheManager cacheManager = null;

    public static void setCacheManager(CacheManager cacheManager) {
        EhCacheProvider.cacheManager = cacheManager;
    }

    public Cache buildCache(String name, Properties properties) throws CacheException {
        try {
            if(cacheManager == null) {
                String path = "restless/realms/server/configuration/ehcache.xml";
                InputStream is = getClass().getClassLoader().getResourceAsStream(path);
                if(is == null) {
                    throw new IllegalArgumentException("No file found in classpath:" + path);
                }
                setCacheManager(new CacheManager(is));
            }
            net.sf.ehcache.Ehcache cache = cacheManager.getEhcache(name);
            if (cache == null) {
                throw new IllegalArgumentException("No cache with name \"" + name + "\".");
            }
            return new net.sf.ehcache.hibernate.EhCache(cache);
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(e);
        }
    }

    public long nextTimestamp() {
        return Timestamper.next();
    }

    public void start(Properties properties) throws CacheException {
        // no op
    }

    public void stop() {
        // no op
    }

    public boolean isMinimalPutsEnabledByDefault() {
        return false;
    }
}

