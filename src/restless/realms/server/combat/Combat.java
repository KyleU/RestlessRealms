package restless.realms.server.combat;

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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.validator.NotNull;

import restless.realms.server.room.Room;
import restless.realms.server.util.HasIntegerId;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate=true)
public class Combat implements HasIntegerId {
    public static enum CombatType {PvE, PvP};
    
    private Integer id;

    private Room room;

    private CombatType combatType;
    private List<CombatParticipant> participants;
    
    private List<CombatRound> rounds;
    private int activeRoundNumber;
    
    public Combat() {
    }

    public Combat(Integer id, Room room, CombatType type, List<CombatParticipant> participants) {
        super();
        this.id = id;
        this.room = room;
        this.combatType = type;
        this.participants = participants;
        this.rounds = new ArrayList<CombatRound>();
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

    @OneToOne(mappedBy="combat")
    public Room getRoom() {
        return room;
    }
    public void setRoom(Room room) {
        this.room = room;
    }

    @Enumerated(EnumType.STRING)
    @Column(length=3)
    @NotNull
    public CombatType getCombatType() {
        return combatType;
    }
    public void setCombatType(CombatType combatType) {
        this.combatType = combatType;
    }

    @CollectionOfElements(fetch=FetchType.EAGER)
    @JoinTable(
        joinColumns = @JoinColumn( name="combatId")
    )
    @IndexColumn(name="orderIndex")
    public List<CombatParticipant> getParticipants() {
        return participants;
    }
    public void setParticipants(List<CombatParticipant> participants) {
        this.participants = participants;
    }

    @OneToMany(mappedBy="combat", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    @IndexColumn(name="roundNumber")
    public List<CombatRound> getRounds() {
        return rounds;
    }
    public void setRounds(List<CombatRound> rounds) {
        this.rounds = rounds;
    }
    
    public int getActiveRoundNumber() {
        return activeRoundNumber;
    }
    public void setActiveRoundNumber(int activeRoundNumber) {
        this.activeRoundNumber = activeRoundNumber;
    }
}