package restless.realms.server.equipment;

import java.util.List;

import restless.realms.server.effect.Effect;
import restless.realms.server.effect.EffectType;

public class EquipmentBonuses {
    private int hitpoints;
    private int mana;
    private int physical;
    private int fire;
    private int ice;
    private int electric;
    
    private List<Effect> weaponEffects;
    
    public EquipmentBonuses() {
    }

    public int getHitpoints() {
        return hitpoints;
    }
    public void setHitpoints(int hitpoints) {
        this.hitpoints = hitpoints;
    }

    public int getMana() {
        return mana;
    }
    public void setMana(int mana) {
        this.mana = mana;
    }

    public int getPhysical() {
        return physical;
    }
    public void setPhysical(int physical) {
        this.physical = physical;
    }

    public int getFire() {
        return fire;
    }
    public void setFire(int fire) {
        this.fire = fire;
    }

    public int getIce() {
        return ice;
    }
    public void setIce(int ice) {
        this.ice = ice;
    }

    public int getElectric() {
        return electric;
    }
    public void setElectric(int electric) {
        this.electric = electric;
    }
    
    public int getBonus(EffectType type) {
        int bonus = 0;
        switch(type) {
        case ELECTRIC:
            bonus = getElectric();
            break;
        case FIRE:
            bonus = getFire();
            break;
        case ICE:
            bonus = getIce();
            break;
        case PHYSICAL:
            bonus = getPhysical();
            break;
        }
        return bonus;
    }
    
    public List<Effect> getWeaponEffects() {
        return weaponEffects;
    }
    public void setWeaponEffects(List<Effect> weaponEffects) {
        this.weaponEffects = weaponEffects;
    }
}
