package restless.realms.server.adventure.map;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.room.RoomType;
import restless.realms.server.util.HasIntegerId;

@Entity
@NamedQueries({
    @NamedQuery(name="adventureMap.getAll", query="select a from AdventureMap a order by id")
})
public class AdventureMap implements HasIntegerId {
    private Integer id;
    private String adventure;
    private int introductionRoomIndex;
    private String serializedForm;
    private List<AdventureMapLocation> locations;
    
    public AdventureMap() {
    }
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }
    
    @NotEmpty
    @Length(max=FieldLengths.STRING_ID)
    public String getAdventure() {
        return adventure;
    }
    public void setAdventure(String adventure) {
        this.adventure = adventure;
    }

    @Length(max=2500)
    public String getSerializedForm() {
        return serializedForm;
    }
    public void setSerializedForm(String serializedForm) {
        this.serializedForm = serializedForm;
        init(serializedForm.split("/"));
    }
    
    @Transient
    public List<AdventureMapLocation> getLocations() {
        return locations;
    }
    
    @Transient
    public int getDistance(int indexA, int indexB) {
        int ret = 0;
        AdventureMapLocation a = locations.get(indexA);
        AdventureMapLocation b = locations.get(indexB);
        ret += Math.abs(a.getX() - b.getX());
        ret += Math.abs(a.getY() - b.getY());
        return ret;
    }

    @Transient
    public int getIntroductionRoomIndex() {
        return introductionRoomIndex;
    }
    
    private void init(String[] serializedMap) {
        introductionRoomIndex = -1;
        locations = new ArrayList<AdventureMapLocation>();

        int rows = serializedMap.length;
        int cols = 0;
        int y = 0;
        for (int i = rows - 1; i >= 0; i--) {
            char[] row = serializedMap[i].toCharArray();
            if(cols == 0) {
                cols = row.length;
            } else {
                if(cols != row.length) {
                    throw new IllegalArgumentException("Unbalanced row \"" + serializedMap[i] + "\" at position " +  y + " (expected length " + cols + ").");
                }
            }
            for(int x = 0; x < row.length; x++) {
                RoomType type = null;
                switch(row[x]) {
                case '.':
                    break;
                case '!':
                    if(introductionRoomIndex != -1) {
                        throw new IllegalArgumentException("Mulitple introductory rooms for map " + serializedMap + ".");
                    }
                    introductionRoomIndex = locations.size();
                    type = RoomType.INTRO;
                    break;
                case '%':
                    type = RoomType.SHRINE;
                    break;
                case '?':
                    type = RoomType.PUZZLE;
                    break;
                case '0':
                    type = RoomType.EMPTY;
                    break;
                case '#':
                    type = RoomType.EXIT;
                    break;
                default:
                    if('a' <= row[x] && 'z' >= row[x]) {
                        type = RoomType.COMBAT;
                    } else if('1' <= row[x] && '9' >= row[x]) {
                        type = RoomType.LOOT;
                    } else  {
                        throw new IllegalArgumentException("Unknown room code \"" + row[x] + "\" at position " + x + ", " + y + ".");
                    }
                }
                if(type != null) {
                    locations.add(new AdventureMapLocation(row[x], type, x, y));
                }
            }
            y++;
        }
        if(introductionRoomIndex == -1) {
            throw new IllegalArgumentException("No introduction room in \"" + serializedForm + "\".");
        }
    }
}
