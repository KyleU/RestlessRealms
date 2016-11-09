package restless.realms.server.combat;

import java.util.ArrayList;
import java.util.List;

import restless.realms.server.combat.Combat.CombatType;
import restless.realms.server.combat.pvp.PvpCombat;
import restless.realms.server.effect.EffectTarget;
import restless.realms.server.mob.Mob;
import restless.realms.server.playercharacter.PlayerCharacter;

public class CombatState {
    private Combat combat;
    private String introKey;
    private CombatRound activeRound;
    private List<PlayerCharacter> allies;
    private List<EffectTarget> enemies;
    private PvpCombat pvpCombat;

    public CombatState() {
        allies = new ArrayList<PlayerCharacter>();
        enemies = new ArrayList<EffectTarget>();
    }

    public Combat getCombat() {
        return combat;
    }
    public void setCombat(Combat combat) {
        this.combat = combat;
    }
    
    public String getIntroKey() {
        return introKey;
    }
    public void setIntroKey(String introKey) {
        this.introKey = introKey;
    }

    public CombatRound getActiveRound() {
        return activeRound;
    }
    public void setActiveRound(CombatRound activeRound) {
        this.activeRound = activeRound;
    }

    public List<PlayerCharacter> getAllies() {
        return allies;
    }
    public void addAlly(PlayerCharacter ally) {
        this.allies.add(ally);
    }

    public List<EffectTarget> getEnemies() {
        return enemies;
    }
    public void addEnemy(EffectTarget enemy) {
        this.enemies.add(enemy);
    }

    public void setPvpCombat(PvpCombat pvpCombat) {
        this.pvpCombat = pvpCombat;
    }

    public Mob getEnemyMob(int enemyIndex) {
        if(combat.getCombatType() == CombatType.PvE) {
            return (Mob)this.enemies.get(enemyIndex);
        } else {
            return this.pvpCombat.getEnemies().get(enemyIndex);
        }
    }

    public EffectTarget getEffectTarget(int index) {
        if(index < allies.size()) {
            return allies.get(index);
        } else if (index < allies.size() + enemies.size()) {
            EffectTarget effectTarget = enemies.get(index - allies.size());
            return effectTarget;
        } else {
            throw new IllegalArgumentException("Invalid index \"" + index + "\".");
        }
    }

    public boolean isAnybodyAlive() {
        boolean anybodyAlive = false;
        for(PlayerCharacter pc : getAllies()) {
            if(pc.getHitpoints() > 0) {
                anybodyAlive = true;
                break;
            }
        }
        return anybodyAlive;
    }
}
