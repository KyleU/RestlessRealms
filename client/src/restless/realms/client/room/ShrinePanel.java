package restless.realms.client.room;

import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.audio.AudioManager;
import restless.realms.client.layout.SizeConstants;
import restless.realms.client.util.CommonEventHandlers;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;

public class ShrinePanel extends WindowPanel<AbsolutePanel> {
    private ServiceCallback completionCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            ClientState.setPlayerCharacter(result);
            text.setText("As you drink vigorously from the fountain your wounds rapidly disappear and your mind becomes clear. " +
            		"The vibrant ametheyst color fades as you finish and the fountain appears to lose its magical properties." +
            		"Your health and mana have been restored.");
            completeButton.setVisible(false);
            cancelButton.setVisible(false);
            leaveButton.setVisible(true);
            AudioManager.play("healing");
        }
    };

    private HTML text;
    private ButtonPanel completeButton;
    private ButtonPanel cancelButton;
    private ButtonPanel leaveButton;

    public ShrinePanel() {
        super("shrine", new AbsolutePanel(), "Shrine", null);
        
        text = new HTML();
        text.setWidth("500px");
        body.add(text, 5, 5);
        
        ClickHandler completeClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ServiceManager.call("room", "shrine", completionCallback);
            }
        };
        completeButton = new ButtonPanel("Drink", completeClickHandler, 2);
        body.add(completeButton, SizeConstants.BUTTON_LEFT, SizeConstants.BUTTON_TOP);

        cancelButton = new ButtonPanel("Not Now", CommonEventHandlers.CLICK_WINDOW_CLOSE, 2);
        body.add(cancelButton, SizeConstants.BUTTON_LEFT + 135, SizeConstants.BUTTON_TOP);

        leaveButton = new ButtonPanel("Leave", CommonEventHandlers.CLICK_WINDOW_CLOSE, 2);
        body.add(leaveButton, SizeConstants.BUTTON_LEFT, SizeConstants.BUTTON_TOP);
    }
    
    public void show() {
        text.setText(
                "You enter a room glowing with a purple light. " +
                "The source of the light comes from a stone fountain that appears to be centuries old. " +
                "The base of the fountain is overflowing with a thick purple liquid. " +
                "Intuition tells you the contents may have beneficial results when consumed. " +
                "You may drink from the fountain now, or return later when you need it.");
        completeButton.setVisible(true);
        cancelButton.setVisible(true);
        leaveButton.setVisible(false);
        ClientState.getLayout().showPanel("shrine");
    }
    
}
