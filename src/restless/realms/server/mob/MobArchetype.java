package restless.realms.server.mob;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Range;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.effect.EffectType;
import restless.realms.server.skill.Skill;
import restless.realms.server.util.HasStringId;
import restless.realms.server.util.ScaleOptions;

@Entity
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MobArchetype implements HasStringId {
    private String id;
    private String name;
    private String image;
    private int level;
    private boolean boss;
    private int upkeepCost;
    private int minHitpoints;
    private int maxHitpoints;
    private int minMana;
    private int maxMana;
    private List<Skill> skills;
    private String treasureTable;
    
    private String weaknesses;
    private EffectType[] weaknessesArray;
    
    private String resistances;
    private EffectType[] resistancesArray;
    
    private String immunities;
    private EffectType[] immunitiesArray;
    
    public MobArchetype() {
    }
    
    public MobArchetype(String id, String name, String image, int level, boolean boss, int upkeepCost, int minHitpoints, int maxHitpoints, int minMana, int maxMana, List<Skill> skills, String treasureTable) {
        super();
        this.id = id;
        this.name = name;
        this.image = image;
        this.level = level;
        this.boss = boss;
        this.upkeepCost = upkeepCost;
        this.minHitpoints = minHitpoints;
        this.maxHitpoints = maxHitpoints;
        this.minMana = minMana;
        this.maxMana = maxMana;
        this.skills = skills;
        this.treasureTable = treasureTable;
    }

    @Id
    @Length(max=FieldLengths.STRING_ID)
    @Override
    public String getId() {
        return id;
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }

    @NotNull
    @Length(max=FieldLengths.MOB_ARCHETYPE_NAME)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    @NotNull
    @Length(max=FieldLengths.STRING_ID)
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
    
    @Range(min=1, max=ScaleOptions.MAX_LEVEL)
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    
    @Basic
    public boolean isBoss() {
        return boss;
    }
    public void setBoss(boolean boss) {
        this.boss = boss;
    }
    
    @Range(min=0)
    public int getUpkeepCost() {
        return upkeepCost;
    }
    public void setUpkeepCost(int upkeepCost) {
        this.upkeepCost = upkeepCost;
    }

    @Range(min=1)
    public int getMinHitpoints() {
        return minHitpoints;
    }
    public void setMinHitpoints(int minHitpoints) {
        this.minHitpoints = minHitpoints;
    }

    @Range(min=1)
    public int getMaxHitpoints() {
        return maxHitpoints;
    }
    public void setMaxHitpoints(int maxHitpoints) {
        this.maxHitpoints = maxHitpoints;
    }

    @Range(min=0)
    public int getMinMana() {
        return minMana;
    }
    public void setMinMana(int minMana) {
        this.minMana = minMana;
    }

    @Range(min=0)
    public int getMaxMana() {
        return maxMana;
    }
    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    @ManyToMany(fetch=FetchType.EAGER)
    @ForeignKey(name="MobArchetype_Id_FK", inverseName = "MobArchetype_Skill_FK")
    @JoinTable(
            name="MobArchetype_Skill",
            joinColumns = @JoinColumn( name="id"),
            inverseJoinColumns = @JoinColumn( name="skillId")
    )
    @IndexColumn(name="orderIndex")
    public List<Skill> getSkills() {
        return skills;
    }
    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    @NotEmpty
    @Length(max=FieldLengths.STRING_ID)
    public String getTreasureTable() {
        return treasureTable;
    }
    public void setTreasureTable(String treasureTable) {
        this.treasureTable = treasureTable;
    }

    @Length(max=FieldLengths.STATISTIC_KEY)
    public String getWeaknesses() {
        return weaknesses;
    }
    public void setWeaknesses(String weaknesses) {
        this.weaknesses = weaknesses;
        if(weaknesses == null || weaknesses.trim().length() == 0) {
            this.weaknessesArray = null;
        } else {
            String[] weaknessesSplit = weaknesses.split(",");
            this.weaknessesArray = new EffectType[weaknessesSplit.length];
            for(int i = 0; i < weaknessesSplit.length; i++) {
                String string = weaknessesSplit[i].trim();
                EffectType type = EffectType.valueOf(string);
                weaknessesArray[i] = type;
            }
        }
    }
    
    @Transient
    public EffectType[] getWeaknessesArray() {
        return weaknessesArray;
    }

    @Length(max=FieldLengths.STATISTIC_KEY)
    public String getResistances() {
        return resistances;
    }
    public void setResistances(String resistances) {
        this.resistances = resistances;
        if(resistances == null || resistances.trim().length() == 0) {
            this.resistancesArray = null;
        } else {
            String[] resistancesSplit = resistances.split(",");
            this.resistancesArray = new EffectType[resistancesSplit.length];
            for(int i = 0; i < resistancesSplit.length; i++) {
                String string = resistancesSplit[i].trim();
                EffectType type = EffectType.valueOf(string);
                resistancesArray[i] = type;
            }
        }
    }
    
    @Transient
    public EffectType[] getResistancesArray() {
        return resistancesArray;
    }
    
    @Length(max=FieldLengths.STATISTIC_KEY)
    public String getImmunities() {
        return immunities;
    }
    public void setImmunities(String immunities) {
        this.immunities = immunities;
        if(immunities == null || immunities.trim().length() == 0) {
            this.immunitiesArray = null;
        } else {
            String[] immunitiesSplit = immunities.split(",");
            this.immunitiesArray = new EffectType[immunitiesSplit.length];
            for(int i = 0; i < immunitiesSplit.length; i++) {
                String string = immunitiesSplit[i].trim();
                EffectType type = EffectType.valueOf(string);
                immunitiesArray[i] = type;
            }
        }
    }
    
    @Transient
    public EffectType[] getImmunitiesArray() {
        return immunitiesArray;
    }
}
