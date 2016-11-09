package restless.realms.client.room;

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
import com.google.gwt.user.client.ui.Label;

public class LevelUpPanel extends WindowPanel<AbsolutePanel> {
    private ButtonPanel okButton;
    private Runnable onClose;
    
    private Label hpGainLabel = new Label("", false);
    private Label manaGainLabel = new Label("", false);

    public LevelUpPanel() {
        super("levelup", new AbsolutePanel(), "Level Up!", null);
        
        body.add(hpGainLabel, 25, 133);
        body.add(manaGainLabel, 25, 153);
        
        okButton = new ButtonPanel("OK", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onClose.run();
            }
        }, 1);
        body.add(okButton, SizeConstants.BUTTON_LEFT, SizeConstants.BUTTON_TOP);
    }

    public static void show(Runnable onClose) {
        final LevelUpPanel panel = (LevelUpPanel)ClientState.getLayout().getPanel("levelup");
        panel.onClose = onClose;
        ClientState.getLayout().showPanel("levelup");
        
        ServiceManager.call("character", "get", new ServiceCallback() {
            @Override
            public void onSuccess(ScriptObject result) {
                ClientState.setPlayerCharacter(result);
                PlayerCharacterCache pcc = PlayerCharacterCache.getInstance();
                ScriptObject profession = pcc.getProfession();
                panel.hpGainLabel.setText(profession.get("hitpointsPerLevel") + " hitpoints");
                panel.manaGainLabel.setText(profession.get("manaPerLevel") + " mana");
//                ChatService.refreshNow();
                AudioManager.play("levelup");
//                FacebookManager.streamPublish(
//                    "levelup", 
//                    "Share your achievement!", 
//                    "Yeah, that's right. I leveled up and you're way behind. You should join me and catch up!", 
//                    "Level Up!", 
//                    pcc.getName() + " has reached level " + pcc.getLevel() + " in Restless Realms!"
//                );
            }
        });
    }
}
