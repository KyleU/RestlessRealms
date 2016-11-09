package restless.realms.client.item;


import restless.realms.client.ClientManager;
import restless.realms.client.action.ActionPanel;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Label;

public class ItemPanel extends ActionPanel {
    private Label quantityLabel;
    
	public ItemPanel(final ScriptObject item, final String panelType, ClickHandler clickHandler, boolean linkName) {
        this.setTitle(item.get("name"));
        this.setRarity(item.getInt("rarity"));
        this.setSummary(item.get("summary"));

        boolean draggable = false;
        if("buy".equals(panelType)) {
            int price = item.getInt("msrp");
            String priceString = NumberFormat.getDecimalFormat().format(price);
            this.setAdditionalInfo("Buy for " + priceString + "G");
        } else if("sell".equals(panelType)) {
            int price = (int)Math.round(item.getInt("msrp") * 0.10);
            String priceString = NumberFormat.getDecimalFormat().format(price);
            this.setAdditionalInfo("Sell for " + priceString + "G");
        } else if("loot".equals(panelType)) { 
            //no op
        } else if("inv".equals(panelType)) {
            String type = item.get("type");
            if(type.equals("WEAPON")) {
                draggable = true;
            } else if(type.equals("CONSUMABLE")) {
                draggable = true;
            } else if(
                type.equals("HEAD") ||
                type.equals("CHEST") ||
                type.equals("LEGS") || 
                type.equals("ACCESSORY")
            ) {
                draggable = true;
            } else {
                //ConsoleUtils.debug(type);
            }
        } else  {
            assert false;
        }

        this.setAction("item", item, draggable);

        if("buy".equals(panelType)) {
            this.getIcon().addStyleName("buy");
        } else if("sell".equals(panelType)) {
            this.getIcon().addStyleName("sell");
        }
        if(clickHandler != null) {
            this.getIcon().addClickHandler(clickHandler);
            this.getIcon().getElement().getStyle().setProperty("cursor", "pointer");
        }
        
        if(linkName) {
            if(item.get("type").equals("QUEST") || item.get("type").equals("TRASH")) {
                //no op
            } else if(item.get("type").equals("CONSUMABLE")) {
                ClickHandler linkClickHandler = new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        ClientManager.send(MessageType.ACTIVATE, "item", item.getInt("id"));
                    }
                };
                this.setTitleLink(linkClickHandler);
            } else {
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

    public void setQuantity(int quantity) {
        if(quantity > 1) {
            this.quantityLabel = new Label("" + quantity);
            this.quantityLabel.setStylePrimaryName("quantity");
            getBody().add(this.quantityLabel, 25, 5);
        }
    }
    
    
}