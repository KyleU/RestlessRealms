package restless.realms.server.encounter;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Range;

import restless.realms.server.database.FieldLengths;

@Embeddable
public class MobChance {
    private String mobArchetype;
    private int min;
    private int max;
    private int percentChance;
    
    public MobChance() {
    }

    public MobChance(String mobArchetype, int min, int max, int percentChance) {
        super();
        this.mobArchetype = mobArchetype;
        this.min = min;
        this.max = max;
        this.percentChance = percentChance;
    }

    @NotEmpty
    @Length(max=FieldLengths.STRING_ID)
    @Column(length=FieldLengths.STRING_ID)
    public String getMobArchetype() {
        return mobArchetype;
    }
    public void setMobArchetype(String mobArchetype) {
        this.mobArchetype = mobArchetype;
    }

    @Range(min=1, max=4)
    public int getMin() {
        return min;
    }
    public void setMin(int min) {
        this.min = min;
    }

    @Range(min=1, max=4)
    public int getMax() {
        return max;
    }
    public void setMax(int max) {
        this.max = max;
    }

    @Range(min=1, max=100)
    public int getPercentChance() {
        return percentChance;
    }
    public void setPercentChance(int percentChance) {
        this.percentChance = percentChance;
    }
}
