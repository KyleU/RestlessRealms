package restless.realms.client.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import restless.realms.client.ClientState;
import restless.realms.client.item.ItemPanel;
import restless.realms.client.perk.PerkPanel;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.skill.SkillPanel;
import restless.realms.client.skill.SkillsHandler;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class InventoryPanel extends WindowPanel<ScrollPanel> implements InventoryHandler, SkillsHandler {
    private final VerticalPanel itemList;
    private ItemFilterPanel itemFilterPanel;

    public InventoryPanel() {
        super("inventory", new ScrollPanel(), "Gear", null);
        itemList = new VerticalPanel();
        ClientState.addInventoryHandler(this);
        ClientState.addSkillsHandler(this);
        
        itemFilterPanel = new ItemFilterPanel(this);
        
        body.add(itemList); 
        body.setStylePrimaryName("actionlist");
        setWidth("262px");
	}
    
    @Override
    public void onCurrency(int currency) {
        //no op
    }
    
    @Override
    public void onTokens(int tokens) {
        //no op
    }
    
    @Override
    public void onItems(JsArray<ScriptObject> items) {
        boolean itemsPanel = !itemFilterPanel.getActiveFilter().equals("skill") && !itemFilterPanel.getActiveFilter().equals("perk");
        if(itemsPanel) {
            itemList.clear();
            List<Integer> equipmentIds = PlayerCharacterCache.getInstance().getEquipmentIds();
            List<ScriptObject> sortedItems = new ArrayList<ScriptObject>(items.length());
            assert items != null;
        
            for(int i = 0; i < items.length(); i++) {
                ScriptObject item = items.get(i);
                if(shouldAdd(item)) {
                    int id = item.getInt("id");
                    if(equipmentIds.contains(id)) {
                        equipmentIds.remove((Integer)id);
                    } else {
                        sortedItems.add(item);
                    }
                }
            }
            
            Collections.sort(sortedItems, new Comparator<ScriptObject>() {
                @Override
                public int compare(ScriptObject item1, ScriptObject item2) {
                    return item1.get("name").compareTo(item2.get("name"));
                }
            });
            
            int currentId = 0;
            ItemPanel currentItemPanel = null;
            int currentItemQuantity = 0;
            for(ScriptObject item : sortedItems) {
                int id = item.getInt("id");
                if(id != currentId) {
                    if(currentItemPanel != null) {
                        currentItemPanel.setQuantity(currentItemQuantity);
                    }
                    currentItemQuantity = 0;
                    currentItemPanel = new ItemPanel(item, "inv", null, true);
                    itemList.add(currentItemPanel);
                    currentId = id;
                }
                currentItemQuantity++;
            }
            if(currentItemPanel != null) {
                currentItemPanel.setQuantity(currentItemQuantity);
            }
        }
    }

    public AbsolutePanel getTitleRow() {
        return titleRow;
    }

    private boolean shouldAdd(ScriptObject item) {
        String type = item.get("type");
        String filter = itemFilterPanel.getActiveFilter();
        boolean add = false;
        if(filter.equals("weapon")) {
            add = type.equals("WEAPON");
        } else if(filter.equals("armor")) {
            add = type.equals("HEAD") || type.equals("CHEST") || type.equals("LEGS") || type.equals("ACCESSORY");
        } else if(filter.equals("trash")) {
            add = type.equals("TRASH");
        } else if(filter.equals("quest")) {
            add = type.equals("QUEST");
        } else if(filter.equals("consumable")) {
            add = type.equals("CONSUMABLE");
        } else {
            assert false : filter;
        }
        return add;
	}

    @Override
    public void onSkills(JsArray<ScriptObject> skills) {
        String activeFilter = itemFilterPanel.getActiveFilter();
        if(activeFilter.equals("skill")) {
            itemList.clear();
    
            if(skills != null) {
                for(int i = 0; i < skills.length(); i++) {
                    ScriptObject skill = skills.get(i);
                    addSkill(skill);
                }
            }
        }
    }

    private void addSkill(ScriptObject skill) {
        itemList.add(new SkillPanel(skill, null, true));
    }

    @Override
    public void onPerks(JsArray<ScriptObject> perks) {
        String activeFilter = itemFilterPanel.getActiveFilter();
        if(activeFilter.equals("perk")) {
            itemList.clear();
            if(perks != null) {
                for(int i = 0; i < perks.length(); i++) {
                    ScriptObject perk = perks.get(i);
                    addPerk(perk);
                }
            }
        }
    }

    private void addPerk(ScriptObject perk) {
        itemList.add(new PerkPanel(perk, null, true));
    }
}
