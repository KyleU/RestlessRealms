package restless.realms.client.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.adventure.AdventurePanel;
import restless.realms.client.audio.AudioManager;
import restless.realms.client.belt.Quickslot;
import restless.realms.client.console.CombatLogPanel;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.layout.PanelLocation;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.perspective.Perspective;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.room.LootPanel;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.Event.NativePreviewEvent;

public class CombatPerspective extends Perspective {
    private static List<String> allyNames;
    private static List<String> enemyNames;

    private CombatPanel combatPanel;
    private String latestState;
    private Map<Integer, Integer> cooldownCache;
    private CombatIntroPanel combatIntroPanel;

    private JsArray<ScriptObject> allies;
    private int allyIndex;
    
    private boolean combatHasFocus = false;
    private boolean pendingServiceCall = false;

    private ServiceCallback loadCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            setHasFocus(true);
            pendingServiceCall = false;
            if(result.hasKey("cooldownCache")) {
                ScriptObject cooldowns = result.getObject("cooldownCache");
                cooldownCache = new LinkedHashMap<Integer, Integer>();
                JsArrayString skills = cooldowns.keys();
                for(int j = 0; j < skills.length(); j++) {
                    String skillString = skills.get(j);
                    if(!skillString.equals("__gwt_ObjectId")) {
                        cooldownCache.put(Integer.parseInt(skillString), cooldowns.getInt(skillString));
                    }
                }
            }

            allyIndex = -1;
            allies = result.getArray("allies");
            allyNames = new ArrayList<String>();
            for(int i = 0; i < allies.length(); i++) {
                ScriptObject ally = allies.get(i);
                String name = ally.get("name");
                allyNames.add(name);
                if(PlayerCharacterCache.getInstance().getName().equals(name)) {
                    assert allyIndex == -1 : allyIndex;
                    allyIndex = i;
                } else {
                    ConsoleUtils.error("MULTI[" + allyIndex + "]: " + name + " = " + ally.toDebugString());
                }
            }
            assert allyIndex > -1 : "No allyIndex."; 
            ClientState.setPlayerCharacter(allies.get(allyIndex));
            
