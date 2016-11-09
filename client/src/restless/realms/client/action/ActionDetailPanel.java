package restless.realms.client.action;

import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

public class ActionDetailPanel extends Composite {
    private static ActionDetailPanel instance;
    
    private HTML body;
    
    private ActionDetailPanel() {
        body = new HTML("");
        body.setStylePrimaryName("actiondetail");
        initWidget(body);
    }
    
    public static ActionDetailPanel getInstance() {
        if(instance == null) {
            instance = new ActionDetailPanel();
            //instance.setVisible(false);
            Style style = instance.getElement().getStyle();
            style.setProperty("position", "absolute");
            style.setProperty("top", "0px");
            style.setProperty("left", "0px");
            style.setProperty("display", "none");
            RootPanel.get().add(instance);
        }
        return instance;
    }
    
    public void clear() {
        this.setVisible(false);
        body.setText("");
    }

    public void setAction(ActionIcon actionIcon, boolean showAbove) {
        body.setHTML(getDetailHtml(actionIcon));

        Style style = this.getElement().getStyle();
        this.setVisible(true);
        if(showAbove) {
            style.setProperty("top", (actionIcon.getAbsoluteTop() - this.getOffsetHeight()) + "px");
            style.setProperty("left", (actionIcon.getAbsoluteLeft() - 2) + "px");
//            style.setProperty("top", (actionIcon.getAbsoluteTop() + 45) + "px");
//            style.setProperty("left", (actionIcon.getAbsoluteLeft()) + "px");
        } else {
            //style.setProperty("top", actionIcon.getAbsoluteTop() + "px");
            style.setProperty("top", (actionIcon.getAbsoluteTop() - (this.getOffsetHeight() - 45)) + "px");
            style.setProperty("left", (actionIcon.getAbsoluteLeft() + 45) + "px");
        }
    }

    public static String getDetailHtml(ActionIcon actionIcon) {
        String ret = "";
        ScriptObject action = actionIcon.getAction();
        if(action == null) {
            return null;
        }
        String type = actionIcon.getType();

        String rarityName = "normal";
        if(type == "item") {
            int rarity = action.getInt("rarity");
            if(rarity == 0) {
                //normal
            } else if(rarity == 1) {
                rarityName = "magic";
            } else if(rarity == 2) {
                rarityName = "rare";
            } else if(rarity == 3) {
                rarityName = "unique";
            } else if(rarity == 4) {
                rarityName = "legendary";
            } else {
                assert false : rarity;
            }
        }
        
        ret += "<div class='" + rarityName  + " name'>" + action.get("name") + "</div>";

        if("skill".equals(type)) {
            
        } else if("perk".equals(type)) {
            
        } else if("item".equals(type)) {
            type = action.get("type");
            ret += "<div class='type'>" + type.substring(0, 1) + type.substring(1).toLowerCase() + "</div>";

            String summary = action.get("summary");
            ret += "<div class='actionsummary'>" + summary + "</div>";
        } else {
            assert false : type;
        }

        //ret += "<div class='summary'>" + action.get("summary") + "</div>";
        JsArray<ScriptObject> effects = action.getArray("effects");
        if(effects.length() > 0) {
            for(int i = 0; i < effects.length(); i++) {
                ScriptObject effect = effects.get(i);
                ret += getEffectString(type, i, effect) + "<br/>";
            }
        }
        if(type == "skill") {
            if(action.getInt("warmup") > 0) {
                ret += "<div class='warmup'>" + action.get("warmup") + " round warmup.</div>";
            }
            if(action.getInt("cooldown") > 0) {
                ret += "<div class='cooldown'>" + action.get("cooldown") + " round cooldown.</div>";
            }
            if(action.getInt("manaCost") > 0) {
                int manaCost = action.getInt("manaCost");
                int manaCostPerLevel = action.getInt("manaCostPerLevel");
                if(manaCostPerLevel > 0) {
                    int level = PlayerCharacterCache.getInstance().getLevel();
                    manaCost = manaCost + (manaCostPerLevel * level);
                }
                ret += "<div class='cost'>" + manaCost + " mana.</div>";
            }
        } else if(type == "perk") {
            //no op
        } else {
            //item
            if(action.getInt("minLevel") > 1) {
                ret += "<div class='level'>Level " + action.get("minLevel") + " required.</div>";
            }
            if(action.hasKey("requiredProfession")) {
                String prof = action.get("requiredProfession");
                ret += "<div class='profession'>" + prof.substring(0, 1).toUpperCase() + prof.substring(1) + " only.</div>";
            }
        }
        return ret;
    }
    
    public static String getEffectString(String type, int index, ScriptObject effect) {
        int level = PlayerCharacterCache.getInstance().getLevel();

        String effectType = effect.get("effectType");
        String targeting = effect.get("targeting");
        int percentChance = effect.getInt("percentChance");
        int percentChancePerLevel = effect.getInt("percentChancePerLevel");
        if(percentChancePerLevel > 0) {
            percentChance += (percentChancePerLevel + level);
            if(percentChance > 100) {
                percentChance = 100;
            }
        }
        int minQuantity = effect.getInt("minQuantity");
        int maxQuantity = effect.getInt("maxQuantity");
        int quantityPerLevel = effect.getInt("quantityPerLevel"); 
        if(quantityPerLevel > 0) {
            minQuantity += (quantityPerLevel * level);
            maxQuantity += (quantityPerLevel * level);
        }
        int minAdditionalRounds = effect.getInt("minAdditionalRounds");
        int maxAdditionalRounds = effect.getInt("maxAdditionalRounds");
        String effectString = ActionDetailSerializer.getEffectString(type, targeting, index, effectType, percentChance, minQuantity, maxQuantity, minAdditionalRounds, maxAdditionalRounds);
        return "<span class='" + effectType.toLowerCase() + "'>" + effectString + "</span>";
    }
}
