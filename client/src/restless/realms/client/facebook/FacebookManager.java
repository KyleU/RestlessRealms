package restless.realms.client.facebook;

import com.google.gwt.core.client.JavaScriptObject;

public abstract class FacebookManager extends JavaScriptObject {
    protected FacebookManager() {
    }

    public static native void signout() /*-{
        $wnd.FB.Connect.logoutAndRedirect('/index.html');
    }-*/;

    public static native void streamPublishX(String key, String prompt, String defaultMessage, String title, String message) 
    /*-{
        $wnd.FB.ensureInit(function() {
            var attachment = { 
                'name': title, 
                'href': 'http://restlessrealms.com', 
                'caption': message, 
                'properties': {
                },
                'media': [
                    { 
                        'type': 'image', 
                        'src': 'http://restlessrealms.com/img/achievement/' + key + '.png', 
                        'href': 'http://restlessrealms.com/index.html'
                    }
                ]
            }; 
            var addedLinks = [{ "text": "Play Now", "href": "http://restlessrealms.com/index.html" }];
            $wnd.FB.Connect.streamPublish(defaultMessage, attachment, addedLinks, null, prompt);
        });
    }-*/;
}