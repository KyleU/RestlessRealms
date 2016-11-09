package restless.realms.client.combat;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.audio.AudioManager;
import restless.realms.client.layout.SizeConstants;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class DeathPanel extends WindowPanel<AbsolutePanel> {
    private Label nameLabel;
    private Label penaltyText;
    
    public DeathPanel() {
        super("death", new AbsolutePanel(), "You Died", null);
        
        Label text = new Label("Well, you're dead.  It happens.  Next time, be a little more careful. Now might be a good time to head to the Outpost to stock up on supplies. If you need help, check the guide.  You'll need to spend an adventure point to start your next adventure.");
        text.setStylePrimaryName("largetext");
        text.setWidth("470px");
        body.add(text, 15, 10);

        Image penaltyImage = new Image("img/icon/warning.png");
        body.add(penaltyImage, 15, 90);
        
        penaltyText = new Label("Loading result...");
        penaltyText.setStylePrimaryName("largetext");
        penaltyText.setWidth("470px");
        body.add(penaltyText, 40, 91);

        nameLabel = new Label(PlayerCharacterCache.getInstance().getName(), false);
        nameLabel.setStylePrimaryName("headstone");
        body.add(nameLabel, 523, 180);
        
        ButtonPanel ok = new ButtonPanel("World Map", new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                ClientManager.setPerspective("play");                
            }
        }, 2);
        body.add(ok, SizeConstants.BUTTON_LEFT, SizeConstants.BUTTON_TOP);
    }
    
    public static void show() {
        final DeathPanel panel = (DeathPanel)ClientState.getLayout().getPanel("death");

        ClientState.setAdventureStatus(null);
        ClientState.setAdventureId(null);
        ClientState.getLayout().showPanel("death");

        ServiceManager.call("character", "get", new ServiceCallback() {
            @Override
            public void onSuccess(ScriptObject result) {
                int oldXp = PlayerCharacterCache.getInstance().getXp();
                int newXp = result.getInt("xp");
                panel.penaltyText.setText("You've lost " + (oldXp - newXp) + " experience.");
                AudioManager.play("playerdeath");
                ClientState.setPlayerCharacter(result);
                //ChatService.refreshNow();
            }
        });
    }

}
