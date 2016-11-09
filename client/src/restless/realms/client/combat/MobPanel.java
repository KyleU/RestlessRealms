package restless.realms.client.combat;

import java.util.List;

import restless.realms.client.ClientState;
import restless.realms.client.animation.EffectResultAnimation;
import restless.realms.client.animation.ShakeAnimation;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.widget.CompositeImage;
import restless.realms.client.widget.ProgressBar;
import restless.realms.client.widget.VerticalIconPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class MobPanel extends Composite {
    private final int index;
    private final AbsolutePanel panel;
    private final Label nameLabel;
    private final CompositeImage mobImage;
    private final VerticalIconPanel damageMods;
    private final VerticalIconPanel recurringEffects;
    private ProgressBar hitpoints;    
    private ProgressBar mana;
    
    public MobPanel(int index) {
        this.index = index;
        panel = new AbsolutePanel();
        initWidget(panel);
        panel.setStylePrimaryName("mobpanel");
        
        nameLabel = new Label();
        nameLabel.setStylePrimaryName("name");
        panel.add(nameLabel, 0, 10);

        mobImage = new CompositeImage(6, "img/paperdoll/male/background/empty.png", 185, 215);
        if(index > -1) {
            mobImage.setStylePrimaryName("portrait");
        }
        panel.add(mobImage, 5, 35);
        mobImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(MobPanel.this.index > -1) {
                    if(hitpoints.getValue() == 0) {
                        ConsoleUtils.error("You cannot target the dead.");
                    } else {
                        CombatPanel cp = (CombatPanel)ClientState.getLayout().getPanel("combat");
                        cp.setTargetSelectionIndex(MobPanel.this.index, true);
                    }
                }
            }
        });

        damageMods = new VerticalIconPanel("img/icon/damage.png");
        panel.add(damageMods, 5, 70);
        
        recurringEffects = new VerticalIconPanel("img/icon/damage.png");
        panel.add(recurringEffects, 150, 38);
        
        if(index > -1) {
            hitpoints = new ProgressBar(117, 0, null, false);
            panel.add(hitpoints, 31, 258);
            mana = new ProgressBar(117, 1, null, false);
            panel.add(mana, 31, 277);
            initMouseHandlers();
        }
    }
    
    public void setMob(ScriptObject mob) {
        if(mob != null) {
            if(mob.hasKey("name")) {
                nameLabel.setText(mob.get("name"));
            }
            if(index > -1) {
                hitpoints.setMaxValue(mob.getInt("maxHitpoints"));
                hitpoints.setValue(mob.getInt("hitpoints"), true);
                mana.setMaxValue(mob.getInt("maxMana"));
                mana.setValue(mob.getInt("mana"), true);
            }
            
            if(mob.hasKey("image")) {
                String image = mob.get("image");
                mobImage.setVisible(true);
                if(image.startsWith("!")) {
                    String[] images = image.substring(1).split(",");
                    assert images.length == 6 : images.toString();
                    for(int i = 0; i < images.length; i++) {
                        mobImage.setUrl(i, "img/paperdoll/" + images[i] + ".png");
                    }
                } else {
                    mobImage.setUrl(0, null);
                    mobImage.setUrl(1, null);
                    mobImage.setUrl(2, null);
                    mobImage.setUrl(3, null);
                    mobImage.setUrl(4, null);
                    mobImage.setUrl(5, "img/mob/" + image + ".png");
                }
            }
            
            if(mob.hasKey("immunities") || mob.hasKey("resistances") || mob.hasKey("weaknesses")) {
                damageMods.clear();
            }
            if(mob.hasKey("immunities")) {
                String immunities = mob.get("immunities");
                addDamageModIcons(0, immunities.split(","));
            }
            if(mob.hasKey("resistances")) {
                String resistances = mob.get("resistances");
                addDamageModIcons(1, resistances.split(","));
            }
            if(mob.hasKey("weaknesses")) {
                String weaknesses = mob.get("weaknesses");
                addDamageModIcons(2, weaknesses.split(","));
            }
            
            if(index > -1) {
                if(hitpoints.getValue() == 0) {
                    damageMods.clear();
                    recurringEffects.clear();
                    mobImage.setUrl(0, null);
                    mobImage.setUrl(1, null);
                    mobImage.setUrl(2, null);
                    mobImage.setUrl(3, null);
                    mobImage.setUrl(4, null);
                    mobImage.setUrl(5, "img/mob/" + "dead" + ".png");
                }
            }
            panel.setVisible(true);
        } else {
            panel.setVisible(false);
            nameLabel.setText("");
            if(index > -1) {
                hitpoints.setMaxValue(0);
                hitpoints.setValue(0, false);
                mana.setMaxValue(0);
                mana.setValue(0, false);
            }
            damageMods.clear();
            recurringEffects.clear();
            mobImage.setUrl(0, null);
            mobImage.setUrl(1, null);
            mobImage.setUrl(2, null);
            mobImage.setUrl(3, null);
            mobImage.setUrl(4, null);
            mobImage.setUrl(5, null);
        }
    }

    public void apply(List<ScriptObject> effectResults) {
        recurringEffects.clear();
        boolean showShakeAnimation = false;
        for(int i = 0; i < effectResults.size(); i++) {
            ScriptObject effectResult = effectResults.get(i);
            String type = effectResult.get("type");
            if(!showShakeAnimation && !type.equals("DEATH") && !type.equals("HEALING") && !type.equals("REPLENISH")) {
                showShakeAnimation = true;
            }
            if(showShakeAnimation && type.equals("DEATH")) {
                showShakeAnimation = false;
            }
            int additionalRounds = effectResult.getInt("additionalRounds");
            if(additionalRounds > 0) {
                addRecurringEffectsIcon(type, additionalRounds);
            }
            
            final EffectResultAnimation animation = new EffectResultAnimation(panel.getElement(), 150, 0, effectResult);
            if(i == 0) {
                animation.run();
            } else {
                Timer t = new Timer() {
                    @Override
                    public void run() {
                        animation.run();
                    }
                };
                t.schedule(500 * i);
            }
        }
        if(showShakeAnimation) {
            ShakeAnimation shakeAnimation = new ShakeAnimation(mobImage.getElement(), 2, 5);
            shakeAnimation.run(250);
        }
    }

    public ProgressBar getHitpoints() {
        return hitpoints;
    }

    public ProgressBar getMana() {
        return mana;
    }
    
    public int getIndex() {
        return index;
    }
    
    private void initMouseHandlers() {
        MouseOverHandler mouseOverHandler = new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                hitpoints.fadeIn();
                mana.fadeIn();
            }
        };
        this.addDomHandler(mouseOverHandler, MouseOverEvent.getType());
        
        MouseOutHandler mouseOutHandler = new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                hitpoints.fadeOut();
                mana.fadeOut();
            }
        };
        this.addDomHandler(mouseOutHandler, MouseOutEvent.getType());
    }

    private void addDamageModIcons(int x, String[] effectTypes) {
        if(effectTypes != null) {
            for(int i = 0; i < effectTypes.length; i++) {
                String effectType = effectTypes[i].toLowerCase();
                if(!"null".equals(effectType)) {
                    addDamageModIcon(x, effectType);
                }
            }
        }
    }

    private void addDamageModIcon(int x, String effectType) {
        String message = "This target is ";
        if(x == 0) {
            message += "immune to ";
        } else if(x == 1) {
            message += "resistant against ";
        } else if(x == 2) {
            message += "weak against ";
        } else {
            assert false;
        }
        message += effectType;
        int y = 0;
        if("physical".equals(effectType)) {
            y = 1;
            message += " damage.";
        } else if("fire".equals(effectType)) {
            y = 2;
            message += " damage.";
        } else if("ice".equals(effectType)) {
            y = 3;
            message += " damage.";
        } else if("electric".equals(effectType)) {
            y = 4;
            message += " damage.";
        } else if("healing".equals(effectType)) {
            y = 5;
            message += " effects.";
        } else if("drain".equals(effectType)) {
            y = 6;
            message += " effects.";
        } else if ("replenish".equals(message)) {
            y = 7;
            message += " effects.";
        } else if ("stun".equals(effectType)) {
            y = 8;
            message += " effects.";
        } else if("death".equals(effectType)) {
            y = 9;
            message += " effects.";
        } else {
            assert false : effectType;
        }

        damageMods.addIcon(x, y, message);
    }

    private void addRecurringEffectsIcon(String effectType, int additionalRounds) {
        String message = "This target is ";
        int y = 0;
        if("PHYSICAL".equals(effectType)) {
            y = 1;
            message += "bleeding";
        } else if("FIRE".equals(effectType)) {
            y = 2;
            message += "burning";
        } else if("ICE".equals(effectType)) {
            y = 3;
            message += "freezing";
        } else if("ELECTRIC".equals(effectType)) {
            y = 4;
            message += "shocked";
        } else if("HEALING".equals(effectType)) {
            y = 5;
            message += "regenerating";
        } else if("DRAIN".equals(effectType)) {
            y = 6;
            message += "drained";
        } else if ("REPLENISH".equals(message)) {
            y = 7;
            message += "replenishing";
        } else if ("STUN".equals(effectType)) {
            y = 8;
            message += "stunned";
        } else {
            assert false : effectType;
        }
        
        message += " for " + additionalRounds + " additional round";
        if(additionalRounds != 1) {
            message += "s";
        }
        message += ".";
        
        recurringEffects.addIcon(3, y, message);
    }
}