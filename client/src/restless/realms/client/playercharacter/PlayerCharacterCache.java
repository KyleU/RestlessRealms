package restless.realms.client.playercharacter;

import java.util.ArrayList;
import java.util.List;

import restless.realms.client.ClientState;
import restless.realms.client.belt.BeltPanel;
import restless.realms.client.belt.Quickslot;
import restless.realms.client.inventory.InventoryHandler;
import restless.realms.client.skill.SkillsHandler;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

public class PlayerCharacterCache implements PlayerCharacterHandler, SkillsHandler, InventoryHandler {
    private static PlayerCharacterCache instance = new PlayerCharacterCache();
    
    private int adventurePoints;
    
    private String name;
    private String professionId;
    private ScriptObject profession;
    private int level;
    private int xp;
    private int maxHitpoints;
    private int maxMana;
    
    private int currency;
    private int tokens;
    private ScriptObject equipment;
    private JsArray<ScriptObject> items;
    private JsArray<ScriptObject> perks;
    private JsArray<ScriptObject> skills;

    public static PlayerCharacterCache getInstance() {
        return instance;
    }
    
    private PlayerCharacterCache() {
    }

    public ScriptObject getEquipment() {
        return equipment;
    }
    
    public void setEquipment(ScriptObject equipment) {
        this.equipment = equipment;
    }
    
    public List<Integer> getEquipmentIds() {
        assert equipment != null;
        List<Integer> ret = new ArrayList<Integer>();
        int id = 0;
        id = equipment.getInt("head");
        if(id > 0) {
            ret.add(id);
        }
        id = equipment.getInt("chest");
        if(id > 0) {
            ret.add(id);
        }
        id = equipment.getInt("legs");
        if(id > 0) {
            ret.add(id);
        }
        id = equipment.getInt("accessory");
        if(id > 0) {
            ret.add(id);
        }
        id = equipment.getInt("weapon");
        if(id > 0) {
            ret.add(id);
        }
        return ret;
    }

    public List<Integer> getPerkIds() {
        assert equipment != null;
        List<Integer> ret = new ArrayList<Integer>();
        String id = null;
        id = equipment.get("perkOne");
        if(id != null && !"undefined".equals(id)) {
            ret.add(Integer.parseInt(id));
        } else {
            ret.add(null);
        }
        id = equipment.get("perkTwo");
        if(id != null && !"undefined".equals(id)) {
            ret.add(Integer.parseInt(id));
        } else {
            ret.add(null);
        }
        return ret;
    }

    public ScriptObject getAction(String type, int id) {
        ScriptObject ret = null;
        if(type.equals("skill")) {
            ret = PlayerCharacterCache.getInstance().getSkill(id);
        } else if(type.equals("item")) {
            ret = PlayerCharacterCache.getInstance().getItem(id);
        } else if(type.equals("perk")) {
            ret = PlayerCharacterCache.getInstance().getPerk(id);
        } else {
            assert false;
        }
        if(ret == null) {
            throw new IllegalArgumentException("You don't have a " + type + " with id " + id + ".");
        }
        return ret;
    }

    public ScriptObject getSkill(int id) {
        ScriptObject ret = null;
        if(skills != null) {
            for(int i = 0; i < skills.length(); i++) {
                ScriptObject skill = skills.get(i);
                if(id == skill.getInt("id")) {
                    ret = skill;
                    break;
                }
            }
        }
        return ret;
    }
    
    public JsArray<ScriptObject> getSkills() {
        return skills;
    }
    
    public ScriptObject getItem(int id) {
        ScriptObject ret = null;
        if(items != null) {
            for(int i = 0; i < items.length(); i++) {
                ScriptObject item = items.get(i);
                if(id == item.getInt("id")) {
                    ret = item;
                    break;
                }
            }
        }
        return ret;
    }
    
    public JsArray<ScriptObject> getItems() {
        return items;
    }

