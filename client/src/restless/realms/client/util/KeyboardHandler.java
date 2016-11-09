package restless.realms.client.util;

import restless.realms.client.ClientManager;
import restless.realms.client.perspective.MessageType;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;

public class KeyboardHandler implements Event.NativePreviewHandler {
    private static HandlerRegistration registration;
    
    private KeyboardHandler() {
    }
    
    public static void init() {
        if(registration != null) {
            throw new IllegalStateException();
        }
        registration = Event.addNativePreviewHandler(new KeyboardHandler());
    }
    
    @Override
    public void onPreviewNativeEvent(NativePreviewEvent event) {
        int type = event.getTypeInt();
        if(type == 128) {
            NativeEvent ne = event.getNativeEvent();
            if(ne.getAltKey() || ne.getCtrlKey() || ne.getMetaKey() || ne.getShiftKey()) {
                //ignore
            } else {
                ClientManager.send(MessageType.KEY_PRESS, event);
            }
        }
    }

}
