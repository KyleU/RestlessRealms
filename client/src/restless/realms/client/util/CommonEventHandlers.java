package restless.realms.client.util;

import restless.realms.client.ClientManager;
import restless.realms.client.action.ActionDetailPanel;
import restless.realms.client.action.ActionIcon;
import restless.realms.client.perspective.MessageType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;

public interface CommonEventHandlers {
    public static final ClickHandler CLICK_WINDOW_CLOSE = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            ClientManager.send(MessageType.WINDOW_CLOSE);
        }
    };

    public static final ClickHandler CLICK_ENTER_CITY = new ClickHandler(){
        @Override
        public void onClick(ClickEvent event) {
            ClientManager.send(MessageType.ENTER_LOCATION, "city");
        }
    };
    
    public static MouseOverHandler TOOLTIP_MOUSE_OVER_SIDE = new MouseOverHandler() {
        @Override
        public void onMouseOver(MouseOverEvent event) {
            ActionIcon source = (ActionIcon)event.getSource();
            ActionDetailPanel.getInstance().setAction(source, false);
        }
    };

    public static MouseOverHandler TOOLTIP_MOUSE_OVER_ABOVE = new MouseOverHandler() {
        @Override
        public void onMouseOver(MouseOverEvent event) {
            ActionIcon source = (ActionIcon)event.getSource();
            ActionDetailPanel.getInstance().setAction(source, true);
        }
    };
    
    public static MouseOutHandler TOOLTIP_MOUSE_OUT = new MouseOutHandler() {
        @Override
        public void onMouseOut(MouseOutEvent event) {
            ActionDetailPanel.getInstance().clear();
        }
    };

}
