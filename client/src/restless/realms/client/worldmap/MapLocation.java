package restless.realms.client.worldmap;

import restless.realms.client.ClientManager;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

public class MapLocation extends Composite {
    private final String id;
    private final String map;
    private final String name;
    private final int minLevel;
    private final int maxLevel;
    private final int mapX;
    private final int mapY;
    private final int mapWidth;
    private final int mapHeight;
    private final String description;
    
    public MapLocation(ScriptObject l) {
        this(l.get("id"), l.get("mapCode"), l.get("name"), l.getInt("minLevel"), l.getInt("maxLevel"), l.getInt("worldMapX"), l.getInt("worldMapY"), l.getInt("worldMapWidth"), l.getInt("worldMapHeight"), l.get("description"), null);
    }
    
    public MapLocation(String id, String map, String name, int minLevel, int maxLevel, int mapX, int mapY, int mapWidth, int mapHeight, String description, ClickHandler clickHandler) {
        super();
        this.id = id;
        this.map = map;
        this.name = name;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.mapX = mapX;
        this.mapY = mapY;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.description = description;

        HTML hotspot = new HTML();
        Style style = hotspot.getElement().getStyle();
        style.setProperty("cursor", "pointer");
        style.setProperty("width", this.mapWidth + "px");
        style.setProperty("height", this.mapHeight + "px");
        
        String title = this.name;
        if(this.maxLevel > 0) {
            title += " (Levels " + minLevel + " - " + maxLevel + ")";
        }
        
        hotspot.setTitle(title);
        if(clickHandler == null) {
            clickHandler = new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    boolean playPerspective = "play".equals(ClientManager.getActivePerspective());
                    if(MapLocation.this.minLevel == 0 && playPerspective) {
                        ClientManager.send(MessageType.ENTER_LOCATION, MapLocation.this.id);
                    } else {
                        AdventureIntroductionPanel.show(MapLocation.this);
                    }
                }
            };
        }
        hotspot.addClickHandler(clickHandler);
        initWidget(hotspot);
    }

    public String getId() {
        return id;
    }
    public String getMap() {
        return map;
    }
    public String getName() {
        return name;
    }
    public int getMinLevel() {
        return minLevel;
    }
    public int getMaxLevel() {
        return maxLevel;
    }
    public int getMapX() {
        return mapX;
    }
    public int getMapY() {
        return mapY;
    }
    public int getMapWidth() {
        return mapWidth;
    }
    public int getMapHeight() {
        return mapHeight;
    }
    public String getDescription() {
        return description;
    }
}
