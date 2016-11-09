package restless.realms.server.skill;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Range;

import restless.realms.server.action.Action;
import restless.realms.server.effect.Effect;
import restless.realms.server.item.IconInfo;

@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
    @NamedQuery(name="skill.getAll", query="select s from Skill s order by s.name", cacheable=true)
})
public class Skill extends Action {
    private int warmup;
    private int cooldown;
    private int manaCost;
    private int manaCostPerLevel;

    public Skill() {
    }

    public Skill(Integer id, String name, int warmup, int cooldown, int manaCost, int manaCostPerLevel, IconInfo icon, String summary, List<Effect> effects) {
        super(id, name, icon, summary, effects);
        this.warmup = warmup;
        this.cooldown = cooldown;
        this.manaCost = manaCost;
        this.manaCostPerLevel = manaCostPerLevel;
    }

    @Range(min=0, max=10) 
    public int getWarmup() {
        return warmup;
    }
    public void setWarmup(int warmup) {
        this.warmup = warmup;
    }
    
    @Range(min=0, max=10) 
    public int getCooldown() {
        return cooldown;
    }
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }
    
    @Basic
    public int getManaCost() {
        return manaCost;
    }
    public void setManaCost(int manaCost) {
        this.manaCost = manaCost;
    }
    
    @Basic
    public int getManaCostPerLevel() {
        return manaCostPerLevel;
    }
    public void setManaCostPerLevel(int manaCostPerLevel) {
        this.manaCostPerLevel = manaCostPerLevel;
    }
}
