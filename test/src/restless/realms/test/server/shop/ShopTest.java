package restless.realms.test.server.shop;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import restless.realms.server.item.Item;
import restless.realms.server.shop.ShopDao;
import restless.realms.test.server.IntegrationTestCase;

public class ShopTest extends IntegrationTestCase {
    @Autowired
    private ShopDao shopDao;
    
    @Test
    public void testCache() {
        StopWatch sw = new StopWatch();
        sw.start();
        List<Item> items = shopDao.get("weaponsarmor").getItemsForProfession("warrior");
        sw.stop();
        
        for(int i = 0; i < 10; i++) {
            sw.start();
            items = shopDao.get("weaponsarmor").getItemsForProfession("warrior");
            Assert.assertTrue(items.size() > 0);
            sw.stop();
            if(i > 0) {
                Assert.assertTrue(sw.getLastTaskTimeMillis() < 10);
            }
        }
    }
    
    @Test
    public void testCacheHit() {
        StopWatch sw = new StopWatch();
        sw.start();
        List<Item> items = shopDao.get("weaponsarmor").getItemsForProfession("warrior");
        Assert.assertTrue(items.size() > 0);
        sw.stop();
        Assert.assertTrue(sw.getLastTaskTimeMillis() < 20);
    }
}