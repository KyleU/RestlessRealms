package restless.realms.client.playercharacter;

import restless.realms.client.action.ActionIcon;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.widget.CompositeImage;
import restless.realms.client.widget.ProgressBar;
import restless.realms.client.widget.VerticalIconPanel;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class PlayerCharacterDisplay extends Composite {
    public static final int[] PORTRAIT_Z_ORDERS = new int[] {1,3,2,4,5};;

    private final boolean self;
    private String gender;
    private final AbsolutePanel body;

    private Image details;
    private ProgressBar xpProgressBar;

    private Label level;
    private CompositeImage portrait;
    private ActionIcon[] equipment;
    private final VerticalIconPanel recurringEffects;

    public PlayerCharacterDisplay(boolean self) {
        body = new AbsolutePanel();
        body.setStylePrimaryName("pcinfo");
        initWidget(body);

        //body=0,head,legs,chest,accessory,weapon
        portrait = new CompositeImage(6, "img/paperdoll/male/background/empty.png", 185, 215);
        body.add(portrait, 23, 0);

        this.self = self;
        if(self) {
            details = new Image("img/portraits/details.png");
            details.getElement().getStyle().setProperty("cursor", "pointer");
            details.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    PlayerCharacterInfoPanel.show(PlayerCharacterCache.getInstance().getName());
                }
            });
            body.add(details, 60, 86);
            
//          xpProgressBar = new ProgressBar(219, 2, " experience", false);
            xpProgressBar = new ProgressBar(750, 2, " experience", true);
            xpProgressBar.setMaxValue(0);
            RootPanel.get("experiencecontainer").add(xpProgressBar);
            //body.add(xpProgressBar, 6, 200);
        }
        
        level = new Label("", false);
        level.setStylePrimaryName("level");
        body.add(level, 183, 19);
        
        equipment = new ActionIcon[7];
        
        equipment[0] = new ActionIcon();//6
        body.add(equipment[0], 5, 5);

        equipment[1] = new ActionIcon();//4
        body.add(equipment[1], 5, 53);

        equipment[2] = new ActionIcon();//5
        body.add(equipment[2], 5, 101);

        equipment[3] = new ActionIcon();//7
        body.add(equipment[3], 180, 53);

        equipment[4] = new ActionIcon();//8
        body.add(equipment[4], 180, 101);
        
        equipment[5] = new ActionIcon();//9
        body.add(equipment[5], 5, 149);
        
        equipment[6] = new ActionIcon();//10
        body.add(equipment[6], 180, 149);
        
        recurringEffects = new VerticalIconPanel("img/icon/damage.png");
        body.add(recurringEffects, 150, 5);

        clear();
    }

    public void clear() {
        equipment[0].clear(6);
        equipment[1].clear(4);
        equipment[2].clear(5);
        equipment[3].clear(7);
        equipment[4].clear(8);
        equipment[5].clear(9);
        equipment[6].clear(10);
        clearRecurringEffects();
    }

    public void setPlayerCharacter(ScriptObject playerCharacter) {
        assert playerCharacter != null;
        if(playerCharacter.hasKey("level")) {
            int level = playerCharacter.getInt("level");
            if(xpProgressBar != null) {
                //also in PlayerCharacterDao.addXp
                int requiredXp = (level * level * 6000) - 5000;
                xpProgressBar.setMaxValue(requiredXp);
            }
        }
        if(playerCharacter.hasKey("xp")) {
            if(xpProgressBar != null) {
                xpProgressBar.setValue(playerCharacter.getInt("xp"), true);
            }
        }
        if(playerCharacter.hasKey("profession")) {
            assert playerCharacter.hasKey("level");

            String profession = playerCharacter.get("profession");
            int level = playerCharacter.getInt("level");
            this.level.setText(String.valueOf(level));
            this.level.setTitle("Level " + level + " " + profession + ".");

            if(self) {
                String url = "url(img/portraits/" + profession + "-bg.png)";
                body.getElement().getStyle().setProperty("backgroundImage", url);
            }

        }
        if(playerCharacter.hasKey("gender")) {
            setGender(playerCharacter.get("gender").toLowerCase());
        }
    }
    
    public void setEquipment(int index, ScriptObject action) {
        ActionIcon eq = equipment[index];
        if(action == null) {
            eq.setTitle("Empty " + (index > 4 ? "perk" : "item") + " slot.");
            if(index <= 4) {
                String url = "img/paperdoll/" + gender + "/background/empty.png";
                portrait.setUrl(PORTRAIT_Z_ORDERS[index], url);
            }
        } else {
            eq.setTitle(null);
            eq.show(index > 4 ? "perk" : "item", action, true);
            if(index <= 4) {
                ScriptObject icon = action.getObject("icon");
                String filename = icon.get("x") + "-" + icon.get("y");
                String actionType = action.get("type").toLowerCase();
                String url = "img/paperdoll/" + gender + "/" + actionType + "/" + filename  + ".png";
                portrait.setUrl(PORTRAIT_Z_ORDERS[index], url);
            }
        }
    }
    
    public void setGender(String gender) {
        this.gender = gender;
        assert "m".equals(gender) || "f".equals(gender) : gender;
        this.gender = "m".equals(gender) ? "male" : "female";
        portrait.setUrl(0, "img/paperdoll/" + this.gender + "/model/default.png");
    }

    public ProgressBar getXpProgressBar() {
        return xpProgressBar;
    }
    
    public void clearRecurringEffects() {
        recurringEffects.clear();
    }
    
    public void addRecurringEffectsIcon(String effectType, int additionalRounds) {
        String message = "You are ";
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

    public Element getDetailsLink() {
        return details == null ? null : details.getElement();
    }
}
