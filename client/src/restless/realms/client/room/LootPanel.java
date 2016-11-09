package restless.realms.client.room;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.action.ActionDetailPanel;
import restless.realms.client.audio.AudioManager;
import restless.realms.client.item.ItemPanel;
import restless.realms.client.layout.SizeConstants;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LootPanel extends WindowPanel<AbsolutePanel> {
    private boolean active;
    private HTML text;
    private String type;
    private ButtonPanel lootAllButton;
    private VerticalPanel contentPanels;

    private ClickHandler exitHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            if(type.equals("COMBAT")) {
                ClientManager.setPerspective("adventure");
            } else if(type.equals("PVP")) {
                ClientManager.setPerspective("play");
            } else {
                ClientManager.send(MessageType.WINDOW_CLOSE);
            }
        }
    };
    
    private ServiceCallback loadCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            onResult(result);
        }
    };
     
    private ServiceCallback lootCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            ClientState.setItems(result.getArray("playerItems"));
            JsArray<ScriptObject> newContents = result.getArray("roomContents");
            setContents(newContents);
            if(newContents.length() == 0) {
                active = false;
                exitHandler.onClick(null);
            }
        }
    };
    private Timer soundTimer = new Timer() {
        public void run() {
            AudioManager.play("loot");
        };
    };
    
    public LootPanel() {
        super("loot", new AbsolutePanel(), "Free Loot!", null);
        addExitButton(exitHandler);
        text = new HTML();
        text.setWidth("340px");
        body.add(text, 5, 5);
        
        ClickHandler lootAllClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                lootAll();
            }
        };
        lootAllButton = new ButtonPanel("Loot All", lootAllClickHandler, 2);
        body.add(lootAllButton, SizeConstants.BUTTON_LEFT, SizeConstants.BUTTON_TOP);

        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.setWidth(SizeConstants.ACTION_CONTAINER_WIDTH + "px");
        scrollPanel.setHeight(SizeConstants.MAIN_HEIGHT + "px");
        scrollPanel.setStylePrimaryName("actionlist");

        contentPanels = new VerticalPanel();
        scrollPanel.add(contentPanels);
        body.add(scrollPanel, 488, 0);
    }

    private void clear() {
        this.type = null;
        setWindowTitle("");
        text.setHTML("Loading...");
        contentPanels.clear();
    }

    public void show(String type) {
        clear();
        this.type = type;
        ClientState.getLayout().showPanel("loot");
        if(type.equals("PVP")) {
            ServiceManager.call("pvp", "result", loadCallback);
//            ChatService.refreshNow();
        } else {
            ServiceManager.call("room", "contents", loadCallback);
        }
    }

    public void lootAll() {
        boolean pvp = type.equals("PVP");
        if(pvp) {
            ClientState.setAdventureId(null);
            ClientManager.setPerspective("play");
        } else {
            ServiceManager.call("room", "lootall", lootCallback);
        }
    }

    protected void onResult(ScriptObject result) {
        active = true;

        soundTimer.schedule(500);
        
        if(type.equals("LOOT")) {
            setWindowTitle("Treasure");
        } else if(type.equals("COMBAT")) {
            setWindowTitle("Victory");
        } else if(type.equals("PVP")) {
            setWindowTitle("Duel Victory");
        } else {
            assert false;
        }
        
        String contentText = "";
        int newCurrency = result.getInt("playerCurrency");
        int oldCurrency = PlayerCharacterCache.getInstance().getCurrency();
        if(newCurrency - oldCurrency != 0) {
            contentText += "You looted " + (newCurrency - oldCurrency) + " gold!<br/>";
            ClientState.setCurrency(newCurrency);
        }
        
        int newTokens = result.getInt("playerTokens");
        int oldTokens = PlayerCharacterCache.getInstance().getTokens();
        if(newTokens - oldTokens != 0) {
            contentText += "You looted " + (newTokens - oldTokens) + " tokens!<br/>";
            ClientState.setTokens(newTokens);
        }
        
        int newXp = result.getInt("playerXp");
        int oldXp = PlayerCharacterCache.getInstance().getXp();
        if(newXp - oldXp > 0) {
            contentText += "You gained " + (newXp - oldXp) + " experience!<br/>";
            ScriptObject xp = ScriptObject.fromJson("{xp:" + newXp + "}");
            ClientState.setPlayerCharacter(xp);
        } else if(newXp - oldXp < 0) {
            LevelUpPanel.show(new Runnable() {
                @Override
                public void run() {
                    ClientState.getLayout().showPanel("loot");
                }
            });
        } else {
            //no xp, no op
        }
        text.setHTML(contentText);
        
        JsArray<ScriptObject> contents = result.getArray("contents");
        setContents(contents);
    }

    private void setContents(JsArray<ScriptObject> contents) {
        this.contentPanels.clear();
        for(int i = 0; i < contents.length(); i++) {
            final ScriptObject item = contents.get(i);
            final int index = i;
            ClickHandler clickHandler = new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    ActionDetailPanel.getInstance().clear();
                    String service = type.equals("PVP") ? "pvp" : "room";
                    ServiceManager.call(service, "lootitem", lootCallback, "itemId", item.getInt("id"), "itemIndex", index);
                }
            };
            ItemPanel itemPanel = new ItemPanel(item, "loot", clickHandler, false);
            this.contentPanels.add(itemPanel);
        }
    }

    public boolean isActive() {
        return active;
    }
}
