package restless.realms.client;

import java.util.Map;

import restless.realms.client.util.ServiceCallback;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.URL;

public class ServiceManager {
    private static final String REQUEST_SESSION_HEADER_KEY = "X-Restless-Realms-Session";

    public static void call(String service, String method, ServiceCallback callback) {
        call(service, method, (String)null, callback);
    }

    public static void call(String service, String method, ServiceCallback callback, Map<String, String> params) {
        call(service, method, getRequestData(params), callback);
    }

    public static void call(String service, String method, ServiceCallback callback, Object... paramKeysAndValues) {
        call(service, method, getRequestData(paramKeysAndValues), callback);
    }

    private static void call(String service, String method, String requestData, ServiceCallback callback) {
        String url = "/" + service + "/" + method + ".json";
        RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, url);
        //rb.setTimeoutMillis(20000);
        rb.setHeader("Content-Type", "application/x-www-form-urlencoded");
        rb.setHeader("Accept", "application/json");
        rb.setHeader(REQUEST_SESSION_HEADER_KEY, ClientState.getSessionId());
        
        if(requestData != null) {
            rb.setRequestData(requestData);
        }
        rb.setCallback(callback);
        try {
            rb.send();
        } catch(RequestException e) {
            callback.onError(null, e);
        }
    }
    
    private static String getRequestData(Map<String, String> params) {
        StringBuffer sb = new StringBuffer();
        int i = 0;
        for(String key : params.keySet()) {
            if(i > 0) {
                sb.append("&");
            }
            i++;
            String encodedName = URL.encodeComponent(key);
            sb.append(encodedName);

            sb.append("=");

            String encodedValue = URL.encodeComponent(params.get(key));
            sb.append(encodedValue);
        }
        return sb.toString();
    }    

    private static String getRequestData(Object[] keysAndValues) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < keysAndValues.length; i+=2) {
            if(i > 0) {
                sb.append("&");
            }
            String encodedName = URL.encodeComponent((String)keysAndValues[i]);
            sb.append(encodedName);

            sb.append("=");

            String encodedValue = URL.encodeComponent(keysAndValues[i+1].toString());
            sb.append(encodedValue);
        }
        return sb.toString();
    }
}
