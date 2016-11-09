package restless.realms.client.inventory;

import restless.realms.client.ClientState;
import restless.realms.client.layout.SizeConstants;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;

public class ItemFilterPanel {
    private static final int ICON_OFFSET = 112;
    
    private String activeFilter;
    private Image activeFilterImage;

    private final Image showSkills;
    private final Image showWeapons;
    private final Image showArmor;
    private final Image showTrash;
    private final Image showQuest;
    private final Image showConsumable;
    private final Image showPerk;

    private final int iconDimension = SizeConstants.SMALL_ICON_DIMENSION;
    private final int iconPadding = SizeConstants.SMALL_ICON_PADDING;
    private final int iconStartX = 120;

    
    private final InventoryPanel inventoryPanel;
    
    public ItemFilterPanel(InventoryPanel inventoryPanel) {
        this.inventoryPanel = inventoryPanel;
        
        AbsolutePanel titleRow = inventoryPanel.getTitleRow();

        showSkills = new Image("img/icon/filters.png", ICON_OFFSET, 0, iconDimension, iconDimension);
        showSkills.setTitle("Skills");
        showSkills.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                filter("skill");
            }
        });
        titleRow.add(showSkills, iconStartX, 5);

        showWeapons = new Image("img/icon/filters.png", ICON_OFFSET + iconDimension, 0, iconDimension, iconDimension);
        showWeapons.setTitle("Weapons");
        showWeapons.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                filter("weapon");
            }
        });
        titleRow.add(showWeapons, iconStartX + iconDimension + iconPadding, 5);

        showArmor = new Image("img/icon/filters.png", ICON_OFFSET + (iconDimension * 2), 0, iconDimension, iconDimension);
        showArmor.setTitle("Armor");
        showArmor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                filter("armor");
            }
        });
        titleRow.add(showArmor, iconStartX + (iconDimension * 2) + (iconPadding * 2), 5);

        showTrash = new Image("img/icon/filters.png", ICON_OFFSET + (iconDimension * 3), 0, iconDimension, iconDimension);
        showTrash.setTitle("Trash");
        showTrash.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                filter("trash");
            }
        });
        titleRow.add(showTrash, iconStartX + (iconDimension * 3) + (iconPadding * 3), 5);

        showQuest = new Image("img/icon/filters.png", ICON_OFFSET + (iconDimension * 4), 0, iconDimension, iconDimension);
        showQuest.setTitle("Quest Items");
        showQuest.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                filter("quest");
            }
        });
        titleRow.add(showQuest, iconStartX + (iconDimension * 4) + (iconPadding * 4), 5);

        showConsumable = new Image("img/icon/filters.png", ICON_OFFSET + (iconDimension * 5), 0, iconDimension, iconDimension);
        showConsumable.setTitle("Consumable Items");
        showConsumable.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                filter("consumable");
            }
        });
        titleRow.add(showConsumable, iconStartX + (iconDimension * 5) + (iconPadding * 5), 5);
        
        showPerk = new Image("img/icon/filters.png", ICON_OFFSET + (iconDimension * 6), 0, iconDimension, iconDimension);
        showPerk.setTitle("Perks");
        showPerk.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                filter("perk");
            }
        });
        titleRow.add(showPerk, iconStartX + (iconDimension * 6) + (iconPadding * 6), 5);
        
        filter("skill");
    }

    private void filter(String activeFilter) {
        if(this.activeFilter == activeFilter) {
            return;
        }
        
        this.activeFilter = activeFilter;
        if(this.activeFilterImage != null) {
            this.activeFilterImage.setVisibleRect(this.activeFilterImage.getOriginLeft(), 0, iconDimension, iconDimension);
        }
        
        showSkills.addStyleDependentName("clickable");
        showWeapons.addStyleDependentName("clickable");
        showArmor.addStyleDependentName("clickable");
        showTrash.addStyleDependentName("clickable");
        showQuest.addStyleDependentName("clickable");
        showConsumable.addStyleDependentName("clickable");
        showPerk.addStyleDependentName("clickable");
        
        if("skill" == activeFilter) {
            this.activeFilterImage = showSkills;
            inventoryPanel.setWindowTitle("Skills");
        } else if("weapon".equals(activeFilter)) {
            this.activeFilterImage = showWeapons;
            inventoryPanel.setWindowTitle("Weapons");
        } else if("armor".equals(activeFilter)) {
            this.activeFilterImage = showArmor;
            inventoryPanel.setWindowTitle("Armor");
        } else if("trash".equals(activeFilter)) {
            this.activeFilterImage = showTrash;
            inventoryPanel.setWindowTitle("Trash");
        } else if("quest".equals(activeFilter)) {
            this.activeFilterImage = showQuest;
            inventoryPanel.setWindowTitle("Quest Items");
        } else if("consumable".equals(activeFilter)) {
            this.activeFilterImage = showConsumable;
            inventoryPanel.setWindowTitle("Consumables");
        } else if("perk".equals(activeFilter)) {
            this.activeFilterImage = showPerk;
            inventoryPanel.setWindowTitle("Perks");
        } else {
            assert false : activeFilter;
        }
        this.activeFilterImage.setVisibleRect(this.activeFilterImage.getOriginLeft(), iconDimension, iconDimension, iconDimension);
        this.activeFilterImage.removeStyleName("clickable");
        
        if("skill" == activeFilter) {
            JsArray<ScriptObject> skills = PlayerCharacterCache.getInstance().getSkills();
            if(skills != null) {
                ClientState.setSkills(skills);
            }
        } else if("perk" == activeFilter) {
            JsArray<ScriptObject> perks = PlayerCharacterCache.getInstance().getPerks();
            if(perks != null) {
                ClientState.setPerks(perks);
            }
        } else {
            JsArray<ScriptObject> items = PlayerCharacterCache.getInstance().getItems();
            if(items != null) {
                ClientState.setItems(items);
            }
        }
    }

    public String getActiveFilter() {
        return activeFilter;
    }
}
