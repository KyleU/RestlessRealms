package restless.realms.server.facebook;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import restless.realms.server.web.RequestUtils;

import com.google.code.facebookapi.FacebookException;
import com.google.code.facebookapi.FacebookJsonRestClient;
import com.google.code.facebookapi.ProfileField;

public class FacebookUtils {
    private static final Log log = LogFactory.getLog(FacebookUtils.class);
    public static final String FACEBOOK_API_KEY = "6200f4c07b6ef91fa2c89856aeefd6e5";
    public static final String FACEBOOK_SECRET_KEY = "16e914ba16f8063e8f7a38a063994845";

    private static final List<ProfileField> fields = new ArrayList<ProfileField>();
    static {
        fields.add(ProfileField.FIRST_NAME);
        fields.add(ProfileField.LAST_NAME);
        fields.add(ProfileField.NAME);
        fields.add(ProfileField.LOCALE);
        fields.add(ProfileField.BIRTHDAY);
        fields.add(ProfileField.ABOUT_ME);
        fields.add(ProfileField.ACTIVITIES);
        fields.add(ProfileField.AFFILIATIONS);
        fields.add(ProfileField.PIC_BIG);
        fields.add(ProfileField.PIC_SMALL);
        fields.add(ProfileField.PIC_SQUARE);
        fields.add(ProfileField.PIC_SQUARE_WITH_LOGO);
        fields.add(ProfileField.EMAIL_HASHES);
        fields.add(ProfileField.SIGNIFICANT_OTHER_ID);
        fields.add(ProfileField.RELATIONSHIP_STATUS);
        fields.add(ProfileField.RELIGION);
        fields.add(ProfileField.SEX);
        fields.add(ProfileField.STATUS);
        fields.add(ProfileField.TIMEZONE);
        fields.add(ProfileField.UID);
        fields.add(ProfileField.WALL_COUNT);
        fields.add(ProfileField.WORK_HISTORY);
        fields.add(ProfileField.PROXIED_EMAIL);
        fields.add(ProfileField.ONLINE_PRESENCE);
        fields.add(ProfileField.POLITICAL);
        fields.add(ProfileField.CURRENT_LOCATION);
        fields.add(ProfileField.HOMETOWN_LOCATION);
    }
    
    public static Long getFacebookConnectUserId(HttpServletRequest request) {
        FacebookJsonRestClient facebookClient = getFacebookConnectClient(request);
        Long ret = null;
        if(facebookClient != null) {
            try {
                ret = facebookClient.users_getLoggedInUser();
            } catch(FacebookException e) {
                log.error("Facebook Exception: " + e.getMessage());
            }
        }
        return ret;
    }

    public static Map<String, Object> getUserInfo(FacebookJsonRestClient client, long userId) {
        Map<String, Object> ret = new LinkedHashMap<String, Object>();

        ret.put("apikey", client.getApiKey());
        ret.put("uid", userId);
        
        List<Long> userIds = new ArrayList<Long>();
        JSONArray ja = null;
        try {
            userIds.add(userId);
            ja = (JSONArray)client.users_getInfo(userIds, fields);
        } catch(FacebookException e) {
            log.error("Error communicating with Facebook.", e);
        }
        try {
            JSONObject jo = null;
            if(ja != null) {
                jo = ja.getJSONObject(0);
            }
            
            if(jo != null) {
                for(String name : JSONObject.getNames(jo)) {
                    Object value = jo.get(name);
                    ret.put(name, value);
                }
            }
        } catch (JSONException e) {
            log.error("Error getting facebook info.", e);
        }
        
        return ret;
    }

    public static FacebookJsonRestClient getFacebookConnectClient(HttpServletRequest request) {
        String facebookSessionKey = RequestUtils.getCookieValue(FACEBOOK_API_KEY + "_session_key", request);
        if(facebookSessionKey == null) {
            return null;
        }
        FacebookJsonRestClient facebookClient = new FacebookJsonRestClient(FACEBOOK_API_KEY, FACEBOOK_SECRET_KEY, facebookSessionKey);

        return facebookClient;
    }
}