    public void removeItem(int id) {
        int index = -1;
        JsArray<ScriptObject> newItems = ScriptObject.createArray().cast();
        if(items != null) {
            for(int i = 0; i < items.length(); i++) {
                ScriptObject item = items.get(i);
                if(index == -1 && id == item.getInt("id")) {
                    index = i;
                } else {
                    newItems.set(newItems.length(), item);
                }
            }
        }
        ClientState.setItems(newItems);

        if(!hasItem(id)) {
            BeltPanel beltPanel = ClientState.getLayout().getBeltPanel();
            Quickslot[] quickslots = beltPanel.getQuickslots();
            for(int j = 0; j < quickslots.length; j++) {
                Quickslot quickslot = quickslots[j];
                if("item".equals(quickslot.getType())) {
                    if(quickslot.getId() == id) {
                        beltPanel.setQuickslot(j, null);
                    }
                }
            }            
        }
    }

    public boolean hasItem(int id) {
        boolean ret = false;
        if(items != null) {
            for(int i = 0; i < items.length(); i++) {
                ScriptObject item = items.get(i);
                if(id == item.getInt("id")) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    public ScriptObject getPerk(int id) {
        ScriptObject ret = null;
        if(perks != null) {
            for(int i = 0; i < perks.length(); i++) {
                ScriptObject perk = perks.get(i);
                if(id == perk.getInt("id")) {
                    ret = perk;
                    break;
                }
            }
        }
        return ret;
    }
    
    public JsArray<ScriptObject> getPerks() {
        return perks;
    }

    public int getAdventurePoints() {
        return adventurePoints;
    }
    
    public String getName() {
        return name;
    }

    public String getProfessionId() {
        return professionId;
    }
    
    public ScriptObject getProfession() {
        return profession;
    }
    public void setProfession(ScriptObject profession) {
        this.profession = profession;
        this.professionId = profession == null ? null : profession.get("id");
    }
    
    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public int getMaxHitpoints() {
        return maxHitpoints;
    }
    
    public int getMaxMana() {
        return maxMana;
    }
    
    public void init() {
        ClientState.addPlayerCharacterHandler(this);
        ClientState.addSkillsHandler(this);
        ClientState.addInventoryHandler(this);
    }

    @Override
    public void onSkills(JsArray<ScriptObject> skills) {
        this.skills = skills;
    }

    @Override
    public void onItems(JsArray<ScriptObject> items) {
        this.items = items;
    }
    
    @Override
    public void onPerks(JsArray<ScriptObject> perks) {
        this.perks = perks;
    }

    public int getCurrency() {
        return currency;
    }

    @Override
    public void onCurrency(int currency) {
        this.currency = currency;
    }

    public int getTokens() {
        return tokens;
    }

    @Override
    public void onTokens(int tokens) {
        this.tokens = tokens;
    };
    
    @Override
    public void applyEffects(JsArray<ScriptObject> actions, JsArrayString actionNames) {
        // no op
    }

    @Override
    public void onAdventurePoints(int adventurePoints) {
        this.adventurePoints = adventurePoints;
    }

    @Override
    public void onPlayerCharacter(ScriptObject playerCharacter) {
        if(playerCharacter.hasKey("name")) {
            this.name = playerCharacter.get("name");
        }
        if(playerCharacter.hasKey("level")) {
            this.level = playerCharacter.getInt("level");
        }
        if(playerCharacter.hasKey("xp")) {
            this.xp = playerCharacter.getInt("xp");
        }
        if(playerCharacter.hasKey("profession")) {
            this.professionId = playerCharacter.get("profession");
        }
        if(playerCharacter.hasKey("maxHitpoints")) {
            this.maxHitpoints = playerCharacter.getInt("maxHitpoints");
        }
        if(playerCharacter.hasKey("maxMana")) {
            this.maxMana = playerCharacter.getInt("maxMana");
        }
    }
}
