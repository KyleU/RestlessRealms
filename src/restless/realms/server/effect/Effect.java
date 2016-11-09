package restless.realms.server.effect;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Range;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasIntegerId;

@Entity
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Effect implements HasIntegerId {
    private Integer id;
    
    private EffectType effectType;
    private TargetingType targeting;
    private int percentChance;
    private int percentChancePerLevel;
    private int minQuantity;
    private int maxQuantity;
    private int quantityPerLevel;
    private int minAdditionalRounds;
    private int maxAdditionalRounds;
    
    public Effect() {
        
    }

    public Effect(Integer id, EffectType effectType, TargetingType targetingType) {
        this(id, effectType, targetingType, 1, 1);
    }

    public Effect(Integer id, EffectType effectType, TargetingType targetingType, int minQuantity, int maxQuantity) {
        this(id, effectType, targetingType, 100, 0, minQuantity, maxQuantity, 0, 0, 0);
    }

    public Effect(Integer id, EffectType effectType, TargetingType targetingType, int percentChance) {
        this(id, effectType, targetingType, percentChance, 0, 1, 1, 0, 0, 0);
    }

    public Effect(Integer id, EffectType effectType, TargetingType targetingType, int percentChance, int percentChancePerLevel, int minQuantity, int maxQuantity, int quantityPerLevel, int minAdditionalRounds, int maxAdditionalRounds) {
        super();
        this.id = id;
        this.effectType = effectType;
        this.targeting = targetingType;
        this.percentChance = percentChance;
        this.percentChancePerLevel = percentChancePerLevel;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.quantityPerLevel = quantityPerLevel;
        this.minAdditionalRounds = minAdditionalRounds;
        this.maxAdditionalRounds = maxAdditionalRounds;
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

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(length=FieldLengths.ENUM_LENGTH)
    public EffectType getEffectType() {
        return effectType;
    }
    public void setEffectType(EffectType effectType) {
        this.effectType = effectType;
    }
    
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(length=FieldLengths.ENUM_LENGTH)
    public TargetingType getTargeting() {
        return targeting;
    }
    public void setTargeting(TargetingType targeting) {
        this.targeting = targeting;
    }

    @Range(min=0, max=100)
    public int getPercentChance() {
        return percentChance;
    }
    public void setPercentChance(int percentChance) {
        this.percentChance = percentChance;
    }

    @Range(min=0, max=100)
    public int getPercentChancePerLevel() {
        return percentChancePerLevel;
    }
    public void setPercentChancePerLevel(int percentChancePerLevel) {
        this.percentChancePerLevel = percentChancePerLevel;
    }

    @Range(min=0)
    public int getMinQuantity() {
        return minQuantity;
    }
    public void setMinQuantity(int minQuantity) {
        this.minQuantity = minQuantity;
    }

    @Range(min=0)
    public int getMaxQuantity() {
        return maxQuantity;
    }
    public void setMaxQuantity(int maxQuantity) {
        this.maxQuantity = maxQuantity;
    }
    
    @Range(min=0)
    public int getQuantityPerLevel() {
        return quantityPerLevel;
    }

    public void setQuantityPerLevel(int quantityPerLevel) {
        this.quantityPerLevel = quantityPerLevel;
    }

    @Range(min=0)
    public int getMinAdditionalRounds() {
        return minAdditionalRounds;
    }
    public void setMinAdditionalRounds(int minAdditionalRounds) {
        this.minAdditionalRounds = minAdditionalRounds;
    }

    @Range(min=0)
    public int getMaxAdditionalRounds() {
        return maxAdditionalRounds;
    }
    public void setMaxAdditionalRounds(int maxAdditionalRounds) {
        this.maxAdditionalRounds = maxAdditionalRounds;
    }

    @Transient
    public boolean isDoWeaponDamage() {
        return getEffectType() == EffectType.PHYSICAL && getMinQuantity() == 0 && getMaxQuantity() == 0;
    }
    
    public static Collection<EffectResult> apply(EffectType effectType, int sourceIndex, int targetIndex, EffectTarget effectTarget, int quantity, int additionalRounds) {
        Collection<EffectResult> ret = new ArrayList<EffectResult>();

        if(effectTarget.getHitpoints() > 0) {
            switch(effectType) {
            case REPLENISH:
                if(effectTarget.getMana() + quantity > effectTarget.getMaxMana()) {
                    quantity = effectTarget.getMaxMana() - effectTarget.getMana();
                }
                effectTarget.setMana(effectTarget.getMana() + quantity);
                ret.add(new EffectResult(effectType, sourceIndex, targetIndex, quantity, additionalRounds));
                break;
            case DRAIN:
                if(effectTarget.getMana() - quantity < 0) {
                    quantity = effectTarget.getMana();
                }
                effectTarget.setMana(effectTarget.getMana() - quantity);
                ret.add(new EffectResult(effectType, sourceIndex, targetIndex, quantity, additionalRounds));
                break;
            case HEALING:
                if(effectTarget.getHitpoints() + quantity > effectTarget.getMaxHitpoints()) {
                    quantity = effectTarget.getMaxHitpoints() - effectTarget.getHitpoints();
                }
                effectTarget.setHitpoints(effectTarget.getHitpoints() + quantity);
                ret.add(new EffectResult(effectType, sourceIndex, targetIndex, quantity, additionalRounds));
                break;
            case PHYSICAL:
            case FIRE:
            case ICE:
            case ELECTRIC:
                if(quantity > effectTarget.getHitpoints()) {
                    quantity = effectTarget.getHitpoints();
                }
                effectTarget.setHitpoints(effectTarget.getHitpoints() - quantity);
                ret.add(new EffectResult(effectType, sourceIndex, targetIndex, quantity, additionalRounds));
                if(effectTarget.getHitpoints() == 0) {
                    ret.add(new EffectResult(EffectType.DEATH, sourceIndex, targetIndex, 1, 0));
                }
                break;
            case DEATH:
                effectTarget.setHitpoints(0);
                ret.add(new EffectResult(effectType, sourceIndex, targetIndex, quantity, additionalRounds));
                break;
            case STUN:
                ret.add(new EffectResult(effectType, sourceIndex, targetIndex, quantity, additionalRounds));
                break;
            default:
                throw new IllegalStateException("Unknown effect type \"" + effectType + "\".");
            }
        }
        return ret;
    }
}