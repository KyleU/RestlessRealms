package restless.realms.client.adventure;

import java.util.ArrayList;
import java.util.List;

import restless.realms.client.room.RoomPanel;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;

public class AdventureMapPanel extends Composite {
    private List<RoomPanel> rooms;
    private int activeRoomIndex;
    
    AbsolutePanel body = new AbsolutePanel();

    public AdventureMapPanel() {
        this.rooms = new ArrayList<RoomPanel>();
        this.activeRoomIndex = -1;
        initWidget(body);
    }
    
    public void setRooms(JsArray<ScriptObject> roomArray) {
        clear();
        if(roomArray != null) {
            for(int i = 0; i < roomArray.length(); i++) {
                ScriptObject room = roomArray.get(i);
                RoomPanel roomPanel = new RoomPanel(room);
                addPanel(roomPanel);
            }
        }
    }
    
    public void setActiveRoomIndex(String previousRoomState, int index, String type, String state) {
        if(activeRoomIndex > -1) {
            int[] oldNeighbors = getNeighbors(activeRoomIndex);
            for(int i = 0; i < oldNeighbors.length; i++) {
                if(oldNeighbors[i] > -1) {
                    rooms.get(oldNeighbors[i]).setNeighborDirection(-1);
                }
            }
            RoomPanel previous = rooms.get(activeRoomIndex);
            previous.setOccupied(false);
            if(previousRoomState != null) {
                previous.setState(previousRoomState);
            }
        }
        
        this.activeRoomIndex = index;
        int[] neighbors = getNeighbors(index);
        for(int i = 0; i < neighbors.length; i++) {
            if(neighbors[i] > -1) {
                rooms.get(neighbors[i]).setNeighborDirection(i);
            }
        }
        RoomPanel current = rooms.get(activeRoomIndex);
        current.setState(state);
        current.setOccupied(true);
        if(type != null) {
            current.setType(type);
        }
    }
    
    public int[] getNeighbors(int index) {
        assert rooms.get(index) != null;
        int[] ret = new int[] {-1, -1, -1, -1};
        RoomPanel activeRoom = rooms.get(index);
        for(int i = 0; i < rooms.size(); i++) {
            RoomPanel room = rooms.get(i);
            if(room.getX() == activeRoom.getX()) {
                if(room.getY() == activeRoom.getY() + 1) {
                    assert ret[0] == -1;
                    ret[0] = i;
                } else if(room.getY() == activeRoom.getY() - 1) {
                    assert ret[2] == -1;
                    ret[2] = i;
                } 
            }
            if(room.getY() == activeRoom.getY()) {
                if(room.getX() == activeRoom.getX() + 1) {
                    assert ret[1] == -1;
                    ret[1] = i;
                } else if(room.getX() == activeRoom.getX() - 1) {
                    assert ret[3] == -1;
                    ret[3] = i;
                } 
            }
        }
        return ret;
    }

    private void addPanel(RoomPanel roomPanel) {
        rooms.add(roomPanel);
        int x = (roomPanel.getX() * 35) + 10;
        int y = 260 - ((roomPanel.getY() * 35));
        body.add(roomPanel, x, y);        
    }

    private void clear() {
        this.activeRoomIndex = -1;
        for(RoomPanel room : rooms) {
            body.remove(room);
        }
        rooms.clear();
    }
}
