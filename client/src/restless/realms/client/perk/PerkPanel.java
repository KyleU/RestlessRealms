package restless.realms.client.perk;

import restless.realms.client.ClientManager;
import restless.realms.client.action.ActionPanel;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class PerkPanel extends ActionPanel {
    public PerkPanel(final ScriptObject perk, ClickHandler iconClickHandler, boolean linkName) {
        this.setTitle(perk.get("name"));
        this.setSummary(perk.get("summary"));

        this.setAction("perk", perk, iconClickHandler == null);
        if(iconClickHandler != null) {
            this.getIcon().addStyleName("buy");
            this.getIcon().addClickHandler(iconClickHandler);
        }
        
        if(linkName) {
            ClickHandler linkClickHandler = new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    ClientManager.send(MessageType.EQUIP, getIcon(), 0);
                }
            };
            this.setTitleLink(linkClickHandler);
        }

    }
}
