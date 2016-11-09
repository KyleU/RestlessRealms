package restless.realms.client.room;

import restless.realms.client.ClientManager;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;

public class RoomPanel extends Composite {
    private final int x;
    private final int y;

    private String state;
    private boolean occupied;
    private int backgroundOffsetX;

    private int neighborDirection;
    
    private Image widget;
    private String type;
    
    public RoomPanel(final ScriptObject room) {
        this.x = room.getInt("x");
        this.y = room.getInt("y");
        
        widget = new Image("img/icon/adventure.png", 0, 0, 30, 30);
        initWidget(widget);
        widget.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(neighborDirection > -1) {
                    ClientManager.send(MessageType.MOVE, neighborDirection);
                }
            }
        });
        this.setNeighborDirection(-1);
        this.setStylePrimaryName("room");
        String state = room.get("state");
        setState(state);
        if(room.hasKey("type")) {
            setType(room.get("type"));
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public void setState(String state) {
        this.state = state;
        updateIcon();
    }
    
    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
        updateIcon();
    }
    
    public void setNeighborDirection(int neighborDirection) {
        this.neighborDirection = neighborDirection;
        if(neighborDirection > -1) {
            this.addStyleDependentName("neighbor");
        } else {
            this.removeStyleDependentName("neighbor");
        }
    }
    
    public void setType(String type) {
        this.type = type;
        widget.addStyleDependentName(type.toLowerCase());
        if("EMPTY".equals(type)) {
            backgroundOffsetX = 0;
        } else if("INTRO".equals(type)) {
            backgroundOffsetX = 30;
        } else if("LOOT".equals(type)) {
            backgroundOffsetX = 60;
        } else if("COMBAT".equals(type)) {
            backgroundOffsetX = 90;
        } else if("PUZZLE".equals(type)) {
            backgroundOffsetX = 120;
        } else if("EXIT".equals(type)) {
            backgroundOffsetX = 150;
        } else if("SHRINE".equals(type)) {
            backgroundOffsetX = 180;
        } else {
            assert false;
        }
        updateIcon();
    }
    
    public String getType() {
        return type;
    }
 
    private void updateIcon() {
        int backgroundOffsetY = 0;
        if(!occupied) {
            if("NEW".equals(state)) {
                backgroundOffsetY = 30;
            } else if("ACTIVE".equals(state)) {
                backgroundOffsetY = 90;
            } else if("COMPLETED".equals(state)) {
                backgroundOffsetY = 60;
            } else {
                backgroundOffsetY = 60;
            }
        }
        widget.setVisibleRect(backgroundOffsetX, backgroundOffsetY , 30, 30);
    }
}
