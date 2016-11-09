package restless.realms.client.pvp;

import java.util.ArrayList;
import java.util.List;

import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.action.ActionIcon;
import restless.realms.client.belt.Quickslot;
import restless.realms.client.bestiary.BestiaryPanel;
import restless.realms.client.dragdrop.DragDropManager;
import restless.realms.client.layout.PanelLocation;
import restless.realms.client.layout.SizeConstants;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class PvpDefensesPanel extends WindowPanel<AbsolutePanel> {
    private static final String DEFAULT_MOB_IMAGE = "<img src=\"img/mob/bodyguard.png\" width=\"93\" height=\"108\" />";
    
    private static PvpDefensesPanel instance;
    
    private Quickslot[] defenses = new Quickslot[8];
    
    private HTML sequenceLabel;
    
    private String[] bodyguards = new String[3];
    private HTML[] bodyguardImages = new HTML[3];
    private Label[] bodyguardNames = new Label[3];
    private Label[] bodyguardLevels = new Label[3];
    private ButtonPanel[] bodyguardButtons = new ButtonPanel[3];
    
    private Label totalUpkeep;
    
    private ServiceCallback loadCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            JsArrayInteger skillIds = result.getIntArray("skills");
            setDefenses(skillIds);
            JsArrayString enemies = result.getStringArray("enemies");
            JsArrayString names = result.getStringArray("names");
            JsArrayInteger levels = result.getIntArray("levels");
            JsArrayInteger upkeeps = result.getIntArray("upkeeps");
            setEnemies(enemies, names, levels, upkeeps);
            if(result.hasKey("currency")) {
                int newCurrency = result.getInt("currency");
                ClientState.setCurrency(newCurrency);
            }
            ClientState.getLayout().showPanel("defenses");
        }
    };
    
    private ServiceCallback saveCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            ClientState.getLayout().showPanel("pcinfo");
        }
    };
    
    private PvpDefensesPanel() {
        super("defenses", new AbsolutePanel(), "Duel Defenses", null);

        Label explanation = new Label("The Defense Belt allows you to customize the skills your character will use automatically when defending in a Duel and the order in which they are used. The sequence below illustrates the order skills will be used. Bodyguards can be recruited or dismissed at any time from the Bestiary. They will protect you when defending in a duel, but require an upkeep cost. The total cost is shown below.");
        body.add(explanation, 5, 5);
        
        for(int i = 0; i < defenses.length; i++) {
            defenses[i] = new Quickslot(i == 7 ? -3 : -2);
            if(i != 7) {
                DragDropManager.registerPvpDefenseDroppable(defenses[i]);
            }
            body.add(defenses[i], 5 + (48 * i), 108);
        }
        
        sequenceLabel = new HTML();
        sequenceLabel.setWidth("380px");
        body.add(sequenceLabel, 5, 190);
        
        totalUpkeep = new Label();
        totalUpkeep.setStylePrimaryName("totalupkeep");
        totalUpkeep.setTitle("Every eight hours, the total upkeep cost is removed from your gold.");
        body.add(totalUpkeep, 523, 83);
        
        for(int i = 0; i < bodyguardNames.length; i++) {
            final int index = i;
            ClickHandler clickHandler = new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    BestiaryPanel.show(bodyguards[index]);
                }
            };
            bodyguardImages[i] = new HTML(DEFAULT_MOB_IMAGE);
            bodyguardImages[i].setStylePrimaryName("bodyguardimage");
            bodyguardImages[i].addClickHandler(clickHandler);
            body.add(bodyguardImages[i], 418 + (105 * i), 108);
            bodyguardNames[i] = new Label();
            bodyguardNames[i].setStylePrimaryName("bodyguardname");
            body.add(bodyguardNames[i], 418 + (105 * i), 222);
            bodyguardLevels[i] = new Label();
            bodyguardLevels[i].setStylePrimaryName("bodyguardlevel");
            body.add(bodyguardLevels[i], 418 + (105 * i), 244);
            bodyguardButtons[i] = new ButtonPanel("", clickHandler, 1);
            body.add(bodyguardButtons[i], 418 + (105 * i) + 2, SizeConstants.BUTTON_TOP);
        }
        
        ButtonPanel ok = new ButtonPanel("OK", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                save();
            }
        }, 1);
        body.add(ok, SizeConstants.BUTTON_LEFT, SizeConstants.BUTTON_TOP);
        
        ButtonPanel cancel = new ButtonPanel("Cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ClientState.getLayout().showPanel("pcinfo");
            }
        }, 1);
        body.add(cancel, SizeConstants.BUTTON_LEFT + 95, SizeConstants.BUTTON_TOP);
    }

    public static void show() {
        if(instance == null) {
            instance = new PvpDefensesPanel();
            ClientState.getLayout().addPanel(PanelLocation.MAIN, "defenses", instance);
        }
        ServiceManager.call("pvp", "defenses", instance.loadCallback);
    }
    
    public static void update() {
        List<ScriptObject> skills = new ArrayList<ScriptObject>();
        for(int i = 0; i < instance.defenses.length; i++) {
            Quickslot quickslot = instance.defenses[i];
            ScriptObject action = quickslot.getAction();
            if(action != null) {
                skills.add(action);
            }
        }
        instance.updateSequence(skills);
    }

    private void save() {
        String ids = "";
        for(Quickslot quickslot : defenses) {
            if(quickslot.getAction() != null) {
                if(ids.length() > 0) {
                    ids += ",";
                }
                ids += quickslot.getAction().getInt("id");
            }
        }
        ServiceManager.call("pvp", "setdefenses", saveCallback, "defenses", ids);
    }
    
    private void setDefenses(JsArrayInteger skillIds) {
        int skillsOffset = defenses.length - skillIds.length();
        for(int i = 0; i < defenses.length; i++) {
            defenses[i].clear();
        }
        List<ScriptObject> skills = new ArrayList<ScriptObject>();
        for(int i = 0; i < skillIds.length(); i++) {
            int skillId = skillIds.get(i);
            ScriptObject skill = PlayerCharacterCache.getInstance().getSkill(skillId);
            assert skill != null;
            skills.add(skill);
            Quickslot quickslot = defenses[i + skillsOffset];
            ActionIcon actionIcon = new ActionIcon();
            actionIcon.show("skill", skill, true);
            quickslot.setActionIcon(actionIcon);
        }
        updateSequence(skills);
    }
    
    private void setEnemies(JsArrayString enemies, JsArrayString names, JsArrayInteger levels, JsArrayInteger upkeeps) {
        int totalUpkeepAmount = 0;
        for(int i = 0; i < 3; i++) {
            if(enemies.length() > i) {
                bodyguards[i] = enemies.get(i);
                bodyguardImages[i].setHTML("<img src=\"img/mob/" + bodyguards[i] + ".png\" width=\"93\" height=\"108\" />");
                bodyguardNames[i].setText(names.get(i));
                bodyguardLevels[i].setText("Level " + levels.get(i));
                bodyguardButtons[i].setText("Dismiss");
                totalUpkeepAmount += upkeeps.get(i);
            } else {
                bodyguards[i] = null;
                bodyguardImages[i].setHTML(DEFAULT_MOB_IMAGE);
                bodyguardNames[i].setText("");
                bodyguardLevels[i].setText("");
                bodyguardButtons[i].setText("Recruit");
            }
        }
        totalUpkeep.setText("Total Upkeep: " + totalUpkeepAmount);
    }


    private void updateSequence(List<ScriptObject> skills) {
        List<ScriptObject> skillSequence = new ArrayList<ScriptObject>();
        for(int i = 0; i < 12; i++) {
            ScriptObject skillToAdd = null;
            for(ScriptObject skill : skills) {
                int warmup = skill.getInt("warmup");
                int cooldown = skill.getInt("cooldown");
                if(warmup <= i) {
                    int startRow = i - cooldown;
                    if(startRow < 0) {
                        startRow = 0;
                    }
                    skillToAdd = skill; 
                    for(int j = startRow; j < i; j++) {
                        if(skillSequence.get(j).getInt("id") == skill.getInt("id")) {
                            skillToAdd = null;
                            break;
                        };
                    }
                    if(skillToAdd != null) {
                        break;
                    }
                }
            }
            assert skillToAdd != null;
            skillSequence.add(skillToAdd);
        }
        
        String sequenceText = "<strong>Sequence:</strong> ";
        for(int i = 0; i < skillSequence.size(); i++) {
            ScriptObject skill = skillSequence.get(i);
            sequenceText += skill.get("name");
            if(i + 1 < skillSequence.size()) {
                sequenceText += ", ";
            }
        }
        sequenceText += "...";
        sequenceLabel.setHTML(sequenceText);
    }
}