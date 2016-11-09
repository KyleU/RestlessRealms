package restless.realms.client.combat;

import restless.realms.client.ClientState;
import restless.realms.client.audio.AudioManager;
import restless.realms.client.layout.SizeConstants;
import restless.realms.client.util.CommonEventHandlers;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.user.client.ui.AbsolutePanel;

public class CombatIntroPanel extends WindowPanel<AbsolutePanel> {
    public CombatIntroPanel() {
        super("combatintro", new AbsolutePanel(), "", null);
        ButtonPanel ok = new ButtonPanel("Fight", CommonEventHandlers.CLICK_WINDOW_CLOSE, 1);
        body.add(ok, SizeConstants.BUTTON_LEFT, SizeConstants.BUTTON_TOP);
    }
    
    public void show(String introKey) {
        this.body.getElement().getStyle().setProperty("backgroundImage", "url(img/intro/" + introKey + ".png)");
        ClientState.getLayout().showPanel("combatintro");
        if("tutorial".equals(ClientState.getAdventureType())) {
            this.setWindowTitle("Tutorial");
        } else {
            this.setWindowTitle("Boss Fight");
            AudioManager.play("boss");
        }
    }
}
