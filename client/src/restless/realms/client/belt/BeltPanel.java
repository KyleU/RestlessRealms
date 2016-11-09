package restless.realms.client.belt;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.action.ActionIcon;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.dragdrop.DragDropManager;
import restless.realms.client.inventory.InventoryHandler;
import restless.realms.client.mail.MessagesLink;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.playercharacter.PlayerCharacterHandler;
import restless.realms.client.playercharacter.PlayerCharacterPanel;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.ProgressBar;
import restless.realms.client.widget.TipPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

public class BeltPanel extends Composite implements PlayerCharacterHandler, InventoryHandler {
    public class QuickslotServiceCallback extends ServiceCallback {
        @Override
        public void onSuccess(ScriptObject result) {
            setQuickslots(result.get("quickslots"));
        }
        @Override
        public void onFailure(String code, String message) {
            if("IllegalArgumentException".equals(code)) {
                ConsoleUtils.error(message);
            } else {
                super.onFailure(code, message);
            }
        }
    }

    private AbsolutePanel body;
    private HTML currency;
    private HTML tokens;
    
    private Quickslot[] quickslots = new Quickslot[8];
    
    private ProgressBar hitpoints;    
    private ProgressBar mana;
    
    public BeltPanel() {
        body = new AbsolutePanel();
        body.setHeight("70px");
        body.setWidth("750px");
        initWidget(body);
        
        currency = new HTML("<div><span id=\"currency\">0</span></div>");
        currency.setStylePrimaryName("beltstats");
        currency.setTitle("Gold");
        currency.getElement().setId("currencyPanel");
        body.add(currency, 5, 5);

        tokens = new HTML("<div><span id=\"tokens\">0</span></div>");
        tokens.setStylePrimaryName("beltstats");
        tokens.setTitle("Tokens (use to buy perks)");
        tokens.getElement().setId("tokensPanel");
        body.add(tokens, 575, 5);
        
        for(int i = 0; i < quickslots.length; i++) {
            Quickslot quickslot = new Quickslot(i);
            quickslots[i] = quickslot;
            body.add(quickslot, 185 + (48 * i), 4);
            DragDropManager.registerDroppable(quickslot);
        }
        
        hitpoints = new ProgressBar(189, 0, " health", true);
        body.add(hitpoints, 185, 51);
        mana = new ProgressBar(189, 1, " mana", true);
        body.add(mana, 377, 51);
        
        initHeader();
        
        ClientState.addPlayerCharacterHandler(this);
        ClientState.addInventoryHandler(this);
    }
    
    private void initHeader() {
        HTML adventures = new HTML("Adventures: <span id=\"adventurePoints\">0</span>");
        adventures.getElement().setId("adventures");
        RootPanel.get("pageheader").add(adventures);

        HTML leaderboardLink = new HTML("Leaderboards");
        leaderboardLink.getElement().setId("leaderboardlink");
        leaderboardLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ClientManager.send(MessageType.LEADERBOARD_OPEN);
            }
        });
        RootPanel.get("pageheader").add(leaderboardLink);

        MessagesLink.init();
        TipPanel.init();
    }

    @Override
    public void onPlayerCharacter(ScriptObject playerCharacter) {
        assert playerCharacter != null;
        if(playerCharacter.hasKey("quickslots")) {
            setQuickslots(playerCharacter.get("quickslots"));
        }
        if(playerCharacter.hasKey("maxHitpoints")) {
            hitpoints.setMaxValue(playerCharacter.getInt("maxHitpoints"));
        }
        if(playerCharacter.hasKey("hitpoints")) {
            this.hitpoints.setValue(playerCharacter.getInt("hitpoints"), true);
        }
        if(playerCharacter.hasKey("maxMana")) {
            mana.setMaxValue(playerCharacter.getInt("maxMana"));
        }
        if(playerCharacter.hasKey("mana")) {
            this.mana.setValue(playerCharacter.getInt("mana"), true);
        }
    }
    
    @Override
    public void applyEffects(JsArray<ScriptObject> actions, JsArrayString actionNames) {
        // No op
    }
    
    @Override
    public void onCurrency(int currency) {
        DOM.getElementById("currency").setInnerText("" + currency);
    }
    
    @Override
    public void onTokens(int tokens) {
        DOM.getElementById("tokens").setInnerText("" + tokens);
    }
    
    @Override
    public void onItems(JsArray<ScriptObject> items) {
        //no op
    }

    @Override
    public void onPerks(JsArray<ScriptObject> items) {
        //no op
    }

    public Quickslot[] getQuickslots() {
        return quickslots;
    }

    public void onAdventurePoints(int adventurePoints) {
        DOM.getElementById("adventurePoints").setInnerText("" + adventurePoints);
    }

    public void setQuickslots(String quickslotsString) {
        String[] strings = quickslotsString.split(",");
        assert strings.length == 8 : quickslotsString;
        for(int i = 0; i < strings.length; i++) {
            String quickslotString = strings[i];
            if(quickslotString.equals("0")) {
                quickslots[i].clear();
            } else {
                char typeCode = quickslotString.charAt(0);
                int id = Integer.parseInt(quickslotString.substring(1));
                ScriptObject target = null;
                if(typeCode == 's') {
                    target = PlayerCharacterCache.getInstance().getSkill(id);
                } else if (typeCode == 'i') {
                    target = PlayerCharacterCache.getInstance().getItem(id);
                } else {
                    assert false;
                }

                if(target == null) {
                    ConsoleUtils.error("Quickslot " + i + " refers to " + (typeCode == 'i' ? "item" : "skill") + " " + id + ", which you do not have.");
                    quickslots[i].clear();
                } else {
                    String type = typeCode == 's' ? "skill" : "item";
                    ActionIcon icon = new ActionIcon();
                    icon.show(type, target, false);
                    quickslots[i].setActionIcon(icon);
                }
            }
        }
        
        if(ClientManager.getActivePerspective().equals("combat") && ClientState.getAdventureId() > 0) {
            ClientManager.send(MessageType.REFRESH, "overlays");
        }
    }

    public void setQuickslot(final int index, final ActionIcon icon) {
        assert index < 8 && index >= 0 : index;
        String quickslotsString = "";
        for(Quickslot quickslot : quickslots) {
            if(quickslot.getIndex() == index) {
                if(icon == null) {
                    quickslotsString += "0";
                } else {
                    quickslotsString += icon.getType().substring(0, 1) + icon.getAction().get("id");
                }
            } else {
                if(quickslot.getId() == 0) {
                    quickslotsString += "0";
                } else {
                    quickslotsString += String.valueOf(quickslot.getTypeCode()) + quickslot.getId();
                }
            }
            if(quickslot.getIndex() != 7) {
                quickslotsString += ",";
            }
        }
        ServiceManager.call("character", "quickslots", new QuickslotServiceCallback(), "quickslots", quickslotsString);
    }

    public void clearOverlays() {
        for(Quickslot quickslot : quickslots) {
            quickslot.removeOverlays();
        }
        ((PlayerCharacterPanel)ClientState.getLayout().getPanel("player")).clearRecurringEffects();
    }

    public void fadeIn() {
        hitpoints.fadeIn();
        mana.fadeIn();
    }

    public void fadeOut() {
        hitpoints.fadeOut();
        mana.fadeOut();
    }
}
