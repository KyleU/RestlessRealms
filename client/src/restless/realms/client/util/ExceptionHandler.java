package restless.realms.client.util;

import restless.realms.client.console.ConsoleUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class ExceptionHandler implements GWT.UncaughtExceptionHandler {
    @Override
    public void onUncaughtException(Throwable t) {
        try {
            if(t.getClass().equals(RuntimeException.class) && t.getCause() != null) {
                t = t.getCause();
            }
            String type = t.getClass().getName();
            if(type.indexOf(".") > -1) {
                type = type.substring(type.lastIndexOf(".")+1);
            }
            t.printStackTrace();
            if(type.equals("JavaScriptException")) {
                ConsoleUtils.error("Error encountered processing a response from the server.");
            } else {
                ConsoleUtils.error("Uncaught Exception: " + type + ".");
            }

            t.printStackTrace();
            AuditManager.audit("exception", "uncaught", type, null);
        } catch(Exception e) {
            Window.alert("Error \"" + e.toString() + "\" encountered while trying to log \"" + t.toString() + "\"");
        }
    }
    
}
