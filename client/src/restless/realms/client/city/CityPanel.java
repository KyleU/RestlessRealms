package restless.realms.client.city;

import restless.realms.client.ClientManager;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.util.AuditManager;
import restless.realms.client.widget.WindowPanel;
import restless.realms.client.worldmap.MapLocation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class CityPanel extends WindowPanel<AbsolutePanel> {
    public CityPanel() {
        super("city", new AbsolutePanel(), "Outpost", null);
        addLocation("tavern", "city", "The Tavern", 3, 32, 116, 198);
        addLocation("guildhall", "city", "Guild Hall", 122, 72, 96, 131);
        addLocation("accessories", "city", "Accessories", 220, 104, 76, 83);
        addLocation("weaponsarmor", "city", "Weapons and Armor", 297, 73, 77, 114);
        addLocation("alchemy", "city", "Alchemist", 375, 87, 76, 101);
        addLocation("home", "city", "Your Home", 453, 120, 77, 67);
        addLocation("trainer", "city", "Skills and Perks", 532, 57, 98, 151);
        addLocation("auction", "city", "Auction House", 632, 73, 109, 168);

        addLocation("wares", "city", "Unique Wares", 129, 211, 95, 88);
        addLocation("news", "city", "Town Crier", 500, 206, 92, 95);
        
        addExitButton(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ClientManager.send(MessageType.WORLD_MAP_OPEN);
            }
        });
	}
	
	private void addLocation(final String id, String map, String title, int x, int y, int width, int height) {
        MapLocation loc = new MapLocation(id, map, title, 0, 0, x, y, width, height, title, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onNavigation(id);
            }
        });
        body.add(loc, loc.getMapX(), loc.getMapY());
    }

    protected void onNavigation(String key) {
	    AuditManager.audit("city", key);
	    if("tavern".equals(key)) {
            ClientManager.send(MessageType.ENTER_LOCATION, "questboard");
        } else if("guildhall".equals(key)) {
            ClientManager.send(MessageType.ENTER_LOCATION, "guildhall");
        } else if("home".equals(key)) {
            ClientManager.send(MessageType.ENTER_LOCATION, "home");
        } else if("auction".equals(key)) {
            ClientManager.send(MessageType.ENTER_LOCATION, "auction");
        } else if("wares".equals(key)) {
            ClientManager.send(MessageType.ENTER_LOCATION, "wares");
        } else if("news".equals(key)) {
            ClientManager.send(MessageType.ENTER_LOCATION, "news");
        } else if("trainer".equals(key)) {
            ClientManager.send(MessageType.ENTER_LOCATION, "trainer");
        } else {
            ClientManager.send(MessageType.ENTER_LOCATION, "itemshop", key);
        }
	}
}

