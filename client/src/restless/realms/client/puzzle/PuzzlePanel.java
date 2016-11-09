package restless.realms.client.puzzle;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.util.AuditManager;
import restless.realms.client.util.CommonEventHandlers;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class PuzzlePanel extends WindowPanel<AbsolutePanel> {
    public PuzzlePanel() {
        super("puzzle", new AbsolutePanel(), "Puzzle", null);
        addExitButton(CommonEventHandlers.CLICK_WINDOW_CLOSE);
        
        ClickHandler winClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ServiceManager.call("room", "solvepuzzle", new ServiceCallback() {
                    @Override
                    public void onSuccess(ScriptObject result) {
                        AuditManager.audit("puzzle", "pass", PlayerCharacterCache.getInstance().getName(), 1);
                        ConsoleUtils.help("You're a winner! Don't worry, real puzzles are coming soon.");
                        ClientManager.send(MessageType.WINDOW_CLOSE);
                    }
                }, "solution", "s");
            }
        };
        ClickHandler loseClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AuditManager.audit("puzzle", "fail", PlayerCharacterCache.getInstance().getName(), 1);
                ConsoleUtils.help("You're a big loser! Don't worry, real puzzles are coming soon.");
                ClientManager.send(MessageType.WINDOW_CLOSE);
            }
        };
        ButtonPanel winButton = new ButtonPanel("Win Puzzle", winClickHandler , 2);
        body.add(winButton, 100, 100);
        ButtonPanel loseButton = new ButtonPanel("Lose Puzzle", loseClickHandler , 2);
        body.add(loseButton, 270, 100);
    }

    public void show(ScriptObject room) {
        ClientState.getLayout().showPanel("puzzle");
    }
}
