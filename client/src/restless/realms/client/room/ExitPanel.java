package restless.realms.client.room;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.audio.AudioManager;
import restless.realms.client.layout.SizeConstants;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.util.AuditManager;
import restless.realms.client.util.CommonEventHandlers;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.WindowPanel;
import restless.realms.client.worldmap.MapLocation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;

public class ExitPanel extends WindowPanel<AbsolutePanel> {
    private ServiceCallback completionCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            AuditManager.audit("adventure", "complete", PlayerCharacterCache.getInstance().getName(), 1);
            ClientState.setCurrency(result.getInt("currency"));
            ClientState.setTokens(result.getInt("tokens"));
            
            int newXp = result.getInt("xp");
            int oldXp = PlayerCharacterCache.getInstance().getXp();
            boolean levelUp = newXp - oldXp < 0;
            ClientState.setPlayerCharacter(result);
            ClientState.setAdventureId(null);
            ClientManager.setPerspective("play");
            if(levelUp) {
                LevelUpPanel.show(new Runnable() {
                    @Override
                    public void run() {
                        ClientManager.send(MessageType.WINDOW_CLOSE);
                    }
                });
            } else {
                AudioManager.play("complete");
                if(!"tutorial".equals(ClientState.getAdventureType())) {
//                    FacebookManager.streamPublish(
//                        "adventurecomplete", 
//                        "Share your victory!",
//                        "Another dungeon down, another friend you're now envious of.",
//                        "Adventure Complete in Restless Realms!", 
//                        "Want to play? It's free. Go ahead and click the play now link to join me in Restless Realms."
//                    );
                }
            }
        }
    };

    private HTML text;
    private ButtonPanel completeButton;
    private ButtonPanel cancelButton;

    public ExitPanel() {
        super("exit", new AbsolutePanel(), "Complete Adventure", null);
        addExitButton(CommonEventHandlers.CLICK_WINDOW_CLOSE);
        text = new HTML();
        text.setWidth("500px");
        body.add(text, 5, 5);
        
        ClickHandler completeClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                completeButton.setEnabled(false);
                ServiceManager.call("adventure", "complete", completionCallback);
            }
        };
        completeButton = new ButtonPanel("Complete", completeClickHandler, 2);
        body.add(completeButton, SizeConstants.BUTTON_LEFT, SizeConstants.BUTTON_TOP);
        
        cancelButton = new ButtonPanel("Cancel", CommonEventHandlers.CLICK_WINDOW_CLOSE, 2);
        body.add(cancelButton, SizeConstants.BUTTON_LEFT + 135, SizeConstants.BUTTON_TOP);
    }

    public void show(String adventureType) {
        int minLevel = 0;
        int maxLevel = 0;
        for(MapLocation loc : ClientState.getMapLocations()) {
            if(loc.getId().equals(adventureType)) {
                minLevel = loc.getMinLevel();
                maxLevel = loc.getMaxLevel();
                break;
            }
        }
        
        assert minLevel > 0 : minLevel;
        assert maxLevel > 0 : minLevel;
        
        completeButton.setEnabled(true);
        text.setHTML(getText(minLevel, maxLevel));
        ClientState.getLayout().showPanel("exit");
    }

    private String getText(int minLevel, int maxLevel) {
        int xp = maxLevel * 250;
        int gold = minLevel * 100;
        String ret = 
            "You've discovered an exit. Leave now and you'll be rewarded for completing this adventure.<br/><br/>" +
            "If you leave however, this adventure will no longer be available and you'll have to start a new one.<br/><br/>" +
            "You will be rewarded with " + xp  + " xp and " + gold + " gold if you choose to complete this adventure.";
        return ret;
    }
}
