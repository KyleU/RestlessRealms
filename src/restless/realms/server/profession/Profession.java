package restless.realms.server.profession;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Range;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasStringId;
import restless.realms.server.util.ScaleOptions;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

@Entity
@NamedQueries({
    @NamedQuery(name="profession.getAll", query="select p from Profession p order by minLevel, name")
})
public class Profession implements HasStringId {
    private String id;
    private String name;
    private int minLevel;
    private int initialHitpoints;
    private int hitpointsPerLevel;
    private int initialMana;
    private int manaPerLevel;
    private List<Integer> initialSkills;
    private List<Integer> initialEquipment;
    private List<Integer> initialItems;
    private String initialQuickslots;
    
    public Profession() {
    }
    
    public Profession(String id, String name, int minLevel, int initialHitpoints, int hitpointsPerLevel, int initialMana, int manaPerlevel,
            String initialSkillsString, String initialEquipmentString, String initialItemsString, String initialQuickslots) {
        super();
        this.id = id;
        this.name = name;
        this.minLevel = minLevel;
        this.initialHitpoints = initialHitpoints;
        this.hitpointsPerLevel = hitpointsPerLevel;
        this.initialMana = initialMana;
        this.manaPerLevel = manaPerlevel;
        setInitialSkills(initialSkillsString);
        setInitialEquipment(initialEquipmentString);
        setInitialItems(initialItemsString);
        this.initialQuickslots = initialQuickslots;
    }

    @Id
    @NotEmpty
    @Length(max=FieldLengths.STRING_ID)
    @Override
    public String getId() {
        return id;
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }
    
    @Column(length=30)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    @Range(min=1, max=ScaleOptions.MAX_LEVEL)
    public int getMinLevel() {
        return minLevel;
    }
    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }
    
    @Range(min=1)
    public int getInitialHitpoints() {
        return initialHitpoints;
    }
    public void setInitialHitpoints(int initialHitpoints) {
        this.initialHitpoints = initialHitpoints;
    }

    @Range(min=0)
    public int getHitpointsPerLevel() {
        return hitpointsPerLevel;
    }
    public void setHitpointsPerLevel(int hitpointsPerLevel) {
        this.hitpointsPerLevel = hitpointsPerLevel;
    }

    @Range(min=1)
    public int getInitialMana() {
        return initialMana;
    }
    public void setInitialMana(int initialMana) {
        this.initialMana = initialMana;
    }
    
    @Range(min=0)
    public int getManaPerLevel() {
        return manaPerLevel;
    }
    public void setManaPerLevel(int manaPerLevel) {
        this.manaPerLevel = manaPerLevel;
    }
    
    @Length(max=300)
    public String getInitialSkills() {
        return Joiner.on(",").join(initialSkills);
    }
    public void setInitialSkills(String initialSkillsString) {
        this.initialSkills = new ArrayList<Integer>();
        Iterator<String> i = Splitter.on(",").trimResults().split(initialSkillsString).iterator();
        while(i.hasNext()) {
            String idString = i.next();
            this.initialSkills.add(new Integer(idString));
        }
    }

    @Transient
    public List<Integer> getInitialSkillIds() {
        return initialSkills;
    }
    
    @Length(max=300)
    public String getInitialItems() {
        return Joiner.on(",").join(initialItems);
    }
    public void setInitialItems(String initialItemsString) {
        this.initialItems = new ArrayList<Integer>();
        Iterator<String> i = Splitter.on(",").trimResults().split(initialItemsString).iterator();
        while(i.hasNext()) {
            String idString = i.next();
            this.initialItems.add(new Integer(idString));
        }
    }
    @Transient
    public List<Integer> getInitialItemIds() {
        return initialItems;
    }
    
    @Length(max=100)
    public String getInitialEquipment() {
        return Joiner.on(",").join(initialEquipment);
    }
    public void setInitialEquipment(String initialEquipmentString) {
        this.initialEquipment = new ArrayList<Integer>();
        Iterator<String> i = Splitter.on(",").trimResults().split(initialEquipmentString).iterator();
        while(i.hasNext()) {
            String idString = i.next();
            this.initialEquipment.add(new Integer(idString));
        }
        if(this.initialEquipment.size() != 5) {
            throw new IllegalArgumentException("Must have exactly 5 equipment slots, not " + initialEquipment.size() + ".");
        }
    }
    @Transient
    public List<Integer> getInitialEquipmentIds() {
        return initialEquipment;
    }
    
    @NotEmpty
    @Length(max=FieldLengths.QUICKSLOTS)
    public String getInitialQuickslots() {
        return initialQuickslots;
    }
    public void setInitialQuickslots(String initialQuickslots) {
        this.initialQuickslots = initialQuickslots;
    }
}