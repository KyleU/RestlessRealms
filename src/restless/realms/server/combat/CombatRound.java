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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.NotNull;

import restless.realms.server.combat.action.CombatAction;
import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasIntegerId;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate=true)
@NamedQueries({
    @NamedQuery(name="combatRound.getByIndex", query="select r from CombatRound r where r.combat.id = ? and r.roundNumber = ?")
})
public class CombatRound implements HasIntegerId {
    public static enum State {WAITING, COMPLETE, VICTORY, LOSS};
    
    private Integer id;
    
    private Combat combat;
    private int roundNumber;
    
    private State state;
    private List<CombatAction> actions;
    
    public CombatRound() {
        this.state = State.WAITING;
        this.actions = new ArrayList<CombatAction>(0);
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
    @NotNull
    @JoinColumn(name="combatId")
    public Combat getCombat() {
        return combat;
    }
    public void setCombat(Combat combat) {
        this.combat = combat;
    }
    
    //0 based
    public int getRoundNumber() {
        return roundNumber;
    }
    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length=FieldLengths.ENUM_LENGTH)
    public State getState() {
        return state;
    }
    public void setState(State state) {
        this.state = state;
    }

    @OneToMany(mappedBy="combatRound", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    public List<CombatAction> getActions() {
        return actions;
    }
    public void setActions(List<CombatAction> actions) {
        this.actions = actions;
    }
}