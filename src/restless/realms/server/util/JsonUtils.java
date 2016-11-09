package restless.realms.server.util;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.springframework.web.servlet.ModelAndView;

public class JsonUtils {
    private JsonUtils() {
    }
    
    private static final ObjectMapper jacksonMapper = new ObjectMapper();
    static {
        jacksonMapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
        //jacksonMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        jacksonMapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
    }
    
    public enum ResponseStatus {
        OK, 
        ERROR
    }
    
    public static ModelAndView getModelAndView(Map<String, Object> result) {
        return getModelAndView(ResponseStatus.OK, result);
    }

    public static ModelAndView getModelAndView(ResponseStatus status, Object result) {
        ModelAndView ret = new ModelAndView("json");
        
        StringBuilder json = new StringBuilder();
        json.append("{\"status\":\"");
        json.append(status);
        json.append("\",\"result\":");
        json.append(toString(result));
        json.append("}");
        ret.addObject("json", json.toString());
        return ret;
    }
    
    public static String toString(Object src) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            jacksonMapper.writeValue(os, src);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return os.toString();
    }

    public static JsonNode fromString(String json) {
        try {
            JsonNode node = jacksonMapper.readTree(json);
            return node;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
