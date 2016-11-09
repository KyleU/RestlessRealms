package restless.realms.server.room;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.NotNull;

import restless.realms.server.adventure.Adventure;
import restless.realms.server.combat.Combat;
import restless.realms.server.database.FieldLengths;
import restless.realms.server.item.Item;
import restless.realms.server.mob.Mob;
import restless.realms.server.util.HasIntegerId;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate=true)
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames={"adventureId", "roomIndex"})}
)
@NamedQueries({
    @NamedQuery(name="room.getByIndex", query="select r from Room r where r.adventure.id = ? and r.roomIndex = ?"),
    @NamedQuery(name="room.getContents", query="select i from Room r join r.contents i where r.id = ?"),
    @NamedQuery(name="room.getInhabitants", query="select i from Room r join r.inhabitants i where r.id = ?")
})
public class Room implements HasIntegerId {
    private Integer id;
    
    private Adventure adventure;
    private int roomIndex;
    
    private RoomType type;
    private RoomState state;
	private Combat combat;
	private List<Mob> inhabitants;
	private List<Item> contents;
	
	public Room() {
    }
	
	public Room(RoomType type) {
        this.setType(type);
        this.setState(RoomState.NEW);
        this.setInhabitants(new ArrayList<Mob>());
        this.setContents(new ArrayList<Item>());
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
    
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="adventureId", nullable=false)
    public Adventure getAdventure() {
        return adventure;
    }

    public void setAdventure(Adventure adventure) {
        this.adventure = adventure;
    }

    public int getRoomIndex() {
        return roomIndex;
    }
    public void setRoomIndex(int roomIndex) {
        this.roomIndex = roomIndex;
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length=FieldLengths.ENUM_LENGTH)
    public RoomType getType() {
        return type;
    }
    public void setType(RoomType type) {
        this.type = type;
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length=FieldLengths.ENUM_LENGTH)
    public RoomState getState() {
        return state;
    }
    public void setState(RoomState state) {
        this.state = state;
    }

    @OneToOne(cascade = CascadeType.ALL, optional=true, fetch=FetchType.LAZY)
    @JoinColumn(name="combatId")
    public Combat getCombat() {
        return combat;
    }
    public void setCombat(Combat combat) {
        this.combat = combat;
    }
    
    @OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    @JoinTable(
            joinColumns = @JoinColumn( name="roomId"),
            inverseJoinColumns = @JoinColumn( name="mobId")
    )
    @IndexColumn(name="orderIndex")
    public List<Mob> getInhabitants() {
        return inhabitants;
    }
    public void setInhabitants(List<Mob> inhabitants) {
        this.inhabitants = inhabitants;
    }

    @ManyToMany(fetch=FetchType.LAZY)
    @IndexColumn(name="orderIndex")
    public List<Item> getContents() {
        return contents;
    }
    public void setContents(List<Item> contents) {
        this.contents = contents;
    }
}
