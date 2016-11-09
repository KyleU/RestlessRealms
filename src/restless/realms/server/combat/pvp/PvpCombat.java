package restless.realms.server.combat.pvp;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

import restless.realms.server.combat.Combat;
import restless.realms.server.mob.Mob;
import restless.realms.server.util.HasIntegerId;

@Entity
@NamedQueries({
    @NamedQuery(name="PvpCombat.getByCombat", query="select p from PvpCombat p where p.combat.id = ?"),
})

public class PvpCombat implements HasIntegerId {
    private Integer id;
    private Combat combat;
    private List<Mob> enemies;

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
    
    @OneToOne
    public Combat getCombat() {
        return combat;
    }
    public void setCombat(Combat combat) {
        this.combat = combat;
    }
    
    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @JoinTable(
            joinColumns = @JoinColumn( name="pvpId"),
            inverseJoinColumns = @JoinColumn( name="mobId")
    )
    @IndexColumn(name="orderIndex")
    public List<Mob> getEnemies() {
        return enemies;
    }
    public void setEnemies(List<Mob> enemies) {
        this.enemies = enemies;
    }
}
