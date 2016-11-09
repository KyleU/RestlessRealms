package restless.realms.client.pvp;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.console.command.AdminCommand;
import restless.realms.client.layout.PanelLocation;
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

public class PvpIntroPanel extends WindowPanel<AbsolutePanel> {
    private static PvpIntroPanel instance;
    
    private final Label allyName;
    private final Label enemyName;

    private final Label multiplier;
    private final Label duelScore;
    private final Label gold;
    private final Label tokens;
    private final Label xp;

    private final Label duelScoreOpponent;
    private final Label goldOpponent;
    private final Label tokensOpponent;
    private final Label xpOpponent;

    private final ServiceCallback infoCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            show(result);
        }
    };
    
    private final ServiceCallback pvpCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            ClientState.setAdventurePoints(PlayerCharacterCache.getInstance().getAdventurePoints() - 1);
            
            int combatId = result.getInt("combatId");
            assert combatId > 0;
            
            int negAdventureId = (-combatId);
            ClientState.setAdventureId(negAdventureId);
            ClientManager.setPerspective("combat");
        }
        
        @Override
        public void onFailure(String code, String message) {
            if(code.equals("InsufficientFundsException")) {
                ClientState.getLayout().showPanel("noadventures");
            } else {
                super.onFailure(code, message);
            }
        }
    };

    private PvpIntroPanel() {
        super("pvpintro", new AbsolutePanel(), "Duel", null);
        
        allyName = new Label(PlayerCharacterCache.getInstance().getName());
        allyName.setStylePrimaryName("name");
        body.add(allyName, 412, 155);
        
        enemyName = new Label();
        enemyName.setStylePrimaryName("name");
        body.add(enemyName, 585, 155);
        
        multiplier = new Label();
        body.add(multiplier, 5, 5);
        
        duelScore = new Label();
        duelScore.setStylePrimaryName("reward");
        body.add(duelScore, 59, 58);
        
        duelScoreOpponent = new Label();
        duelScoreOpponent.setStylePrimaryName("reward");
        body.add(duelScoreOpponent, 231, 58);
        
        gold = new Label();
        gold.setStylePrimaryName("reward");
        body.add(gold, 59, 112);
        
        goldOpponent = new Label();
        goldOpponent.setStylePrimaryName("reward");
        body.add(goldOpponent, 231, 112);
        
        tokens = new Label();
        tokens.setStylePrimaryName("reward");
        body.add(tokens, 59, 166);
        
        tokensOpponent = new Label();
        tokensOpponent.setStylePrimaryName("reward");
        body.add(tokensOpponent, 231, 166);
        
        xp = new Label();
        xp.setStylePrimaryName("reward");
        body.add(xp, 59, 220);
        
        xpOpponent = new Label();
        xpOpponent.setStylePrimaryName("reward");
        body.add(xpOpponent, 231, 220);
        
        ClickHandler fightHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ServiceManager.call("pvp", "start", pvpCallback, "enemy", enemyName.getText());
            }
        };
        ButtonPanel fightButton = new ButtonPanel("Fight", fightHandler, 1);
        body.add(fightButton, SizeConstants.BUTTON_LEFT, SizeConstants.BUTTON_TOP);

        ClickHandler exitHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ClientState.getLayout().showPanel("pcinfo");
            }
        };

        ButtonPanel exitButton = new ButtonPanel("Cancel", exitHandler, 1);
        body.add(exitButton, SizeConstants.BUTTON_LEFT + 95, SizeConstants.BUTTON_TOP);

        addExitButton(exitHandler);
    }
    
    public void show(ScriptObject result) {
        allyName.setText(result.get("allyName"));
        enemyName.setText(result.get("enemyName"));
        ScriptObject rewards = result.getObject("rewards");
        if(AdminCommand.isAdmin()) {
            multiplier.setText("This fight will cost one Adventure Point (" + rewards.get("multiplier") + ").");
        } else {
            multiplier.setText("This fight will cost one Adventure Point.");
        }
        duelScore.setText(rewards.get("duelscore"));
        duelScoreOpponent.setText(Integer.toString(-rewards.getInt("duelscore")));
        gold.setText(rewards.get("gold"));
        goldOpponent.setText("0");
        tokens.setText(rewards.get("tokens"));
        tokensOpponent.setText("0");
        xp.setText(rewards.get("xp"));
        xpOpponent.setText(rewards.get("penalty"));

        ClientState.getLayout().showPanel("pvpintro");
    }
    
    public static void show(String enemy) {
        if(instance == null) {
            instance = new PvpIntroPanel();
            ClientState.getLayout().addPanel(PanelLocation.MAIN, "pvpintro", instance);
        }
        ServiceManager.call("pvp", "pvpinfo", instance.infoCallback, "name", enemy);
    }
}
