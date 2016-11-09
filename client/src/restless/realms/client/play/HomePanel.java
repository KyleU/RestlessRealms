package restless.realms.client.play;

import restless.realms.client.ClientState;
import restless.realms.client.bank.StashPanel;
import restless.realms.client.belt.Quickslot;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.layout.PanelLocation;
import restless.realms.client.util.CommonEventHandlers;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.NavigationPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class HomePanel extends NavigationPanel {
    private static HomePanel instance;

    private DeckPanel deck;
    private StashPanel stashPanel;
    
    private ServiceCallback stashCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            Quickslot[] contents = stashPanel.getContents();
            JsArray<ScriptObject> items = result.getArray("items");
            for(int i = 0; i < contents.length; i++) {
                if(i < items.length()) {
                    if(items.get(i) != null) {
                        Quickslot quickslot = contents[i];
                        ScriptObject item = items.get(i);
                        quickslot.getActionIcon().show("item", item, true);
                    }
                }
            }
            deck.showWidget(1);
        }
    };
    
    public HomePanel() {
        super("home", "Your Home");
        addLink("lobby", "The Lobby", "An area to show off your home decorations.");
        addLink("stash", "Your Stash", "An account-wide storage area.");
        addLink("settings", "Game Options", "Settings for Restless Realms.");
        
        deck = new DeckPanel();
        deck.add(new SimplePanel());
        stashPanel = new StashPanel();
        deck.add(stashPanel);
        setRightPanel(deck);
        addExitButton(CommonEventHandlers.CLICK_ENTER_CITY);
    }
    
    @Override
    protected void onNavigation(String key) {
        select(key);
        if("stash".equals(key)) {
            ConsoleUtils.error("The stash is coming soon!");
            //ServiceManager.call("stash", "list", stashCallback);
        } else if("settings".equals(key)) {
            ConsoleUtils.error("Settings are coming soon!");
        } else {
            ConsoleUtils.error("The " + key + " is coming soon!");
            deck.showWidget(0);
        }
    }
    
    public static void show() {
        if(instance == null) {
            instance = new HomePanel();
            ClientState.getLayout().addPanel(PanelLocation.MAIN, "home", instance);
        }
        instance.select(null);
        ClientState.getLayout().showPanel("home");
    }
}
