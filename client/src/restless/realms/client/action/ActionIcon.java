package restless.realms.client.action;

import restless.realms.client.layout.SizeConstants;
import restless.realms.client.util.CommonEventHandlers;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.user.client.ui.Image;

public class ActionIcon extends Image {
    private static final String ITEM_ICON_SHEET_URL = "img/icon/items.png";
    private static final String SKILL_ICON_SHEET_URL = "img/icon/skills.png";
    private static final String PERK_ICON_SHEET_URL = "img/icon/perks.png";

    private String type;
    private ScriptObject action;
    
    public ActionIcon() {
        setUrlAndVisibleRect(ITEM_ICON_SHEET_URL, 0, 0, 45, 45);
    }
    
    public void show(String actionType, ScriptObject action, boolean addHandlers) {
        assert actionType == "skill" || actionType == "perk" || actionType == "item" || actionType == "achievement" : actionType;
        this.type = actionType;
        this.action = action;
        ScriptObject iconDescription = action.getObject("icon");
        int x = (iconDescription.hasKey("x") ? iconDescription.getInt("x") : 0);
        int y = (iconDescription.hasKey("y") ? iconDescription.getInt("y") : 0);
        setIconIndexes(x, y);
        if(addHandlers) {
            this.addMouseOverHandler(CommonEventHandlers.TOOLTIP_MOUSE_OVER_SIDE);
            this.addMouseOutHandler(CommonEventHandlers.TOOLTIP_MOUSE_OUT);
        }
    }
    
    public void setIconIndexes(int x, int y) {
        this.setUrlAndVisibleRect(getIconUrl(), x * SizeConstants.ACTION_ICON_SIZE, y * SizeConstants.ACTION_ICON_SIZE, SizeConstants.ACTION_ICON_SIZE, SizeConstants.ACTION_ICON_SIZE);   
    }
    
    private String getIconUrl() {
        String ret;
        if(this.type != null && this.type.equals("skill")) {
            ret = SKILL_ICON_SHEET_URL;
        } else if(this.type != null && this.type.equals("achievement")) {
            ret = SKILL_ICON_SHEET_URL;
        } else if(this.type != null && this.type.equals("perk")) {
            ret = PERK_ICON_SHEET_URL;
        } else {
            ret = ITEM_ICON_SHEET_URL;
        }
        return ret;
    }

    public void clear() {
        clear(1);
    }
    
    public void clear(int index) {
        this.action = null;
        this.type = null;
        this.setUrlAndVisibleRect(getIconUrl(), index * SizeConstants.ACTION_ICON_SIZE, 0 * SizeConstants.ACTION_ICON_SIZE, SizeConstants.ACTION_ICON_SIZE, SizeConstants.ACTION_ICON_SIZE);
    }
    
    public String getType() {
        return type;
    }
    
    public ScriptObject getAction() {
        return action;
    }
}