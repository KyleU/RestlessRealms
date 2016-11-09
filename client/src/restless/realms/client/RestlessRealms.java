package restless.realms.client;

import restless.realms.client.facebook.FacebookManager;
import restless.realms.client.layout.MainLayout;
import restless.realms.client.play.PlayCallback;
import restless.realms.client.util.ExceptionHandler;
import restless.realms.client.util.GwtUtils;
import restless.realms.client.util.KeyboardHandler;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;

/**
 * Main client-side EntryPoint for Restless Realms.
 */
public class RestlessRealms implements EntryPoint {
    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(new ExceptionHandler());

        // Defer to allow exception handler to kick in.
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                init();
            }
        });
    }
    
    private void init() {
        DOM.getElementById("container-main").setInnerText("");
        
        GwtUtils.exportStaticMethods();
        
        ClientManager.init();
        ClientState.setLayout(new MainLayout());
        
        KeyboardHandler.init();
        
        String sessionId = Cookies.getCookie("rrsession");
        if(sessionId == null) {
            String userAgent = GwtUtils.getUserAgent().toLowerCase();
            boolean safari = userAgent.indexOf("safari") > -1 && userAgent.indexOf("chrome") == -1;
            if(safari) {
                Window.open("/index.html", "_top", "");
            } else {
                Window.Location.assign("/index.html");
            }
            return;
        }
         
        ClientState.setSessionId(sessionId);
        ServiceCallback playCallback = new PlayCallback();
        ServiceManager.call("play", "play", playCallback);
    }

    public static void signout(boolean closeSession) {
        if(closeSession) {
            Cookies.setCookie("rrsession", "");
            Cookies.removeCookie("rrsession");
            ServiceManager.call("account", "signout", new ServiceCallback() {
                @Override
                public void onSuccess(ScriptObject result) {
                    if(ClientState.getProvider().equals("Facebook")) {
                        try {
                            FacebookManager.signout();
                        } catch(Exception e) {
                            Window.Location.replace("/index.html?error=Thanks for playing.");
                        }
                    } else {
                        Window.Location.replace("/index.html?error=Thanks For Playing!");
                    }
                }
            });
        } else {
            ServiceManager.call("account", "characterselect", new ServiceCallback() {
                @Override
                public void onSuccess(ScriptObject result) {
                    Window.Location.replace("/index.html");
                }
            });
        }
    }
}