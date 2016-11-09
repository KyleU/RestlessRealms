package restless.realms.client.play;

import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.PurchasePanel;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class NoAdventuresPanel extends WindowPanel<AbsolutePanel> {
    public NoAdventuresPanel() {
        super("noadventures", new AbsolutePanel(), "You've got no adventure points left!", null);
        
        ButtonPanel purchaseButton = new ButtonPanel("Purchase APs" , new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                PurchasePanel.show();
            }
        }, 2);
        body.add(purchaseButton, 290, 210);
    }
}