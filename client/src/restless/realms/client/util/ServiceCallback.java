package restless.realms.client.util;

import restless.realms.client.console.ConsoleUtils;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

public abstract class ServiceCallback implements RequestCallback {
    public static boolean debug = false;
    
    public abstract void onSuccess(ScriptObject result);

    public final void onFailure(ScriptObject result) {
        String code = result.hasKey("code") ? result.get("code") : "error";
        String message = result.hasKey("message") ? result.get("message") : code;
        if("NotSignedInException".equals(code)) {
            Cookies.setCookie("rrsession", "");
            Cookies.removeCookie("rrsession", "/");
            Window.Location.assign("/index.html?error=" + message);
        } else {
            onFailure(code, message);
        }
    }
    
    public void onFailure(String code, String message) {
        ConsoleUtils.error(message);
    }
    
    @Override
    public void onResponseReceived(Request request, Response response) {
        ScriptObject responseObject = null;
        String text = response.getText();
        try {
            responseObject = ScriptObject.fromJson(text);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing " + response.getStatusCode() + " response: " + text + ".", e);
        }
        assert responseObject.hasKey("status");
        String status = responseObject.get("status");
        
        ScriptObject result = responseObject.hasKey("result") ? responseObject.getObject("result") : null;
        if(debug) {
            String message = (result == null ? "Null result." : text);
            ConsoleUtils.error(message);
        }
        if("OK".equals(status)) {
            onSuccess(result);
        } else if("ERROR".equals(status)) {    
            onFailure(result);
        } else {
            String message = "Unhandled status \"" + status + "\" (" + response.getStatusCode() + ") with response \"" + text.trim() + "\".";
            onFailure(Integer.toString(response.getStatusCode()), message);
        }
    }

    @Override
    public final void onError(Request request, Throwable t) {
        onFailure(t.getClass().getName(), t.getMessage());
    }
}