package restless.realms.server.effect;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import restless.realms.server.database.FieldLengths;

import com.sun.istack.internal.NotNull;

@Embeddable
public class EffectResult {
    private EffectType type;
    private int source;
    private int target;
    private int quantity;
    private int additionalRounds;
    
    public EffectResult() {
    }

    public EffectResult(EffectType type, int source, int target, int quantity, int additionalRounds) {
        super();
        this.type = type;
        this.source = source;
        this.target = target;
        this.quantity = quantity;
        this.additionalRounds = additionalRounds;
    }
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length=FieldLengths.ENUM_LENGTH)
    public EffectType getType() {
        return type;
    }
    public void setType(EffectType type) {
        this.type = type;
    }
    
    @Basic
    public int getSource() {
        return source;
    }
    public void setSource(int source) {
        this.source = source;
    }

    @Basic
    public int getTarget() {
        return target;
    }
    public void setTarget(int target) {
        this.target = target;
    }

    @Basic
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Basic
    public int getAdditionalRounds() {
        return additionalRounds;
    }
    public void setAdditionalRounds(int additionalRounds) {
        this.additionalRounds = additionalRounds;
    }
}
