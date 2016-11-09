package restless.realms.server.adventure;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Range;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasStringId;
import restless.realms.server.util.ScaleOptions;

@Entity
@NamedQueries({
    @NamedQuery(name="adventureArchetype.getAll", query="select a from AdventureArchetype a")
})
public class AdventureArchetype implements HasStringId {
    private String id;
    private String name;
    private int minLevel;
    private int maxLevel;
    
    private String mapCode;
    private int worldMapX;
    private int worldMapY;
    private int worldMapWidth;
    private int worldMapHeight;
    private String description;
    
    public AdventureArchetype() {
    }

    public AdventureArchetype(String id, String name, int minLevel, int maxLevel, String mapCode, int mapX, int mapY, int mapWidth, int mapHeight, String description) {
        super();
        this.id = id;
        this.name = name;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.mapCode = mapCode;
        this.worldMapX = mapX;
        this.worldMapY = mapY;
        this.worldMapWidth = mapWidth;
        this.worldMapHeight = mapHeight;
        this.description = description;
    }

    @Id
    @NotEmpty
    @Length(max=FieldLengths.STRING_ID)
    @Override
    public String getId() {
        return id;
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }

    @NotEmpty
    @Length(max=FieldLengths.ADVENTURE_ARCHETYPE_NAME)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Range(min=0, max=ScaleOptions.MAX_LEVEL)
    public int getMinLevel() {
        return minLevel;
    }
    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    @Range(min=0, max=ScaleOptions.MAX_LEVEL)
    public int getMaxLevel() {
        return maxLevel;
    }
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }
    
    @Length(min=0, max=30)
    public String getMapCode() {
        return mapCode;
    }
    public void setMapCode(String mapCode) {
        this.mapCode = mapCode;
    }

    @Range(min=-100, max=1000)
    public int getWorldMapX() {
        return worldMapX;
    }
    public void setWorldMapX(int worldMapX) {
        this.worldMapX = worldMapX;
    }

    @Range(min=-100, max=250)
    public int getWorldMapY() {
        return worldMapY;
    }
    public void setWorldMapY(int worldMapY) {
        this.worldMapY = worldMapY;
    }
    
    @Range(min=0, max=500)
    public int getWorldMapWidth() {
        return worldMapWidth;
    }
    public void setWorldMapWidth(int worldMapWidth) {
        this.worldMapWidth = worldMapWidth;
    }

    @Range(min=0, max=220)
    public int getWorldMapHeight() {
        return worldMapHeight;
    }
    public void setWorldMapHeight(int worldMapHeight) {
        this.worldMapHeight = worldMapHeight;
    }

    @NotEmpty
    @Length(max=1000)
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
