package restless.realms.client.bestiary;

import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.action.ActionIcon;
import restless.realms.client.combat.MobPanel;
import restless.realms.client.pvp.PvpDefensesPanel;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.ButtonPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class MobDisplayPanel extends Composite {
    private static final int NUM_KILLS = 75;

    private AbsolutePanel body;

    private String id;
    
    private MobPanel mobPanel;
    private Label level;
    private Label hitpoints;
    private Label mana;
    private Label numKilled;
    private int upkeep;
    private Label upkeepLabel;
    private HorizontalPanel skills;

    private Label hireLabel;
    private ButtonPanel hireButton;
    private ButtonPanel fireButton;
    
    public MobDisplayPanel() {
        body = new AbsolutePanel();

        id = "";
        
        mobPanel = new MobPanel(-1);
        mobPanel.setVisible(false);
        body.add(mobPanel, 55, -25);
        
        level = new Label();
        body.add(level, 334, 144);

        hitpoints = new Label();
        body.add(hitpoints, 334, 18);

        mana = new Label();
        body.add(mana, 334, 61);
        
        numKilled = new Label();
        body.add(numKilled, 334, 102);
        
        upkeepLabel = new Label();
        body.add(upkeepLabel, 334, 186);
        
        skills = new HorizontalPanel();
        body.add(skills, 304, 239);

        hireLabel = new Label();
        hireLabel.setStylePrimaryName("hire");
        hireLabel.setVisible(false);
        body.add(hireLabel, 40, 257);
        
        hireButton = new ButtonPanel("Recruit", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(Window.confirm("Are you sure you wish to hire this mob for " + (upkeep / 2) + " gold?")) {
                    ServiceManager.call("pvp", "setbodyguard", new ServiceCallback() {
                        @Override
                        public void onSuccess(ScriptObject result) {
                            if(result.hasKey("currency")) {
                                int newCurrency = result.getInt("currency");
                                ClientState.setCurrency(newCurrency);
                            }
                            PvpDefensesPanel.show();
                        }
                    }, "type", id);
                }
            }
        }, 2);
        hireButton.setVisible(false);
        body.add(hireButton, 80, 249);
        
        fireButton = new ButtonPanel("Dismiss", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ServiceManager.call("pvp", "clearbodyguard", new ServiceCallback() {
                    @Override
                    public void onSuccess(ScriptObject result) {
                        fireButton.setVisible(false);
                        hireButton.setVisible(true);
                    }
                }, "type", id);
            }
        }, 2);
        fireButton.setVisible(false);
        body.add(fireButton, 80, 249);
        
        initWidget(body);
    }

    public void setMob(ScriptObject mob, int numKilledInt, boolean hired) {
        this.id = mob.get("id");
        this.mobPanel.setMob(mob);
        
        level.setText("Level " + mob.get("level"));
        hitpoints.setText("Health: " + mob.get("minHitpoints") + "-" + mob.get("maxHitpoints"));
        mana.setText("Mana: " + mob.get("minMana") + "-" + mob.get("maxMana"));
        numKilled.setText("Killed: " + numKilledInt);
        JsArray<ScriptObject> skillsArray = mob.getArray("skills");
        for(int i = 0; i < skillsArray.length(); i++) {
            ScriptObject skill = skillsArray.get(i);
            ActionIcon icon = new ActionIcon();
            skills.add(icon);
            skills.setCellWidth(icon, "47px");
            icon.show("skill", skill, true);
        }
        
        if(mob.getBoolean("boss")) {
            upkeepLabel.setText("Upkeep: N/A");
            hireLabel.setText("Bosses may not be recruited.");
            hireButton.setVisible(false);
            fireButton.setVisible(false);
            hireLabel.setVisible(true);
        } else {
            upkeep = mob.getInt("upkeepCost");
            upkeepLabel.setText("Upkeep: " + upkeep + " gold");
            if(numKilledInt < NUM_KILLS) {
                hireButton.setVisible(false);
                fireButton.setVisible(false);
                hireLabel.setVisible(true);
                int i = NUM_KILLS - numKilledInt;
                hireLabel.setText(i + " kills remaining to recruit.");
            } else {
                hireButton.setVisible(!hired);
                fireButton.setVisible(hired);
                hireLabel.setVisible(false);
            }
        }
    }
    
    public void clear() {
        mobPanel.setMob(null);
        level.setText("");
        hitpoints.setText("");
        mana.setText("");
        numKilled.setText("");
        upkeepLabel.setText("");
        skills.clear();
        hireButton.setVisible(false);
        fireButton.setVisible(false);
        hireLabel.setVisible(false);
    }
}
