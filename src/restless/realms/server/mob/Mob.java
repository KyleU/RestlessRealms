package restless.realms.server.mob;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.validator.Length;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.effect.EffectTarget;
import restless.realms.server.util.HasIntegerId;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate=true)
public class Mob extends EffectTarget implements HasIntegerId {
    private Integer id;
    private String archetype;
    
    public Mob() {
        
    }
    
    public Mob(Integer id, String archetype, int level, int hitpoints, int mana) {
        super();
        this.id = id;
        this.archetype = archetype;
        this.level = level;
        this.maxHitpoints = hitpoints;
        this.maxMana = mana;
        this.hitpoints = hitpoints;
        this.mana = mana;
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

    @Length(max=FieldLengths.STRING_ID)
    public String getArchetype() {
        return archetype;
    }
    public void setArchetype(String archetype) {
        this.archetype = archetype;
    }
}
