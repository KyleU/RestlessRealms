package restless.realms.client.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.action.ActionDetailPanel;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.util.AuditManager;
import restless.realms.client.util.CommonEventHandlers;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.DialogBox;
import restless.realms.client.widget.DialogPanel;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ItemShopPanel extends WindowPanel<AbsolutePanel> {
    private final ScrollPanel sellPanel = new ScrollPanel();
    private final ScrollPanel buyPanel = new ScrollPanel();
    private VerticalPanel sellList = new VerticalPanel();
    private VerticalPanel buyList = new VerticalPanel();
    private int sellScrollPosition = 0;
    private int buyScrollPosition = 0;

    private final Image shopkeeperImage = new Image();
    
    private ServiceCallback callback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            ClientState.setCurrency(result.getInt("currency"));
            ClientState.setTokens(result.getInt("tokens"));
            ClientState.setItems(result.getArray("items"));
            refreshSellPanel();
            ClientState.getLayout().showPanel("itemshop");
            loadScrollPositions();
        }

        @Override
        public void onFailure(String code, String message) {
            super.onFailure(code, message);
            ClientState.getLayout().showPanel("itemshop");
        };
    };

	
	public ItemShopPanel() {
	    super("itemshop", new AbsolutePanel(), "Item Shop", null);

	    Image i = new Image("img/shop/banner.png");
	    body.add(i, 0, 0);
	    
        sellPanel.setStylePrimaryName("itemshopsellpanel");
        sellPanel.addStyleName("actionlist");
        buyPanel.setStylePrimaryName("itemshopbuypanel");
        buyPanel.addStyleName("actionlist");

        body.add(sellPanel, 263, 51);
        sellPanel.add(sellList);

        body.add(buyPanel, 0, 51);
        buyPanel.add(buyList);
		
        ButtonPanel button = new ButtonPanel("Sell All Trash", new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                DialogPanel.show(new DialogBox("Selling Item", "Selling all vendor trash."));
                ServiceManager.call("shop", "selltrash", callback);
            }
        }, 2);
        body.add(button, 384, 10);

        shopkeeperImage.setWidth("226px");
        shopkeeperImage.setHeight("300px");
        body.add(shopkeeperImage, 526, 0);
        
        addExitButton(CommonEventHandlers.CLICK_ENTER_CITY);
	}
	
	public void show(final String merchant) {
	    resetScrollPositions();
	    
        sellList.clear();
        sellList.add(new Label("Loading Items..."));
        
        buyList.clear();
        buyList.add(new Label("Loading Items..."));

        shopkeeperImage.setUrl("img/shop/" + merchant + ".png");
        
        ClientState.getLayout().showPanel("itemshop");
        AuditManager.audit("city", "itemshop", merchant, null);

        ServiceCallback serviceCallback = new ServiceCallback() {
            @Override
            public void onSuccess(ScriptObject ret) {
                buyList.clear();
                setWindowTitle(ret.get("name"));
                JsArray<ScriptObject> items = ret.getArray("items");
                for(int i = 0; i < items.length(); i++) {
                    ScriptObject item = items.get(i);
                    final int id = item.getInt("id");
                    final String name = item.get("name");
                    final int msrp = item.getInt("msrp");
                    ClickHandler clickHandler = new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            ActionDetailPanel.getInstance().clear();
                            if(event.getNativeEvent().getShiftKey()) {
                                saveScrollPositions();
                                String priceString = NumberFormat.getDecimalFormat().format(msrp);
                                DialogPanel.show(new DialogBox("Buying Item", "Buying " + name + " for " + (priceString) + " gold."));
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("item", String.valueOf(id));
                                params.put("merchant", merchant);
                                ServiceManager.call("shop", "buy", callback, params);
                            } else if(event.getNativeEvent().getCtrlKey()) {
                                saveScrollPositions();
                                String priceString = NumberFormat.getDecimalFormat().format(msrp);
                                DialogPanel.show(new DialogBox("Buying Items", "Buying as many " + name + " as possible for " + (priceString) + " gold each."));
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("item", String.valueOf(id));
                                params.put("merchant", merchant);
                                ServiceManager.call("shop", "buyall", callback, params);
                            } else {
                                buy(merchant, id, name, msrp);
                            }
                        }
                    };
                    ItemPanel itemPanel = new ItemPanel(item, "buy", clickHandler, false);
                    buyList.add(itemPanel);
                }
            }
        };
        refreshSellPanel();
        ServiceManager.call("shop", "list", serviceCallback, "merchant", merchant);
        
	}

    public void refreshSellPanel() {
        sellList.clear();
        JsArray<ScriptObject> items = PlayerCharacterCache.getInstance().getItems();
        List<Integer> equipmentIds = PlayerCharacterCache.getInstance().getEquipmentIds();
        List<ScriptObject> sortedItems = new ArrayList<ScriptObject>(items.length());
        Map<ScriptObject, ClickHandler> clickHandlers = new HashMap<ScriptObject, ClickHandler>();
        
        for(int i = 0; i < items.length(); i++) {
            ScriptObject item = items.get(i);
            
            final int index = i;
            final int id = item.getInt("id");
            final String name = item.get("name");
            final String type = item.get("type");
            final int msrp = item.getInt("msrp");

            if(equipmentIds.contains(id)) {
                equipmentIds.remove((Integer)id);
            } else {
                if(!type.equals("QUEST")) {
                    ClickHandler clickHandler = new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            ActionDetailPanel.getInstance().clear();
                            if(event.getNativeEvent().getShiftKey()) {
                                saveScrollPositions();
                                String priceString = NumberFormat.getDecimalFormat().format(msrp / 10);
                                DialogPanel.show(new DialogBox("Selling Item", "Selling " + name + " for " + priceString + " gold."));
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("item", String.valueOf(id));
                                params.put("index", String.valueOf(index));
                                ServiceManager.call("shop", "sell", callback, params );
                            } else {
                                sell(index, id, name, msrp);
                            }
                        }
                    };
                    sortedItems.add(item);
                    clickHandlers.put(item, clickHandler);
                }
            }
        }

        Collections.sort(sortedItems, new Comparator<ScriptObject>() {
            @Override
            public int compare(ScriptObject item1, ScriptObject item2) {
                return item1.get("name").compareTo(item2.get("name"));
            }
        });
        
        for(ScriptObject item : sortedItems) {
            ItemPanel itemPanel = new ItemPanel(item, "sell", clickHandlers.get(item), false);
            sellList.add(itemPanel);
        }
    }

    private void buy(final String merchant, final int id, String name, int msrp) {
        saveScrollPositions();
        String priceString = NumberFormat.getDecimalFormat().format(msrp);
        String message = "Are you sure you want to buy a " + name + " for " + priceString + " gold?";
        message += "<br/><br/><span style='color:#ccc'>You may also shift-click items to buy them automatically, or ctrl-click to buy as many as you can carry.</span>";
        DialogBox buyPrompt = new DialogBox("Buy Item", message, "Buy", "Cancel") {
            @Override
            public void onAction(String action) {
                if("Buy".equals(action)) {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("item", String.valueOf(id));
                    params.put("merchant", merchant);
                    ServiceManager.call("shop", "buy", callback, params );
                } else if("Cancel".equals(action)) {
                    ClientState.getLayout().showPanel("itemshop");
                } 
            }
        };
        DialogPanel.show(buyPrompt);
    }

    private void sell(final int index, final int id, String name, int msrp) {
        saveScrollPositions();
        String message = "Are you sure you want to sell your " + name + " for " + ((int)(msrp * 0.10)) + " gold?.<br/><br/><span style='color:#ccc'>You may also shift-click items to sell them automatically.</span>";
        DialogBox buyPrompt = new DialogBox("Sell Item", message, "Sell", "Cancel") {
            @Override
            public void onAction(String action) {
                if("Sell".equals(action)) {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("item", String.valueOf(id));
                    params.put("index", String.valueOf(index));
                    ServiceManager.call("shop", "sell", callback, params );
                } else if("Cancel".equals(action)) {
                    ClientState.getLayout().showPanel("itemshop");
                } 
            }
        };
        DialogPanel.show(buyPrompt);
    }

    private void resetScrollPositions() {
        buyScrollPosition = 0;
        sellScrollPosition = 0;
    }

    private void saveScrollPositions() {
        buyScrollPosition = buyPanel.getScrollPosition();
        sellScrollPosition = sellPanel.getScrollPosition();
    }

    private void loadScrollPositions() {
        buyPanel.setScrollPosition(buyScrollPosition);
        sellPanel.setScrollPosition(sellScrollPosition);
    }
}