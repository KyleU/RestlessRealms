package restless.realms.server.adventure;

import java.util.ArrayList;
import java.util.Date;
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
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.room.Room;
import restless.realms.server.util.HasIntegerId;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate=true)
@NamedQueries({
})
public class Adventure implements HasIntegerId {
    public static enum Status {
        ACTIVE, 
        COMPLETE, 
        ABANDONED
    }
    
	private Integer id;

	private String type;
	private int seed;
	
	private List<PlayerCharacter> participants;
	
    private Status status;
    private int activeRoomIndex;
    
    private List<Room> rooms;
	
	private Date created;
	private Date updated;
	
	public Adventure() {
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public int getSeed() {
		return seed;
	}
	public void setSeed(int seed) {
		this.seed = seed;
	}

    @NotNull
    @Column(length=FieldLengths.ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}

	public int getActiveRoomIndex() {
		return activeRoomIndex;
	}
	public void setActiveRoomIndex(int activeRoomIndex) {
		this.activeRoomIndex = activeRoomIndex;
	}

    @ManyToMany(fetch=FetchType.LAZY, cascade={CascadeType.PERSIST, CascadeType.MERGE})
    @IndexColumn(name="orderIndex")
	public List<PlayerCharacter> getParticipants() {
        return participants;
    }
	public void setParticipants(List<PlayerCharacter> participants) {
        this.participants = participants;
    }
	
	@OneToMany(mappedBy="adventure", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@IndexColumn(name="roomIndex")
	public List<Room> getRooms() {
        return rooms;
    }
    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public void addRoom(Room room) {
        if(this.rooms == null) {
            this.rooms = new ArrayList<Room>();
        }

        room.setAdventure(this);
        room.setRoomIndex(this.rooms.size());
        this.rooms.add(room);
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}

    @Temporal(TemporalType.TIMESTAMP)
	public Date getUpdated() {
		return updated;
	}
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
}