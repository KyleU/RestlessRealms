package restless.realms.test.server.player;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.test.server.IntegrationTestCase;

public class StatisticsTest extends IntegrationTestCase {
	@Autowired
	private StatisticsDao statisticsDao;
	
    @Test
    public void testGet() {
        int amount = statisticsDao.get("Test User", "kill");
        Assert.assertEquals(0, amount);
    }
    
    @Test
    public void testSave() {
        int amount = statisticsDao.increment("Test User", "kill");
        Assert.assertEquals(1, amount);
        amount = statisticsDao.increment("Test User", "kill", 10);
        Assert.assertEquals(11, amount);
        amount = statisticsDao.get("Test User", "kill");
        Assert.assertEquals(11, amount);

    }

    @Test
    public void testTiming() {
        int amount = statisticsDao.increment("Test User", "kill");
        StopWatch sw = new StopWatch();
        sw.start();
        int iterations = 1000;
        for(int i = 0; i < iterations ; i++) {
            amount = statisticsDao.increment("Test User", "kill");
        }
        sw.stop();
        Assert.assertEquals(iterations + 1, amount);
    }
}
