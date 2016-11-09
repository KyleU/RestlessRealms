package restless.realms.server.equipment;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.Length;

import restless.realms.server.database.FieldLengths;
import restless.realms.server.util.HasPlayerNameId;


@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.hibernate.annotations.Entity(dynamicUpdate=true)
public class Equipment implements HasPlayerNameId {
    private String playerName;
    
    private Integer head; 
    private Integer chest; 
    private Integer legs; 
    private Integer accessory;
    private Integer weapon;

    private Integer perkOne;
    private Integer perkTwo;

    @Id
    @Length(max=FieldLengths.PLAYER_NAME)
    @Override
    public String getPlayerName() {
        return playerName;
    }
    @Override
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Basic
    public Integer getHead() {
        return head;
    }
    public void setHead(Integer head) {
        this.head = head;
    }
    
    @Basic
    public Integer getChest() {
        return chest;
    }
    public void setChest(Integer chest) {
        this.chest = chest;
    }
    
    @Basic
    public Integer getLegs() {
        return legs;
    }
    public void setLegs(Integer legs) {
        this.legs = legs;
    }
    
    @Basic
    public Integer getAccessory() {
        return accessory;
    }
    public void setAccessory(Integer accessory) {
        this.accessory = accessory;
    }
    
    @Basic
    public Integer getWeapon() {
        return weapon;
    }
    public void setWeapon(Integer weapon) {
        this.weapon = weapon;
    }
    
    @Basic
    public Integer getPerkOne() {
        return perkOne;
    }
    public void setPerkOne(Integer perkOne) {
        this.perkOne = perkOne;
    }
    
    @Basic
    public Integer getPerkTwo() {
        return perkTwo;
    }
    public void setPerkTwo(Integer perkTwo) {
        this.perkTwo = perkTwo;
    }
}