package restless.realms.server.adventure.map;

import restless.realms.server.room.RoomType;

public class AdventureMapLocation {
    private final char code;
    private final RoomType type;
    private final int x;
    private final int y;
    
    public AdventureMapLocation(char code, RoomType type, int x, int y) {
        super();
        this.code = code;
        this.type = type;
        this.x = x;
        this.y = y;
    }
    
    public char getCode() {
        return code;
    }
    public RoomType getType() {
        return type;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
}
