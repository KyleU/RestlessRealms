package restless.realms.server.effect;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.validator.Range;

import restless.realms.server.util.ScaleOptions;

@MappedSuperclass
public abstract class EffectTarget {
    protected int level;
    protected int hitpoints;
    protected int maxHitpoints;
    protected int mana;
    protected int maxMana;

    public void apply(EffectType effectType, int quantity) {
        
    }
    
    @Range(min=1, max=ScaleOptions.MAX_LEVEL)
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    @Range(min=0)
    public int getHitpoints() {
        return hitpoints;
    }
    public void setHitpoints(int hitpoints) {
        this.hitpoints = hitpoints;
    }

    @Range(min=1)
    public int getMaxHitpoints() {
        return maxHitpoints;
    }
    public void setMaxHitpoints(int maxHitpoints) {
        this.maxHitpoints = maxHitpoints;
    }

    @Range(min=0)
    public int getMana() {
        return mana;
    }
    public void setMana(int mana) {
        this.mana = mana;
    }

    @Range(min=0)
    public int getMaxMana() {
        return maxMana;
    }
    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }
    
    @Transient
    public Map<String, Object> getClientRepresentation() {
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("level", getLevel());
        ret.put("hitpoints", getHitpoints());
        ret.put("maxHitpoints", getMaxHitpoints());
        ret.put("mana", getMana());
        ret.put("maxMana", getMaxMana());
        return ret;
    }
}
