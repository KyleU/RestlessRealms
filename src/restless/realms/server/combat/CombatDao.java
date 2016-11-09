package restless.realms.server.combat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.action.Action;
import restless.realms.server.adventure.Adventure;
import restless.realms.server.adventure.Adventure.Status;
import restless.realms.server.adventure.AdventureDao;
import restless.realms.server.adventure.AdventureFactory;
import restless.realms.server.adventure.map.AdventureMap;
import restless.realms.server.combat.Combat.CombatType;
import restless.realms.server.combat.CombatParticipant.Role;
import restless.realms.server.combat.CombatParticipant.Type;
import restless.realms.server.combat.CombatRound.State;
import restless.realms.server.combat.action.CombatAction;
import restless.realms.server.combat.action.CombatActionDao;
import restless.realms.server.combat.pvp.PvpCombat;
import restless.realms.server.combat.pvp.PvpCombatDao;
import restless.realms.server.database.AbstractDao;
import restless.realms.server.effect.EffectTarget;
import restless.realms.server.encounter.Encounter;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.item.Item;
import restless.realms.server.item.ItemType;
import restless.realms.server.mob.Mob;
import restless.realms.server.mob.MobArchetype;
import restless.realms.server.mob.MobDao;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.quest.progress.QuestProgressDao;
import restless.realms.server.room.Room;
import restless.realms.server.room.RoomDao;
import restless.realms.server.room.RoomState;
import restless.realms.server.room.RoomType;
import restless.realms.server.session.Session;
import restless.realms.server.treasure.Treasure;
import restless.realms.server.treasure.TreasureDao;
import restless.realms.server.util.ScaleOptions;

@Repository
public class CombatDao extends AbstractDao<Combat> {
    private static final Log log = LogFactory.getLog(CombatDao.class);
    
    @Autowired
    private AdventureDao adventureDao;
    
    @Autowired
    private RoomDao roomDao;
    
    @Autowired
    private PlayerCharacterDao playerCharacterDao;

    @Autowired
    private InventoryDao inventoryDao;
    
    @Autowired
    private TreasureDao treasureDao;

    @Autowired
    private AdventureFactory adventureFactory;

    @Autowired
    private MobDao mobDao;
    
    @Autowired
    private CombatActionDao combatActionDao;
    
    @Autowired
    private QuestProgressDao questProgressDao;
    
    @Autowired
    private StatisticsDao statisticsDao;
    
    @Autowired
    private PvpCombatDao pvpCombatDao;

