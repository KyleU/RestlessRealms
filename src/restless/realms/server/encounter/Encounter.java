package restless.realms.server.encounter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.validator.Length;

import restless.realms.server.util.HasStringId;
import restless.realms.server.util.RandomUtils;

@Entity
@NamedQueries({
    @NamedQuery(name="encounter.getAll", query="select e from Encounter e order by id")
})
public class Encounter implements HasStringId {
    private String id;
    private boolean boss;
    private List<MobChance> mobChances;
    
    public Encounter() {
    }
    
    public Encounter(String id, boolean boss) {
        super();
        this.id = id;
        this.boss = boss;
        this.mobChances = new ArrayList<MobChance>();
    }

    public List<String> create() {
        List<String> ret = new ArrayList<String>();
        for(MobChance chance : mobChances) {
            if(RandomUtils.percentageCheck(chance.getPercentChance())) {
                int quantity = RandomUtils.getInt(chance.getMin(), chance.getMax());
                for(int i = 0; i < quantity; i++) {
                    ret.add(chance.getMobArchetype());
                }
            }
        }
        return ret;
    }

    @Id
    @Length(max=50)
    @Override
    public String getId() {
        return id;
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }
    
    @Basic
    public boolean isBoss() {
        return boss;
    }
    public void setBoss(boolean boss) {
        this.boss = boss;
    }

    @CollectionOfElements(fetch=FetchType.EAGER)
    @JoinTable(
            joinColumns = @JoinColumn( name="encounterId")
    )
    public List<MobChance> getMobChances() {
        return mobChances;
    }
    public void setMobChances(List<MobChance> mobChances) {
        this.mobChances = mobChances;
    }
}