            JsArray<ScriptObject> mobs = result.getArray("enemies");
            assert mobs.length() > 0 : mobs.length();
            enemyNames = new ArrayList<String>();
            for(int i = 0; i < mobs.length(); i++) {
                ScriptObject mob = mobs.get(i);
                enemyNames.add(mob.get("name"));
            }
            combatPanel.setMobs(mobs);
            if(result.hasKey("state") && result.get("state") != null) {
                assert result.hasKey("roundNumber");
                int roundNumber = result.getInt("roundNumber");
                setQuickslotOverlays(roundNumber);
                handleState(result.get("state"));
                if(result.hasKey("intro") && roundNumber == 0) {
                    setHasFocus(false);
                    combatIntroPanel.show(result.get("intro"));
                }
            }
        }
    };

    private ServiceCallback actionCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            pendingServiceCall = false;
            onResult(result);
        }

        @Override
        public void onFailure(String code, String message) {
            pendingServiceCall = false;
            CombatLogPanel combatLogPanel = (CombatLogPanel)ClientState.getLayout().getPanel("combatlog");
            combatLogPanel.add(message, true);
            ConsoleUtils.help(message);
            //super.onFailure(code, message);
        };
    };
    
    public static int getNumAllies() {
        return allyNames.size();
    }
    
    public static String getName(int index) {
        assert allyNames  != null;
        assert allyNames.size() > 0;
        assert enemyNames != null;
        assert enemyNames.size() > 0;

        String ret = null;
        if(index < allyNames.size()) {
            ret = allyNames.get(index);
        } else {
            ret = enemyNames.get(index - allyNames.size());
        }

        assert ret != null;
        return ret;
    }

    public CombatPerspective() {
        super("combat");
    }
    
    @Override
    public void init() {
        super.init();

        combatPanel = new CombatPanel();
        ClientState.addPlayerCharacterHandler(new CombatLogger());

        combatIntroPanel = new CombatIntroPanel();
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "combat", combatPanel);
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "combatintro", combatIntroPanel);
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "death", new DeathPanel());
    }
    
    @Override
    public void onEnter() {
        super.onEnter();
        combatPanel.clear();
        cooldownCache = new HashMap<Integer, Integer>();

        assert ClientState.getAdventureId() != null;
        assert ClientState.getAdventureId() != 0;
        
        if(ClientState.getAdventureId() > 0) {
            AdventurePanel ap = (AdventurePanel)ClientState.getLayout().getPanel("adventure");
            String adventureType = ap.getAdventure().get("type");
            combatPanel.setBackground(adventureType);
        } else {
            combatPanel.setBackground("pvp");
        }
        
        ClientState.getLayout().showPanel("combat");
        ServiceManager.call("combat", "load", loadCallback);
    }
    
    @Override
    public void onMessage(MessageType type, final Object... params) {
        switch(type) {
        case WINDOW_CLOSE:
            if(latestState != null && (
                    latestState.equals("VICTORY") || 
                    latestState.equals("LOSS") 
            )) {
                ClientManager.setPerspective("adventure");
            } else {
                ClientState.getLayout().showPanel("combat");
                ClientState.getLayout().getMainNavigation().activate(null);
                setHasFocus(true);
            }
            break;
        case WORLD_MAP_OPEN:
        case ENTER_LOCATION:
            ConsoleUtils.error("You can't do that while you're in combat!");
            break;
        case GUIDE_OPEN:
        case QUESTS_OPEN:
            setHasFocus(false);
            super.onMessage(type, params);
        case KEY_PRESS:
            NativePreviewEvent event = (NativePreviewEvent)params[0];
            if(combatHasFocus) {
                onKeyPress(event);
            } else {
                int keyCode = event.getNativeEvent().getKeyCode();
                if(keyCode == 'L') {
                    LootPanel lootPanel = (LootPanel)ClientState.getLayout().getPanel("loot");
                    if(lootPanel.isActive()) {
                        lootPanel.lootAll();
                    }
                } else if(keyCode == 27) {
                    onKeyPress(event);
                }
            }
            break;
        case ACTIVATE:
            String actionType = (String)params[0];
            Integer actionId = (Integer)params[1];
            ScriptObject action = PlayerCharacterCache.getInstance().getAction(actionType, actionId);

            if(action == null) {
                ConsoleUtils.error("You don't own that " + actionType + ".");
            }
            
            if(pendingServiceCall) {
                ConsoleUtils.error("You still have a request pending.");
            } else {
                if(combatHasFocus) {
                    activateCombat(actionType, action);
                }
            }
            break;
        case REFRESH:
            ServiceManager.call("combat", "load", loadCallback);
            break;
        default:
            super.onMessage(type, params);
            break;
        }
    }
    
    @Override
    public void onLeave() {
        
    }

    protected void onResult(ScriptObject result) {
        assert result.hasKey("roundNumber") && result.getInt("roundNumber") >= 0;
        int roundNumber = result.getInt("roundNumber");

        assert result.get("state") != null;
        String state = result.get("state");
        
        JsArray<ScriptObject> actions = result.getArray("actions");
        JsArrayString actionNames = result.getStringArray("actionNames");
        for(int i = 0; i < actions.length(); i++) {
            ScriptObject action = actions.get(i);
            if(action.getInt("source") == 0) {
                playSound(action);
                if("s".equals(action.get("type"))) {
                    //cooldown overlay
                    int id = action.getInt("id");
                    Quickslot[] quickslots = ClientState.getLayout().getBeltPanel().getQuickslots();
                    for(Quickslot quickslot : quickslots) {
                        if(quickslot.getId() > 0) {
                            if(quickslot.getTypeCode() == 's' && quickslot.getId() == id ) {
                                cooldownCache.put(quickslot.getId(), roundNumber);
                            }
                        }
                    }
                } else if("i".equals(action.get("type"))) {
                    ScriptObject item = PlayerCharacterCache.getInstance().getItem(action.getInt("id"));
                    if(item == null) {
                        
                    }
                    if(item.get("type").equals("CONSUMABLE")) {
                        int id = item.getInt("id");
                        int maxAdditionalRounds = 0;
                        JsArray<ScriptObject> effectResultsArray = action.getArray("results");
                        for(int j = 0; j < effectResultsArray.length(); j++) {
                            ScriptObject effectResult = effectResultsArray.get(j);
                            int additionalRounds = effectResult.getInt("additionalRounds");
                            if(additionalRounds > maxAdditionalRounds) {
                                maxAdditionalRounds = additionalRounds;
                            }
                        }
                        if(maxAdditionalRounds == 0) {
                            PlayerCharacterCache.getInstance().removeItem(id);
                        }
                    }
                }
            }
        }

        handleState(state);

        ClientState.applyEffects(actions, actionNames);

        JsArray<ScriptObject> stats = result.getArray("stats");
        for(int i = 0; i < stats.length(); i++) {
            ScriptObject statistics = stats.get(i);
            if(i == 0) {
                ClientState.setPlayerCharacter(statistics);
            } else if(i < allyNames.size()) {
                ConsoleUtils.error("STATS: " + allyNames.get(i) + ": " + statistics.toDebugString());
            } else {
                combatPanel.setMob(i - allyNames.size(), statistics);
            }
        }
    
        if(!state.equals("VICTORY")) {
            setQuickslotOverlays(roundNumber + 1);
        }
    }
    
    private void playSound(ScriptObject action) {
        JsArray<ScriptObject> results = action.getArray("results");
        if(results.length() > 0) {
            ScriptObject lastResult = results.get(results.length() - 1);
            AudioManager.play(lastResult.get("type").toLowerCase());
        }
    }

    private void setQuickslotOverlays(int roundNumber) {
        Quickslot[] quickslots = ClientState.getLayout().getBeltPanel().getQuickslots();
        for(Quickslot quickslot : quickslots) {
            if("skill".equals(quickslot.getType())) {
                ScriptObject skill = PlayerCharacterCache.getInstance().getSkill(quickslot.getId());
                int warmup = skill.getInt("warmup");
                if(warmup > roundNumber) {
                    quickslot.showOverlay('w', warmup, warmup - roundNumber);
                } else {
                    int cooldown = skill.getInt("cooldown");
                    int remainingCooldownRounds = 0;
                    if(cooldown > 0) {
                        if(cooldownCache.containsKey(quickslot.getId())) {
                            int lastUsedRound = cooldownCache.get(quickslot.getId());
                            if(cooldown > (roundNumber - 1 - lastUsedRound)) {
                                remainingCooldownRounds = cooldown - (roundNumber - 1 - lastUsedRound);
                                assert remainingCooldownRounds > 0;
                            }
                        }
                    }
                    if(remainingCooldownRounds > 0) {
                        quickslot.showOverlay('c', cooldown, remainingCooldownRounds);
                    } else {
                        quickslot.removeOverlays();
                    }
                }
            }
        }
    }

    private void handleState(String state) {
        this.latestState = state;
        if("VICTORY".equals(state)) {
            setHasFocus(false);
            ClientState.getLayout().getBeltPanel().clearOverlays();

            LootPanel lootPanel = (LootPanel)ClientState.getLayout().getPanel("loot");
            if(ClientState.getAdventureId() > 0) {
                lootPanel.show("COMBAT");
            } else {
                lootPanel.show("PVP");
            }
        } else if("LOSS".equals(state)) {
            setHasFocus(false);
            DeathPanel.show();
        } else if("COMPLETE".equals(state)) {
            //no op
        } else if("WAITING".equals(state)) {
            //no op
        } else {
            assert false : state;
        }
    }
    
    private void activateCombat(String actionType, ScriptObject action) {
        JsArray<ScriptObject> effects = action.getArray("effects");
        int target = allyIndex;
        for(int i = 0; i < effects.length(); i++) {
            ScriptObject effect = effects.get(i);
            if("ENEMY".equals(effect.get("targeting"))) {
                if(target == allyIndex) {
                    target = allies.length() + combatPanel.getSelectionIndex();
                }
            }
        }
        pendingServiceCall = true;
        ServiceManager.call("combat", "action", actionCallback, "target", target, "type", actionType, "id", action.getInt("id"));
    }

    private void onKeyPress(NativePreviewEvent event) {
        if(!ConsoleUtils.onKeyPress(event)) {
            int keyCode = event.getNativeEvent().getKeyCode();
            if(keyCode == 87) {
                ClientManager.send(MessageType.WORLD_MAP_OPEN);
            } else if(keyCode == 71) {
                ClientManager.send(MessageType.GUIDE_OPEN);
            } else if(keyCode == 81) {
                ClientManager.send(MessageType.QUESTS_OPEN);
            } else if(keyCode == 27) {
                ClientManager.send(MessageType.WINDOW_CLOSE);
            } else if(48 < keyCode && keyCode < 57){
                int index = keyCode - 49;
                Quickslot quickslot = ClientState.getLayout().getBeltPanel().getQuickslots()[index];
                if(quickslot.getId() > 0) {
                    ClientManager.send(MessageType.ACTIVATE, quickslot.getType(), quickslot.getId());
                }
            } else if(keyCode == 39) {
                combatPanel.moveSelectionIndex(1);
            } else if(keyCode == 37) {
                combatPanel.moveSelectionIndex(-1);
            }
        }
    }

    private void setHasFocus(boolean hasFocus) {
        combatHasFocus = hasFocus;
//        ConsoleUtils.log(ConsoleChannel.Debug, "HasFocus:" + hasFocus);
    }
}
