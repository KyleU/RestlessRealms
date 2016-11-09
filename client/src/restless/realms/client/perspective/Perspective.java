package restless.realms.client.perspective;

import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.action.ActionIcon;
import restless.realms.client.audio.AudioManager;
import restless.realms.client.belt.Quickslot;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.equipment.EquipmentCallback;
import restless.realms.client.leaderboard.LeaderboardPanel;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.quest.QuestListPanel;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.DialogPanel;
import restless.realms.client.widget.SignoutOptionsPanel;
import restless.realms.client.worldmap.WorldMapPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;


public abstract class Perspective {
    protected final String code;
    protected boolean initialized;
    
    public Perspective(String code) {
        this.code = code;
    }
    
    public void init() {
        assert(!this.initialized);
        this.initialized = true;
    }
    
    public void onEnter() {
        if(!initialized) {
            init();
        }
    };
    public abstract void onLeave();

    public String getCode() {
        return code;
    }
    
    public void onMessage(MessageType type, Object... params) {
        switch(type) {
        case WORLD_MAP_OPEN:
            WorldMapPanel worldMapPanel = (WorldMapPanel)ClientState.getLayout().getPanel("worldmap");
            worldMapPanel.show();
            break;
        case LEADERBOARD_OPEN:
            ClientState.getLayout().showPanel("leaderboard");
            LeaderboardPanel leaderboardPanel = (LeaderboardPanel)ClientState.getLayout().getPanel("leaderboard");
            leaderboardPanel.show();
            break;
        case GUIDE_OPEN:
            ClientState.getLayout().showPanel("guide");
            ClientState.getLayout().getMainNavigation().activate("guide");
            break;
        case QUESTS_OPEN:
            QuestListPanel questListPanel = (QuestListPanel)ClientState.getLayout().getPanel("quests");
            questListPanel.show();
            break;
        case SIGNOUT_OPTIONS:
            DialogPanel.show(new SignoutOptionsPanel());
            ClientState.getLayout().getMainNavigation().activate("signout");
            break;
        case QUICKSLOTS:
            Quickslot quickslot = (Quickslot)params[0]; 
            ActionIcon icon = (ActionIcon)params[1];
            ClientState.getLayout().getBeltPanel().setQuickslot(quickslot.getIndex(), icon);
            break;
        case EQUIP:
            ActionIcon equipIcon = (ActionIcon)params[0];
            int id = equipIcon.getAction().getInt("id");
            EquipmentCallback callback = new EquipmentCallback("You have equipped " + equipIcon.getAction().get("name") + ".");
            if(equipIcon.getType().equals("perk")) {
                int slot = (Integer)params[1];
                assert slot == 0 || slot == 1 : slot;
                ServiceManager.call("character", "equipperk", callback, "id", id, "slot", slot);
            } else if(equipIcon.getType().equals("item")) {
                ServiceManager.call("character", "equip", callback, "id", id);
            } else if(equipIcon.getType().equals("skill")) {
                ConsoleUtils.error("You can't equip skills, silly.");                
            } else {
                assert false : equipIcon.getType();
            }
            break;
        case ACTIVATE:
            String actionType = (String)params[0];
            Integer actionId = (Integer)params[1];
            ScriptObject action = PlayerCharacterCache.getInstance().getAction(actionType, actionId);
            
            activateNonCombat(actionType, action);
            break;
        default:
            String message = "Unhandled message of type \"" + type + "\" for perspective \"" + code + "\".";
            ConsoleUtils.error(message);
            break;
        }
    }

    protected void activateNonCombat(String actionType, final ScriptObject action) {
        JsArray<ScriptObject> effects = action.getArray("effects");
        boolean allEffectsSelf = true;
        for(int i = 0; i < effects.length(); i++) {
            ScriptObject effect = effects.get(i);
            if(!"SELF".equals(effect.get("targeting"))) {
                allEffectsSelf = false;
            }
        }
        if(allEffectsSelf) {
            ServiceCallback serviceCallback = new ServiceCallback() {
                @Override
                public void onSuccess(ScriptObject result) {
                    String actionType = action.get("type");
                    if("CONSUMABLE".equals(actionType)) {
                        ScriptObject item = PlayerCharacterCache.getInstance().getItem(action.getInt("id"));
                        int id = item.getInt("id");
                        PlayerCharacterCache.getInstance().removeItem(id);
                    } else {
                        assert false : actionType;
                    }

                    JsArray<ScriptObject> effects = result.getArray("effects");
                    
                    ScriptObject hax = ScriptObject.fromJson("{a:[],b:[]}");
                    JsArray<ScriptObject> actions = hax.getArray("a");
                    action.set("source", 0);
                    action.set("target", 0);
                    action.set("results", result.getArray("results"));
                    actions.push(action);
                    JsArrayString actionNames = hax.getStringArray("b");
                    actionNames.push(action.get("name"));
                    
                    ClientState.applyEffects(actions, actionNames);
                    if(effects.length() > 0) {
                        ScriptObject lastResult = effects.get(effects.length() - 1);
                        AudioManager.play(lastResult.get("type").toLowerCase());
                    }

                    
                    ScriptObject player = result.getObject("player");
                    ClientState.setPlayerCharacter(player);
                }
            };
            ServiceManager.call("adventure", "action", serviceCallback , "type", actionType, "id", action.getInt("id"));
        } else {
            ConsoleUtils.error("This " + actionType + " may only be used in combat.");
        }
    }
}
