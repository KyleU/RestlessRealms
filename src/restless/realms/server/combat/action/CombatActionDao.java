package restless.realms.server.combat.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.action.Action;
import restless.realms.server.combat.Combat.CombatType;
import restless.realms.server.combat.CombatState;
import restless.realms.server.combat.pvp.PvpCombatDao;
import restless.realms.server.combat.pvp.PvpDefenses;
import restless.realms.server.database.AbstractDao;
import restless.realms.server.effect.Effect;
import restless.realms.server.effect.EffectResult;
import restless.realms.server.effect.EffectTarget;
import restless.realms.server.effect.EffectType;
import restless.realms.server.effect.TargetingType;
import restless.realms.server.equipment.EquipmentBonuses;
import restless.realms.server.equipment.EquipmentDao;
import restless.realms.server.item.Item;
import restless.realms.server.item.ItemDao;
import restless.realms.server.item.ItemType;
import restless.realms.server.mob.Mob;
import restless.realms.server.mob.MobArchetype;
import restless.realms.server.mob.MobDao;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.skill.Skill;
import restless.realms.server.skill.SkillDao;
import restless.realms.server.util.RandomUtils;

@Repository
public class CombatActionDao extends AbstractDao<CombatAction> {
    private static final Log log = LogFactory.getLog(CombatActionDao.class);
    
    @Autowired 
    private MobDao mobDao;
    
    @Autowired
    private EquipmentDao equipmentDao;
    
    @Autowired
    private StatisticsDao statisticsDao;
    
    @Autowired
    private SkillDao skillDao;
    
    @Autowired
    private ItemDao itemDao;
    
    @Autowired
    private PvpCombatDao pvpCombatDao;
    
