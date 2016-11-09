package restless.realms.client.playercharacter;

import java.util.HashMap;
import java.util.Map;

import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.bestiary.BestiaryPanel;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.layout.PanelLocation;
import restless.realms.client.mail.MailPanel;
import restless.realms.client.pvp.PvpDefensesPanel;
import restless.realms.client.pvp.PvpIntroPanel;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;

public class PlayerCharacterInfoPanel extends WindowPanel<AbsolutePanel> {
    private static PlayerCharacterInfoPanel instance;  
    
    private String playerName;
    
    private PlayerCharacterDisplay playerCharacterDisplay;
    private Map<String, String> statisticNames;
    private Map<String, Label> statisticLabels;

    private ButtonPanel mailButton;
    private ButtonPanel pvpButton;

    private ButtonPanel defensesButton;
    private ButtonPanel bestiaryButton;

    private String lastObservedPanel = null;
    
    private ServiceCallback callback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            JsArrayString keys = result.keys();
            for(int i = 0; i < keys.length(); i++) {
                String key = keys.get(i);
                if("playerCharacter".equals(key)) {
                    ScriptObject playerCharacter = result.getObject(key);
                    playerCharacterDisplay.clear();
                    playerCharacterDisplay.setPlayerCharacter(playerCharacter);
                    playerName = playerCharacter.get("name");
                    setWindowTitle(playerName);
                    boolean self = playerName.equals(PlayerCharacterCache.getInstance().getName());
                    defensesButton.setVisible(self);
                    bestiaryButton.setVisible(self);
                    mailButton.setVisible(!self);
                    pvpButton.setVisible(!self);
                } else if("equipment".equals(key)) {
                    JsArray<ScriptObject> equipmentArray = result.getArray("equipment");
                    for(int j = 0; j < equipmentArray.length(); j++) {
                        ScriptObject equipment = equipmentArray.get(j);
                        playerCharacterDisplay.setEquipment(j, equipment);
                    }
                } else {
                    if(statisticLabels.containsKey(key)) {
                        String name = statisticNames.get(key);
                        String value = NumberFormat.getDecimalFormat().format(result.getInt(key));
                        statisticLabels.get(key).setText(name + ": " + value);
                    } else {
                        ConsoleUtils.error("Unhandled statistic \"" + key + "\" (" + result.get(key) + ").");
                    }
                }
            }
            
            String mainWindowPanelKey = ClientState.getLayout().getMainWindowPanelKey();
            if(!"pcinfo".equals(mainWindowPanelKey)) {
                lastObservedPanel = mainWindowPanelKey;
            }
            
            ClientState.getLayout().showPanel("pcinfo");
        }
    };
    
    private PlayerCharacterInfoPanel() {
        super("pcinfo", new AbsolutePanel(), "", null);

        playerCharacterDisplay = new PlayerCharacterDisplay(false);
        body.add(playerCharacterDisplay, 16, 13);

        defensesButton = new ButtonPanel("Duel Defenses", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                PvpDefensesPanel.show();
            }
        }, 2);
        body.add(defensesButton, 20, 250);
        
        bestiaryButton = new ButtonPanel("Bestiary", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                BestiaryPanel.show(null);
            }
        }, 1);
        body.add(bestiaryButton, 152, 250);
        
        mailButton = new ButtonPanel("Send Message", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                MailPanel.show("compose", playerName);
            }
        }, 2);
        body.add(mailButton, 20, 250);
        
        pvpButton = new ButtonPanel("Duel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(ClientState.getAdventureId() == null) {
                    PvpIntroPanel.show(playerName);
                    //ConsoleUtils.error("Sorry, duels aren't quite ready yet.");
                } else {
                    ConsoleUtils.error("Finish your current adventure before attempting to duel.");
                }
            }
        }, 1);
        body.add(pvpButton, 152, 250);
        
        statisticNames = new HashMap<String, String>();
        statisticLabels = new HashMap<String, Label>();

        addStatisticLabel("kills", "Kills", 293, 0);
        addStatisticLabel("deaths", "Deaths", 293, 1);
        addStatisticLabel("adventure-complete", "Adventures Completed", 293, 2);
        addStatisticLabel("physical-bonus", "Physical Bonus", 293, 3);
        addStatisticLabel("fire-bonus", "Fire Bonus", 293, 4);
        addStatisticLabel("ice-bonus", "Ice Bonus", 293, 5);
        addStatisticLabel("electric-bonus", "Electric Bonus", 293, 6);

        addStatisticLabel("duelscore", "Duel Score", 540, 0);
        addStatisticLabel("pvp-offensive-win", "Duel Offensive Wins", 540, 1);
        addStatisticLabel("pvp-offensive-loss", "Duel Offensive Losses", 540, 2);
        addStatisticLabel("pvp-defensive-win", "Duel Defensive Wins", 540, 3);
        addStatisticLabel("pvp-defensive-loss", "Duel Defensive Losses", 540, 4);
        addStatisticLabel("damage-dealt", "Damage Dealt", 540, 5);
        addStatisticLabel("damage-taken", "Damage Taken", 540, 6);

        addExitButton(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                assert lastObservedPanel != null;
                ClientState.getLayout().showPanel(lastObservedPanel);
            }
        });
    }
    
    public static void show(String playerName) {
        if(instance == null) {
            instance = new PlayerCharacterInfoPanel();
            ClientState.getLayout().addPanel(PanelLocation.MAIN, "pcinfo", instance);
        }
        ServiceManager.call("pvp", "pcinfo", instance.callback, "name", playerName);
    }

    private void addStatisticLabel(String key, String caption, int left, int row) {
        statisticNames.put(key, caption);
        
        Label statisticLabel = new Label("");
        statisticLabel.setStylePrimaryName("statisticlabel");
        body.add(statisticLabel, left, (row * 41) + 20);
        
        statisticLabels.put(key, statisticLabel);
    }
}