	public CombatDao() {
	}
	
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public CombatState getCombatState(Session session) {
        PlayerCharacter pc = playerCharacterDao.get(session);
        CombatState ret = new CombatState();
        Combat combat;
        Integer activeAdventureId = pc.getActiveAdventureId();
        if(activeAdventureId == null) {
            throw new IllegalArgumentException("You're not on an adventure.");
        }
        
        if(activeAdventureId > 0) {
            Adventure adventure = adventureDao.getAdventure(activeAdventureId);
            Room room = roomDao.getRoom(adventure.getId(), adventure.getActiveRoomIndex());
            if(room.getState() != RoomState.ACTIVE) {
                throw new IllegalStateException("Room is " + room.getState().toString().toLowerCase() + ", not active.");
            }
            
            combat = room.getCombat();
            if(combat == null) {
                startCombat(adventure, room);
                combat = room.getCombat();
            }
            Encounter encounter = adventureFactory.getEncounter(room);
            if(encounter.isBoss()) {
                ret.setIntroKey(encounter.getId());
            }
            int knownParticipants = 0;
            for(PlayerCharacter ally : adventure.getParticipants()) {
                knownParticipants++;
                ret.addAlly(ally);            
            }
            for(Mob mob : room.getInhabitants()) {
                knownParticipants++;
                ret.addEnemy(mob);
            }
            if(combat.getParticipants().size() != knownParticipants) {
                throw new IllegalStateException("Invalid number of participants.");
            }
        } else {
            int combatId = Math.abs(activeAdventureId);
            combat = get(combatId);
            if(combat == null) {
                throw new IllegalStateException("Player references invalid PvP combat\"" + combatId + "\".");
            }
            if(combat.getCombatType() != CombatType.PvP) {
                throw new IllegalStateException("Combat \"" + combatId + "\" is not a PvP combat.");
            }
            List<CombatParticipant> participants = combat.getParticipants();
            PlayerCharacter ally = playerCharacterDao.get(participants.get(0).getName());
            ret.addAlly(ally);

            CombatParticipant enemyCombatParticipant = participants.get(1);
            if(enemyCombatParticipant.getType() != Type.PLAYER) {
                throw new IllegalStateException("Combat \"" + combatId + "\" is not a PvP combat against a valid player.");
            }
            PlayerCharacter enemy = playerCharacterDao.get(enemyCombatParticipant.getName());
            ret.addEnemy(enemy);
            PvpCombat pvpCombat = (PvpCombat)template.findByNamedQuery("PvpCombat.getByCombat", combat.getId()).get(0);
            if(pvpCombat.getEnemies().size() > 1) {
                //odd loop to skip player
                for(int i = 1; i < pvpCombat.getEnemies().size(); i++) {
                    Mob mob = pvpCombat.getEnemies().get(i);
                    ret.addEnemy(mob);
                }
            }
            ret.setPvpCombat(pvpCombat);
        }
        ret.setCombat(combat);
        
        CombatRound activeRound = getCombatRound(combat.getId(), combat.getActiveRoundNumber());
        ret.setActiveRound(activeRound);
        
        return ret;
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public CombatState onPlayerAction(Session session, CombatAction combatAction) {
        playerCharacterDao.validateAction(combatAction, session.getCharacterName());

        CombatState combatState = getCombatState(session);

        for(int i = 0; i < combatState.getAllies().size(); i++) {
            PlayerCharacter ally = combatState.getAllies().get(i);
            if(ally.getName().equals(session.getCharacterName())) {
                combatAction.setSource(i);
            }
        }
        if(combatAction.getSource() == -1) {
            throw new IllegalStateException("You are not in this combat.");
        }
        
        CombatRound activeRound = combatState.getActiveRound();
        
        if(activeRound == null || activeRound.getState() != State.WAITING) {
            throw new IllegalStateException("Active round has status " + (activeRound == null ? null : activeRound.getState()) + ".");
        }
        
        for(CombatAction tempCombatAction : activeRound.getActions()) {
            if(tempCombatAction.getSource() == combatAction.getSource()) {
                throw new IllegalStateException("Participant \"" + tempCombatAction.getSource() + "\" has already performed an action this round (\"" + activeRound.getId() + "\").");
            }
        }
        
        carryOverActions(combatState);
        
        combatAction.setCombatRound(activeRound);
        activeRound.getActions().add(combatAction);
        combatActionDao.validatePlayerAction(combatAction, combatState);
        
        Action action = combatActionDao.resolveCombatAction(combatAction, combatState);
        if(action == null) {
            //not sure, exception for now.
            throw new RuntimeException("Unhandled action. Please report this error.");
        } else if(action instanceof Item) {
            if(ItemType.CONSUMABLE.equals(((Item)action).getType())) {
                inventoryDao.consumeItem(session.getCharacterName(), action.getId());
            } else {
                //not sure, exception for now.
                throw new RuntimeException("Unhandled action. Please report this error.");
            }
        }

        for(int i = 0; i < combatState.getEnemies().size(); i++) {
            if(combatState.isAnybodyAlive()) {
                int numAllies = combatState.getAllies().size();
                Action skill = combatActionDao.getAiAction(combatState, i + numAllies);
                if(skill != null) {
                    CombatAction aiCombatAction = new CombatAction(null, i + numAllies, 0);
                    aiCombatAction.setActionType('s');
                    aiCombatAction.setActionId(skill.getId());
                    aiCombatAction.setCombatRound(activeRound);
    
                    activeRound.getActions().add(aiCombatAction);
                    combatActionDao.resolveCombatAction(aiCombatAction, combatState);
                }
            }
        }
                
        boolean loss = true;
        for(PlayerCharacter pc : combatState.getAllies()) {
            if(pc.getHitpoints() > 0) {
                loss = false;
            }
        }
        boolean win = true;
        for(int enemyIndex = 0; enemyIndex < combatState.getEnemies().size(); enemyIndex++) {
            EffectTarget enemy = combatState.getEnemyMob(enemyIndex);
            if(enemy.getHitpoints() > 0) {
                win = false;
            }
        }
        
        if(loss) {
            activeRound.setState(State.LOSS);
            if(combatState.getCombat().getCombatType() == CombatType.PvE) {
                onLoss(combatState);
            } else {
                pvpCombatDao.onLoss(combatState);
            }
        } else if(win) {
            activeRound.setState(State.VICTORY);
            if(combatState.getCombat().getCombatType() == CombatType.PvE) {
                onVictory(combatState);
            } else {
                pvpCombatDao.onVictory(combatState);
            }
        } else {
            activeRound.setState(State.COMPLETE);
            startRound(combatState.getCombat());
        }
        
        return combatState;
    }

    private void carryOverActions(CombatState combatState) {
        int roundNumber = combatState.getActiveRound().getRoundNumber();
        if(roundNumber == 0) {
            // no op
        } else {
            CombatRound previousRound = getCombatRound(combatState.getCombat().getId(), roundNumber - 1);
            for(CombatAction combatAction : previousRound.getActions()) {
                combatActionDao.continueActionIfNeeded(combatAction, combatState);
            }
        }
        
    }

    @SuppressWarnings("unchecked")
    public CombatRound getCombatRound(Integer combatId, int roundNumber) {
        List<CombatRound> list = template.findByNamedQuery("combatRound.getByIndex", combatId, roundNumber);
        return list.get(0);
    }

    private Combat startCombat(Adventure adventure, Room room) {
        List<CombatParticipant> participants = new ArrayList<CombatParticipant>();

        if(room.getCombat() != null) {
            throw new IllegalStateException("Room " + room.getId() + " is already associated to combat " + room.getCombat().getId() + ".");
        }
        if(!room.getType().equals(RoomType.COMBAT)) {
            throw new IllegalStateException("Room " + room.getId() + " is of type " + room.getType() + ", which is not eligible for combat.");
        }
        if(room.getInhabitants() == null) {
           room.setInhabitants(new ArrayList<Mob>()); 
        }
        if(room.getInhabitants().size() == 0) {
            Encounter e = adventureFactory.getEncounter(room);
            List<String> mobs = e.create();
            log.debug("Spawning " + mobs.toString());
            for(String mobArchetype : mobs) {
                room.getInhabitants().add(mobDao.createMob(mobArchetype));
            }
        }

        for(PlayerCharacter pc : adventure.getParticipants()) {
            participants.add(new CombatParticipant(Role.ALLY, pc.getName(), Type.PLAYER));
        }
        for(Mob mob : room.getInhabitants()) {
            participants.add(new CombatParticipant(Role.ENEMY, mob.getArchetype(), Type.MOB));
        }

        Combat combat = new Combat(null, room, CombatType.PvE, participants);
        
        template.save(combat);
        startRound(combat);
        room.setCombat(combat);
        
        log.info("Created combat " + combat.getId() + " for adventure " + adventure.getId() + ", room " + adventure.getActiveRoomIndex() + ".");
        
        return combat;
    }
    
    private void startRound(Combat combat) {
        CombatRound round = new CombatRound();
        round.setCombat(combat);
        round.setRoundNumber(combat.getRounds().size());
        template.save(round);
        combat.getRounds().add(round);
        combat.setActiveRoundNumber(round.getRoundNumber());
    }

    private void onVictory(CombatState combatState) {
        log.debug("Whoo! Victory for combat " + combatState.getCombat().getId() + "!");
        
        int totalXp = 0;
        int totalCurrency = 0;
        int totalTokens = 0;
        List<Item> items = new ArrayList<Item>();
        for(EffectTarget enemy : combatState.getEnemies()) {
            if(enemy instanceof Mob) {
                Mob mob = (Mob)enemy;
                MobArchetype archetype = mobDao.getArchetype(mob.getArchetype());
                int level = archetype.getLevel();
                int xp = level * ScaleOptions.MOB_XP_PER_LEVEL;
                if(archetype.isBoss()) {
                    xp = xp * 10;
                }
                totalXp += xp;
                String treasureTable = archetype.getTreasureTable();
                Treasure treasure = treasureDao.create(treasureTable);
                totalCurrency += treasure.getCurrency();
                totalTokens += treasure.getTokens();
                for (Item item : treasure.getItems()) {
                	if(item.getType() == ItemType.QUEST) {
                		String playerCharacterName = combatState.getAllies().get(0).getName();
                		if(questProgressDao.needsQuestItem(playerCharacterName, item)) {
                		    if(!items.contains(item)) {
                		        items.add(item);
                		    }
                		}
                	} else {
                        items.add(item);
                	}
    			}
                log.debug("Death of " + archetype.getName() + " resulted in " + xp + " xp, " + treasure.getCurrency() + " gold, and " + items.size() + " items.");
            } else {
                throw new IllegalStateException("PvP battle.");
            }
        }

        int totalLevels = 0;
        for(PlayerCharacter pc : combatState.getAllies()) {
            totalLevels += pc.getLevel();
        }
        
        for(PlayerCharacter pc : combatState.getAllies()) {
            double portion = pc.getLevel() / totalLevels;
            
            playerCharacterDao.addXp(pc, (int)(totalXp * portion));
            if(totalCurrency > 0) {
                inventoryDao.addCurrency(pc.getName(), (int)(totalCurrency * portion));
            }
            if(totalTokens > 0) {
                inventoryDao.addTokens(pc.getName(), (int)(totalTokens * portion));
            }
            for(EffectTarget enemy : combatState.getEnemies()) {
                Mob mob = (Mob)enemy;
                questProgressDao.incrementQuestProgressIfNeeded(pc.getName(), mob.getArchetype());
                statisticsDao.increment(pc.getName(), "kill-" + mob.getArchetype());
                MobArchetype archetype = mobDao.getArchetype(mob.getArchetype());
                if(archetype.isBoss()) {
                    statisticsDao.increment(pc.getName(), "kill-boss");
                }
            }
        }

        Room room = combatState.getCombat().getRoom();
        if(items.size() == 0) {
            room.setState(RoomState.COMPLETED);
        } else {
            room.getContents().addAll(items);
        }
    }

    private void onLoss(CombatState combatState) {
        for(PlayerCharacter pc : combatState.getAllies()) {
            pc.setHitpoints(pc.getMaxHitpoints());
            pc.setMana(pc.getMaxMana());
            pc.setActiveAdventureId(null);

            int oldXp = pc.getXp();
            
            int requiredXp = playerCharacterDao.requiredXpByLevel[pc.getLevel()];
            int xpLoss = (requiredXp / 20);
            
            pc.setXp(oldXp - xpLoss );
            if(pc.getXp() < 0) {
                pc.setXp(0);
            }
        }

        Adventure adventure = combatState.getCombat().getRoom().getAdventure();
        adventure.setStatus(Status.ABANDONED);

        AdventureMap map = adventureFactory.getMap(adventure);
        adventure.setActiveRoomIndex(map.getIntroductionRoomIndex());
    }

    @Override
    protected Class<?> getManagedClass() {
        return Combat.class;
    }
}
