package restless.realms.test.server.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.springframework.util.StopWatch;

import restless.realms.server.util.JsonUtils;

public class JsonSerializationBenchmark {
    private static final Log log = LogFactory.getLog(JsonSerializationBenchmark.class);
    
    @Test
    public void testGsonPerformance() {
        Map<String, Object> testMap = new LinkedHashMap<String, Object>();
        for(int i = 0; i < 1000; i++) {
            testMap.put("testKey" + i, i + "!");
        }
        
        StopWatch sw = new StopWatch();
        for(int i = 0; i < 1000; i++) {
            if(i == 1) {
                sw.start();
            }
            String json = JsonUtils.toString(testMap);
            Assert.assertNotNull(json);
        }
        sw.stop();
        
        log.info("GSON Timing: " + sw.getLastTaskTimeMillis() + "ms.");
    }

    @Test
    public void testJacksonPerformance() {
        Map<String, Object> testMap = new LinkedHashMap<String, Object>();
        for(int i = 0; i < 1000; i++) {
            testMap.put("testKey" + i, i + "!");
        }
        
        ObjectMapper mapper = new ObjectMapper();
        
        StopWatch sw = new StopWatch();
        
        for(int i = 0; i < 1000; i++) {
            if(i == 1) {
                sw.start();
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                mapper.writeValue(os, testMap);
            } catch(JsonGenerationException e) {
                e.printStackTrace();
            } catch(JsonMappingException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
            os.toString();
        }
        sw.stop();
        
        log.info("Jackson Timing: " + sw.getLastTaskTimeMillis() + "ms.");
    }
}
