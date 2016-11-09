package restless.realms.server.combat.action;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Range;

import restless.realms.server.combat.CombatRound;
import restless.realms.server.effect.EffectResult;
import restless.realms.server.util.HasIntegerId;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate=true)
@NamedQueries({
    @NamedQuery(name="combatAction.getPriorActionInstances", query="select a from CombatAction a where a.combatRound.combat.id = ? and a.source = ? and (a.combatRound.roundNumber between ? and ?) and a.actionType = ? and a.actionId = ?"),
    @NamedQuery(name="combatAction.getPreviousActions", query="select a from CombatAction a where a.combatRound.combat.id = ? and a.source = ? and a.combatRound.roundNumber >= ?")
})
public class CombatAction implements HasIntegerId {
    private Integer id;
    private CombatRound combatRound;
    private int source;
    private int target;
    private char actionType;
    private Integer actionId;
    private List<EffectResult> effectResults;
    
    public CombatAction() {
        
    }

    public CombatAction(Integer id, int source, int target) {
        super();
        this.id = id;
        this.source = source;
        this.target = target;
        this.effectResults = new ArrayList<EffectResult>();
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
    @JoinColumn(name="combatRoundId", nullable=false)
    public CombatRound getCombatRound() {
        return combatRound;
    }
    public void setCombatRound(CombatRound combatRound) {
        this.combatRound = combatRound;
    }

    @Range(min=0, max=7)
    public int getSource() {
        return source;
    }
    public void setSource(int source) {
        this.source = source;
    }

    @Range(min=0, max=7)
    public int getTarget() {
        return target;
    }
    public void setTarget(int target) {
        this.target = target;
    }

    // i or s
    public char getActionType() {
        return actionType;
    }
    public void setActionType(char actionType) {
        this.actionType = actionType;
    }

    public Integer getActionId() {
        return actionId;
    }
    public void setActionId(Integer actionId) {
        this.actionId = actionId;
    }

    @CollectionOfElements(fetch=FetchType.LAZY)
    @JoinTable(
            joinColumns = @JoinColumn( name="combatActionId")
    )
    @IndexColumn(name="orderIndex")
    public List<EffectResult> getEffectResults() {
        return effectResults;
    }
    public void setEffectResults(List<EffectResult> effectResults) {
        this.effectResults = effectResults;
    }
}