    public Action getAiAction(CombatState combatState, int sourceIndex) {
        Action ret = null;
        int numAllies = combatState.getAllies().size();
        EffectTarget enemy = combatState.getEnemies().get(sourceIndex - numAllies);
        Mob enemyMob = combatState.getEnemyMob(sourceIndex - numAllies);
        if(enemyMob.getHitpoints() > 0) {
            boolean stunned = false;
            for(CombatAction action : combatState.getActiveRound().getActions()) {
                for(EffectResult effectResult : action.getEffectResults()) {
                    if(effectResult.getTarget() == sourceIndex) {
                        if(effectResult.getType().equals(EffectType.STUN)) {
                            stunned = true;
                            break;
                        }
                    }
                }
                if(stunned) {
                    break;
                }
            }
            
            if(!stunned) {
                int roundNumber = combatState.getActiveRound().getRoundNumber();
                if(enemy instanceof Mob) {
                    Mob mob = (Mob)enemy;
                    List<Skill> skills = mobDao.getArchetype(mob.getArchetype()).getSkills();
        
                    for(Skill skill : skills) {
                        boolean available = isAvailable(combatState, sourceIndex, roundNumber, mob, skill);
                        if(available) {
                            ret = skill;
                            break;
                        }
                    }
                } else {
                    PlayerCharacter playerCharacter = (PlayerCharacter)enemy;
                    PvpDefenses defenses = pvpCombatDao.getDefenses(playerCharacter);
                    Mob mob = combatState.getEnemyMob(sourceIndex - 1);
                    for(int i = 0; i < defenses.getSkills().length; i++) {
                        int skillId = defenses.getSkills()[i];
                        Skill skill = skillDao.get(skillId);

                        boolean available = isAvailable(combatState, sourceIndex, roundNumber, playerCharacter, skill);
                        if(available) {
                            int manaCost = skill.getManaCost() + (skill.getManaCostPerLevel() * playerCharacter.getLevel());
                            if(manaCost <= mob.getMana()) {
                                ret = skill;
                                break;
                            }
                        }
                    }
                    if(ret == null) {
                        log.error("No available PvP action for player \"" + playerCharacter.getName() + "\".");
                    } else {
                        log.debug("PvP action for player \"" + playerCharacter.getName() + "\", skill \"" + ret.getName() + "\".");
                    }
                }
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public void validatePlayerAction(CombatAction action, CombatState combatState) {
        EffectTarget source = combatState.getEffectTarget(action.getSource());
        if(action.getSource() > 0 && source instanceof PlayerCharacter) {
            source = combatState.getEnemyMob(action.getSource() - 1);
        }
        if('s' == action.getActionType()) {
            int roundNumber = combatState.getActiveRound().getRoundNumber();
            Skill s = template.get(Skill.class, action.getActionId());

            //warmups
            if(s.getWarmup() > roundNumber) {
                throw new IllegalArgumentException(s.getName() + " requires a " + s.getWarmup() + " round warmup. This is only round " + (roundNumber + 1) + ".");
            }
            
            //cooldowns
            if(s.getCooldown() > 0 && roundNumber > 0) {
                Object[] params = {
                        combatState.getCombat().getId(),
                        action.getSource(),
                        roundNumber - s.getCooldown(), 
                        roundNumber,
                        's',
                        s.getId()
                };
                List<CombatAction> list = template.findByNamedQuery("combatAction.getPriorActionInstances", params);
                if(list.size() > 0) {
                    int delta = roundNumber - list.get(0).getCombatRound().getRoundNumber();
                    if(delta > 0) {
                        throw new IllegalArgumentException(s.getName() + " has a " + s.getCooldown() + " round cooldown, so you'll need to wait " + (s.getCooldown() - delta + 1) + " more rounds before using it.");
                    }
                }
            }

            int manaCost = s.getManaCost() + (source.getLevel() * s.getManaCostPerLevel());
            if(source.getMana() < manaCost) {
                throw new IllegalArgumentException("Not enough mana.");
            }
        } else {
            Item i = itemDao.get(action.getActionId());
            // TODO Validate ownership?
            if(ItemType.CONSUMABLE.equals(i.getType())) {
                if(combatState.getCombat().getCombatType() == CombatType.PvP) {
                    throw new IllegalStateException("You may not use consumable items in duels.");
                }
            } else {
                throw new IllegalArgumentException("Sorry, " + i.getType().toString().toLowerCase() + "s aren't supported yet.");
            }
        }
    }

    public Action resolveCombatAction(CombatAction action, CombatState combatState) {
        Action ret = null;
        EffectTarget source = combatState.getEffectTarget(action.getSource());
        int numAllies = combatState.getAllies().size();
        EffectTarget sourceMob = source;
        if(action.getSource() > (numAllies - 1) && sourceMob instanceof PlayerCharacter) {
            sourceMob = combatState.getEnemyMob(action.getSource() - numAllies);
        }
        
        EquipmentBonuses bonuses = null;
        if(source instanceof PlayerCharacter) {
            PlayerCharacter pc = (PlayerCharacter)source;
            bonuses = equipmentDao.getBonuses(pc.getName());
        }
        int stunRounds = 0;
        
        List<Effect> effects = null;
        if('s' == action.getActionType()) {
            Skill s = skillDao.get(action.getActionId());;
            //scaling mana cost
            int manaCost = s.getManaCost() + (s.getManaCostPerLevel() * source.getLevel());
            int mana = sourceMob.getMana() - manaCost;
            if(mana < 0) {
                throw new IllegalStateException("Not enough mana.");
            }
            sourceMob.setMana(mana);
            ret = s;
        } else if('i' == action.getActionType()) {
            Item i = itemDao.get(action.getActionId());
            ret = i;
        } else {
            throw new IllegalStateException("Illegal action type \"" + action.getActionType() + "\".");
        }
        effects = ret.getEffects();
        if(effects == null) {
            throw new IllegalStateException("No effects for action " + action);
        }
        for(Effect effect : effects) {
            int target = action.getTarget();
            if(TargetingType.SELF.equals(effect.getTargeting())) {
                target = action.getSource();
            }

            Collection<EffectResult> effectResults = calculateEffectResults(effect, action.getSource(), target, combatState, bonuses);
            action.getEffectResults().addAll(effectResults);
            if(source instanceof PlayerCharacter) {
                PlayerCharacter pc = (PlayerCharacter)source;
                for(EffectResult effectResult : effectResults) {
                    if(effectResult.getSource() == 0) {
                        statisticsDao.increment(pc.getName(), "deal-" + effectResult.getType().toString(), effectResult.getQuantity());
                    } else {
                        statisticsDao.increment(pc.getName(), "deal-pvp-" + effectResult.getType().toString(), effectResult.getQuantity());
                    }
                }
            }
            for(EffectResult effectResult : effectResults) {
                EffectTarget effectTarget = combatState.getEffectTarget(effectResult.getTarget());
                EffectType type = effectResult.getType();
                if(effectTarget instanceof PlayerCharacter) {
                    PlayerCharacter pc = (PlayerCharacter)effectTarget;
                    if(effectResult.getTarget() == 0) {
                        if(type == EffectType.STUN && effectResult.getAdditionalRounds() + 1 > stunRounds) {
                            stunRounds = effectResult.getAdditionalRounds() + 1;
                        }
                        statisticsDao.increment(pc.getName(), "receive-" + type.toString(), effectResult.getQuantity());
                    } else {
                        statisticsDao.increment(pc.getName(), "receive-pvp-" + type.toString(), effectResult.getQuantity());
                    }
                }
            }
        }
        if(log.isDebugEnabled()) {
            StringBuilder message = new StringBuilder();
            message.append("Applying ");
            message.append(ret instanceof Skill ? "skill \"" : "item \"");
            message.append(ret.getName());
            message.append("\" ");
            message.append(action.getSource());
            message.append("->");
            message.append(action.getTarget());
            message.append(" with the following results {");
            for(Iterator<EffectResult> iterator = action.getEffectResults().iterator(); iterator.hasNext();) {
                EffectResult effectResult = iterator.next();
                message.append(effectResult.getSource());
                message.append("->");
                message.append(effectResult.getTarget());
                message.append(" ");
                message.append(effectResult.getType());
                message.append(":");
                message.append(effectResult.getQuantity());
                if(effectResult.getAdditionalRounds() > 0) {
                    message.append(" (+");
                    message.append(effectResult.getAdditionalRounds());
                    message.append("r)");
                }
                if(iterator.hasNext()) {
                    message.append(", ");
                }
            }
            message.append("}.");
            log.debug(message.toString());
        }
        
        for(int i = 0; i < stunRounds; i++) {
            log.info("Player is stunned. Enemies taking another turn.");
            for(int j = 0; j < combatState.getEnemies().size(); j++) {
                if(combatState.isAnybodyAlive()) {
                    Action aiAction = getAiAction(combatState, j + 1);
                    if(aiAction != null) {
                        CombatAction aiCombatAction = new CombatAction(null, j + 1, 0);
                        aiCombatAction.setActionType('s');
                        aiCombatAction.setActionId(aiAction.getId());
                        aiCombatAction.setCombatRound(combatState.getActiveRound());
        
                        combatState.getActiveRound().getActions().add(aiCombatAction);
                        resolveCombatAction(aiCombatAction, combatState);
                    }
                }
            }
        }

        return ret;
    }

    public void continueActionIfNeeded(CombatAction action, CombatState combatState) {
        EffectTarget effectTarget = combatState.getEffectTarget(action.getTarget());
        int numAllies = combatState.getAllies().size();

        if(action.getTarget() > (numAllies - 1) && effectTarget instanceof PlayerCharacter) {
            effectTarget = combatState.getEnemyMob(action.getTarget() - numAllies);
        }
        if(effectTarget.getHitpoints() > 0) {
            CombatAction newAction = null;
            for(EffectResult effectResult : action.getEffectResults()) {
                if(effectResult.getAdditionalRounds() > 0) {
                    if(newAction == null) {
                        newAction = new CombatAction(null, action.getSource(), action.getTarget());
                        newAction.setActionType('s');
                        newAction.setActionId(10);
//                        newAction.setActionType(action.getActionType());
//                        newAction.setActionId(action.getActionId());
                        newAction.setCombatRound(combatState.getActiveRound());
                    }
                    int newAdditionalRounds = effectResult.getAdditionalRounds() - 1;
                    EffectTarget target = combatState.getEffectTarget(effectResult.getTarget());
                    if(effectResult.getTarget() > 0 && target instanceof PlayerCharacter) {
                        target = combatState.getEnemyMob(effectResult.getTarget() - numAllies);
                    }
                    Collection<EffectResult> results = Effect.apply(effectResult.getType(), effectResult.getSource(), effectResult.getTarget(), target, effectResult.getQuantity(), newAdditionalRounds);
                    newAction.getEffectResults().addAll(results);
                }
            }
            if(newAction != null) {
                combatState.getActiveRound().getActions().add(newAction);
                if(log.isDebugEnabled()) {
                    StringBuilder message = new StringBuilder();
                    message.append("Continuing ");
                    message.append(newAction.getActionType() == 's' ? "skill " : "item ");
                    message.append(newAction.getActionId());
                    message.append(" ");
                    message.append(action.getSource());
                    message.append("->");
                    message.append(action.getTarget());
                    message.append(" with the following results {");
                    for(Iterator<EffectResult> iterator = action.getEffectResults().iterator(); iterator.hasNext();) {
                        EffectResult effectResult = iterator.next();
                        message.append(effectResult.getSource());
                        message.append("->");
                        message.append(effectResult.getTarget());
                        message.append(" ");
                        message.append(effectResult.getType());
                        message.append(":");
                        message.append(effectResult.getQuantity());
                        if(effectResult.getAdditionalRounds() > 0) {
                            message.append(" (+");
                            message.append(effectResult.getAdditionalRounds());
                            message.append("r)");
                        }
                        if(iterator.hasNext()) {
                            message.append(", ");
                        }
                    }
                    message.append("}.");
                    log.debug(message.toString());
                }


            }
        }
    }

    private Collection<EffectResult> calculateEffectResults(Effect effect, int source, int target, CombatState combatState, EquipmentBonuses bonuses) {
        Collection<EffectResult> ret = new ArrayList<EffectResult>();
        int sourceLevel = combatState.getEffectTarget(source).getLevel();

        if(effect.getTargeting() == TargetingType.SELF) {
            target = source;
        }
        
        List<Integer> targets = getTargets(effect.getTargeting(), source, target, combatState);
        for(Integer targetIndex : targets) {
            int percentChance = effect.getPercentChance();
            if(effect.getPercentChancePerLevel() > 0) {
                percentChance = percentChance + (effect.getPercentChancePerLevel() * sourceLevel);
            }

            if(RandomUtils.percentageCheck(percentChance)) {
                Collection<EffectResult> effectResults = null;
                if(effect.isDoWeaponDamage()) {
                    for(Effect weaponEffect : bonuses.getWeaponEffects()) {
                        ret.addAll(calculateEffectResults(weaponEffect, source, targetIndex, combatState, bonuses));
                    }
                } else {
                    //check immunities
                    EffectTarget effectTarget = combatState.getEffectTarget(targetIndex);
                    
                    if(isImmune(effectTarget, effect.getEffectType())) {
                        log.debug("Target is immune to " + effect.getEffectType() + ".");
                    } else {
                        int quantity = RandomUtils.getInt(effect.getMinQuantity(), effect.getMaxQuantity());
                        //scaling quantity
                        if(effect.getQuantityPerLevel() > 0) {
                            quantity = quantity + (effect.getQuantityPerLevel() * sourceLevel);
                        }
                        if(bonuses != null) {
                            quantity += bonuses.getBonus(effect.getEffectType());
                        }

                        if(isWeak(effectTarget, effect.getEffectType())) {
                            log.debug("Target is weak to " + effect.getEffectType() + ". Doubling quantity from " + quantity + " to " + (quantity * 2) + ".");
                            quantity = quantity * 2;
                        }
                        if(isResistant(effectTarget, effect.getEffectType())) {
                            log.debug("Target is resistant to " + effect.getEffectType() + ". Halving quantity from " + quantity + " to " + (quantity / 2) + ".");
                            quantity = quantity / 2;
                        }

                        int additionalRounds = RandomUtils.getInt(effect.getMinAdditionalRounds(), effect.getMaxAdditionalRounds());
            
                        if(targetIndex > 0 && effectTarget instanceof PlayerCharacter) {
                            effectTarget = combatState.getEnemyMob(targetIndex - 1);
                        }
                        effectResults = Effect.apply(effect.getEffectType(), source, targetIndex, effectTarget, quantity, additionalRounds);
                        ret.addAll(effectResults);
                    }
                }
            }
        }
        return ret;
    }
    
    private List<Integer> getTargets(TargetingType type, int source, int target, CombatState combatState) {
        List<Integer> ret = new ArrayList<Integer>();
        if(type == TargetingType.SELF) {
            ret.add(source);
        } else if(type == TargetingType.ALLY) {
            if(source < combatState.getAllies().size()) {
                if(target > combatState.getAllies().size() - 1) {
                    throw new IllegalArgumentException("Participant at position " + target + " is not an ally.");
                }
            }
            ret.add(target);
        } else if(type == TargetingType.ALLIES) {
            if(source < combatState.getAllies().size()) {
                for(int i = 0; i < combatState.getAllies().size(); i++) {
                    ret.add(i);
                }
            } else {
                for(int i = 0; i < combatState.getEnemies().size(); i++) {
                    ret.add(i + combatState.getAllies().size());
                }
            }
        } else if(type == TargetingType.ENEMY) {
            if(source < combatState.getAllies().size()) {
                if(target < combatState.getAllies().size()) {
                    throw new IllegalArgumentException("Participant at position " + target + " is not an enemy.");
                }
            }
            ret.add(target);
        } else if(type == TargetingType.ENEMIES) {
            if(source < combatState.getAllies().size()) {
                for(int i = 0; i < combatState.getEnemies().size(); i++) {
                    ret.add(i + combatState.getAllies().size());
                }
            } else {
                for(int i = 0; i < combatState.getAllies().size(); i++) {
                    ret.add(i);
                }
            }
        } else if(type == TargetingType.ALL) {
            for(int i = 0; i < combatState.getAllies().size() + combatState.getEnemies().size(); i++) {
                ret.add(i);
            }
        } else {
            throw new IllegalArgumentException(type.toString() + " is not supported.");
        }
        return ret;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public Map<Integer, Integer> getCooldowns(Integer combatId, int participantIndex, int minRoundIndex) {
        Map<Integer, Integer> ret = new LinkedHashMap<Integer, Integer>();
        List<CombatAction> previousActions = template.findByNamedQuery("combatAction.getPreviousActions", combatId, participantIndex, minRoundIndex);
        for(CombatAction combatAction : previousActions) {
            if(combatAction.getActionType() == 's') {
                int roundNumber = combatAction.getCombatRound().getRoundNumber();
                ret.put(combatAction.getActionId(), roundNumber);
            }
        }
        return ret;
    }

    private boolean isImmune(EffectTarget effectTarget, EffectType effectType) {
        EffectType[] immunities = null;
        if(effectTarget instanceof Mob) {
            MobArchetype archetype = mobDao.getArchetype(((Mob)effectTarget).getArchetype());
            immunities = archetype.getImmunitiesArray();
        }
        boolean immune = false;
        if(immunities != null) {
            for(EffectType immunity : immunities) {
                if(immunity == effectType) {
                    immune = true;
                    break;
                }
            }
        }
        return immune;
    }

    private boolean isResistant(EffectTarget effectTarget, EffectType effectType) {
        EffectType[] resistances = null;
        if(effectTarget instanceof Mob) {
            MobArchetype archetype = mobDao.getArchetype(((Mob)effectTarget).getArchetype());
            resistances = archetype.getResistancesArray();
        }
        boolean resistant = false;
        if(resistances != null) {
            for(EffectType resistance : resistances) {
                if(resistance == effectType) {
                    resistant = true;
                    break;
                }
            }
        }
        return resistant;
    }

    private boolean isWeak(EffectTarget effectTarget, EffectType effectType) {
        EffectType[] weaknesses = null;
        if(effectTarget instanceof Mob) {
            MobArchetype archetype = mobDao.getArchetype(((Mob)effectTarget).getArchetype());
            weaknesses = archetype.getWeaknessesArray();
        }
        boolean weak = false;
        if(weaknesses != null) {
            for(EffectType weakness : weaknesses) {
                if(weakness == effectType) {
                    weak = true;
                    break;
                }
            }
        }
        return weak;
    }

    @SuppressWarnings("unchecked")
    private boolean isAvailable(CombatState combatState, int sourceIndex, int roundNumber, EffectTarget source, Skill skill) {
        boolean available = true;
        if(skill.getWarmup() > 0) {
            if(skill.getWarmup() >= roundNumber) {
                available = false;
            }
        }
        if(available && skill.getCooldown() > 0) {
            Object[] params = {
                    combatState.getCombat().getId(),
                    sourceIndex,
                    roundNumber - skill.getCooldown(), 
                    roundNumber,
                    's',
                    skill.getId()
            };
            List<CombatAction> list = template.findByNamedQuery("combatAction.getPriorActionInstances", params);
            if(list.size() > 0) {
                available = false;
            }
        }
        if(available && skill.getManaCost() > source.getMana()) {
            available = false;
        }
        return available;
    }
    
    @Override
    protected Class<?> getManagedClass() {
        return CombatAction.class;
    }
}
