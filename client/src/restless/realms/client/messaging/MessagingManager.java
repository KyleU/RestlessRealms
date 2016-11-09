package restless.realms.client.messaging;

import restless.realms.client.ClientState;

import com.google.gwt.core.client.GWT;
import com.greencat.gwt.comet.client.CometClient;
import com.greencat.gwt.comet.client.CometSerializer;

public class MessagingManager {
    private static MessagingListener messagingListener;
    
    public static void init() {
        messagingListener = new MessagingListener();
    
        CometSerializer serializer = GWT.create(StringSerializer.class);
        String url = "/messaging/update?rrsession=" + ClientState.getSessionId();
        
        CometClient client = new CometClient(url, serializer, messagingListener);
        messagingListener.setClient(client);
        client.setReconnectionTimout(10000);
        client.start();
    }
}
