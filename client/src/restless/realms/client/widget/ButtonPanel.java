package restless.realms.client.widget;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

public class ButtonPanel extends Button {
    public ButtonPanel(String html, ClickHandler handler, int size) {
        super(html, handler);
        if(size == 0) {
            this.setStylePrimaryName("button-header");
        } else if(size == 1) {
            this.setStylePrimaryName("button-small");
        } else if(size == 2) {
            this.setStylePrimaryName("button-large");
        } else {
            assert false;
        }
    }
}
