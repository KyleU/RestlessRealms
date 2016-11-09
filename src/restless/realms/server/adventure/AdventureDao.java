package restless.realms.server.adventure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.account.AccountDao;
import restless.realms.server.action.Action;
import restless.realms.server.adventure.Adventure.Status;
import restless.realms.server.combat.CombatDao;
import restless.realms.server.combat.CombatRound;
import restless.realms.server.combat.action.CombatAction;
import restless.realms.server.database.AbstractDao;
import restless.realms.server.effect.Effect;
import restless.realms.server.effect.EffectResult;
import restless.realms.server.effect.TargetingType;
import restless.realms.server.equipment.EquipmentBonuses;
import restless.realms.server.equipment.EquipmentDao;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.item.Item;
import restless.realms.server.item.ItemType;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.room.Room;
import restless.realms.server.room.RoomDao;
import restless.realms.server.room.RoomState;
import restless.realms.server.room.RoomType;
import restless.realms.server.session.Session;
import restless.realms.server.skill.Skill;
import restless.realms.server.util.RandomUtils;

@Repository
public class AdventureDao extends AbstractDao<Adventure> {
    private static final Log log = LogFactory.getLog(AdventureDao.class);
    
    @Autowired
    private AccountDao accountDao;
    
    @Autowired
    private RoomDao roomDao;
    
    @Autowired
    private CombatDao combatDao;
    
    @Autowired
    private PlayerCharacterDao playerCharacterDao;

    @Autowired
    private EquipmentDao equipmentDao;
    
    @Autowired
    private AdventureFactory adventureFactory;
    
    @Autowired
    private InventoryDao inventoryDao;
    
    @Autowired
    private StatisticsDao statisticsDao;

	public AdventureDao() {
	}

	public Adventure getAdventure(Integer id) {
       Adventure ret = get(id);
       return ret; 
    }
	
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Adventure startAdventure(Session s, String type) {
        PlayerCharacter player = playerCharacterDao.get(s);
        if(player.getActiveAdventureId() != null) {
            throw new IllegalStateException("You're already on an adventure.");
        }

        AdventureArchetype aa = adventureFactory.getAdventureArchetypes().get(type);
        if(aa == null) {
            throw new IllegalArgumentException("There is no adventure of type \"" + type + "\".");
        }
        if(player.getLevel() < aa.getMinLevel()) {
            throw new IllegalStateException("You must be level " + aa.getMinLevel() + " to start " + aa.getName() + ".");
        }
        
        List<PlayerCharacter> participants = new ArrayList<PlayerCharacter>();
        participants.add(player);

        if("Administrator".equals(player.getName())) {
            participants.add(playerCharacterDao.get("Kyle"));
            participants.add(playerCharacterDao.get("Dan"));
        }
        
        Adventure adventure = adventureFactory.create(type, RandomUtils.nextInt());
        adventure.setParticipants(participants);
        template.save(adventure);

        player.setActiveAdventureId(adventure.getId());
        log.info("Created adventure " + adventure.getId() + " for \"" + player.getName() + "\".");

        if(!"tutorial".equals(type)) {
            int adventurePoints = accountDao.spendAdventurePoints(s.getAccountId(), 1);
            if(adventurePoints == 0) {
                statisticsDao.increment(player.getName(), "out-of-aps");
            }
            statisticsDao.increment(player.getName(), "spend-ap");
        }
        
        
        return adventure;
    }
    
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Adventure resumeAdventure(String playerCharacterName) {
        PlayerCharacter player = playerCharacterDao.get(playerCharacterName);

        if(player.getActiveAdventureId() == null) {
            throw new IllegalStateException("You're not on an adventure. How did you get here? Email me, I can't reproduce this bug!");
        }
        
        Adventure adventure = template.get(Adventure.class, player.getActiveAdventureId());
        Status status = adventure.getStatus();
        if(status.equals(Status.ABANDONED) || status.equals(Status.COMPLETE)) {
            throw new IllegalStateException("Unable to resume " + status + " adventure"); 
        }
        boolean participant = false;
        for(PlayerCharacter p : adventure.getParticipants()) {
            if(p.getName().equals(playerCharacterName)) {
                participant = true;
            }
        }
        if(!participant) {
            throw new IllegalStateException("You are not a part of this adventure, " + playerCharacterName);
        }

        //for map iteration, lazy load in transaction
        adventure.getRooms().size();
        
        log.info("Resumed adventure " + adventure.getId() + " for \"" + playerCharacterName + "\".");

        return adventure;
    }
    
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public PlayerCharacter abandonAdventure(String playerCharacterName) {
        PlayerCharacter playerCharacter = playerCharacterDao.get(playerCharacterName);
        Adventure adventure = get(playerCharacter.getActiveAdventureId());
        switch(adventure.getStatus()) {
        case ABANDONED:
            throw new IllegalStateException("Adventure already abandoned.");
        case COMPLETE:
            throw new IllegalStateException("Adventure already completed.");
        default:
            log.debug("Abandoning adventure " + adventure.getId() + ".");
            playerCharacter.setActiveAdventureId(null);
            playerCharacter.setHitpoints(playerCharacter.getMaxHitpoints());
            playerCharacter.setMana(playerCharacter.getMaxMana());
            adventure.setStatus(Adventure.Status.ABANDONED);
            cleanup(adventure);
        }
        return playerCharacter;
    }
    
    private void cleanup(Adventure adventure) {
        log.info("Cleaning up adventure " + adventure.getId() + " (" + adventure.getType() + ":" + adventure.getStatus() + ", " + adventure.getRooms().size() + " rooms).");
        for(Room room : adventure.getRooms()) {
            if(room.getCombat() != null) {
                template.delete(room.getCombat());
            }
            template.delete(room);
        }
        adventure.getRooms().clear();
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void completeAdventure(String playerCharacterName) {
        PlayerCharacter playerCharacter = playerCharacterDao.get(playerCharacterName);
        Adventure adventure = get(playerCharacter.getActiveAdventureId());
        switch(adventure.getStatus()) {
        case ABANDONED:
            throw new IllegalStateException("Adventure already abandoned.");
        case COMPLETE:
            throw new IllegalStateException("Adventure already completed.");
        default:
            Room room = roomDao.getRoom(adventure.getId(), adventure.getActiveRoomIndex());
            if(room.getType().equals(RoomType.EXIT)) {
                adventure.setStatus(Adventure.Status.COMPLETE);
                log.debug("Completing adventure " + adventure.getId() + ".");
                playerCharacter.setActiveAdventureId(null);
                playerCharacter.setHitpoints(playerCharacter.getMaxHitpoints());
                playerCharacter.setMana(playerCharacter.getMaxMana());

                AdventureArchetype adventureArchetype = adventureFactory.getAdventureArchetypes().get(adventure.getType());
                int xp = adventureArchetype.getMaxLevel() * 250;
                int gold = adventureArchetype.getMaxLevel() * 100;
                playerCharacterDao.addXp(playerCharacter, xp);
                inventoryDao.addCurrency(playerCharacter.getName(), gold);
                if(!"tutorial".equals(adventure.getType())) {
                    statisticsDao.increment(playerCharacter.getName(), "adventure");
                }
                statisticsDao.increment(playerCharacter.getName(), "adventure-" + adventure.getType());
                cleanup(adventure);
            } else {
                throw new IllegalStateException("Active room is of type \"" + room.getType() + "\", which cannot complete this adventure.");
            }
        }
    }
    
    //returns [previousRoom, newRoom]
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Room[] move(Integer adventureId, int roomIndex) {
        Adventure adventure = get(adventureId);

        if(!adventure.getStatus().equals(Adventure.Status.ACTIVE)) {
            String message = "Move attempted when adventure " + adventure.getId() + " has " + adventure.getStatus() + " status.";
            log.warn(message);
            throw new IllegalStateException(message);
        }

        Room activeRoom = roomDao.getRoom(adventure.getId(), adventure.getActiveRoomIndex());
        Room proposedRoom = roomDao.getRoom(adventure.getId(), roomIndex);
        
        if(!activeRoom.getState().equals(RoomState.COMPLETED)) {
            switch(activeRoom.getType()) {
            case INTRO:
            case EMPTY:
            case NPC:
                activeRoom.setState(RoomState.COMPLETED);
                break;
            case LOOT:
                if(activeRoom.getContents().size() == 0) {
                    activeRoom.setState(RoomState.COMPLETED);
                }
                break;
            case PUZZLE:
                if(proposedRoom.getState().equals(RoomState.NEW)) {
                    throw new IllegalStateException("You may not advance until you have completed this puzzle.");
                }
                break;
            case COMBAT:
                int activeRoundNumber = activeRoom.getCombat().getActiveRoundNumber();
                Integer combatId = activeRoom.getCombat().getId();
                CombatRound currentRound = combatDao.getCombatRound(combatId, activeRoundNumber);
                if(!currentRound.getState().equals(CombatRound.State.VICTORY)) {
                    throw new IllegalStateException("You may not leave this room until you have completed your combat.");
                }
                if(activeRoom.getContents().size() == 0) {
                    activeRoom.setState(RoomState.COMPLETED);
                }
                break;
            case SHRINE:
            case EXIT:
                //no op
                break;
            default:
                throw new IllegalStateException("Unhandled room type " + activeRoom.getType() + ".");
            }
        }
        
        int distance = adventureFactory.getMap(adventure).getDistance(adventure.getActiveRoomIndex(), roomIndex);
        if(distance == 0) {
            if(activeRoom.getId() == proposedRoom.getId()) {
                throw new IllegalStateException("Cannot move to same room (" + activeRoom.getId() + ").");
            } else {
                throw new IllegalStateException("Rooms " + activeRoom.getId() + " and " + proposedRoom.getId() + " occupy the same position.");
            }
        } else if(distance > 1) {
            throw new IllegalArgumentException("Rooms " + activeRoom.getId() + " and " + proposedRoom.getId() + " are too far apart (" + distance + ").");
        }
        
        if(proposedRoom.getState().equals(RoomState.NEW)) {
            roomDao.startRoom(adventure.getParticipants().get(0).getName(), proposedRoom);
        }

        adventure.setActiveRoomIndex(roomIndex);
        //[old, new]
        return new Room[] {activeRoom, proposedRoom};
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public List<EffectResult> onAction(Session session, CombatAction combatAction) {
        playerCharacterDao.validateAction(combatAction, session.getCharacterName());

        PlayerCharacter character = playerCharacterDao.get(session);
        
        Action action = null;
        if('s' == combatAction.getActionType()) {
            Skill s = template.get(Skill.class, combatAction.getActionId());;
            //scaling mana cost
            int manaCost = s.getManaCost() + (s.getManaCostPerLevel() * character.getLevel());
            character.setMana(character.getMana() - manaCost);
            action = s;
        } else if('i' == combatAction.getActionType()) {
            Item i = template.get(Item.class, combatAction.getActionId());
            if(i.getType() == ItemType.CONSUMABLE) {
                inventoryDao.consumeItem(session.getCharacterName(), i.getId());
            } else {
                throw new IllegalArgumentException("Only consumable items may be used outside of combat. This item is a " + i.getType().toString() + ".");
            }
            action = i;
        } else {
            throw new IllegalStateException("Illegal action type \"" + combatAction.getActionType() + "\".");
        }

        List<EffectResult> ret = new ArrayList<EffectResult>();
        
        for(Effect effect : action.getEffects()) {
            if(effect.getTargeting() != TargetingType.SELF) {
                throw new IllegalArgumentException("Action " + action.getName() + " does not target only yourself."); 
            }
            
            int percentChance = effect.getPercentChance();
            if(effect.getPercentChancePerLevel() > 0) {
                percentChance = percentChance + (effect.getPercentChancePerLevel() * character.getLevel());
            }

            if(RandomUtils.percentageCheck(percentChance)) {
                int quantity = RandomUtils.getInt(effect.getMinQuantity(), effect.getMaxQuantity());
                //scaling quantity
                if(effect.getQuantityPerLevel() > 0) {
                    quantity = quantity + (effect.getQuantityPerLevel() * character.getLevel());
                }
                EquipmentBonuses bonuses = equipmentDao.getBonuses(character.getName());
                quantity += bonuses.getBonus(effect.getEffectType());

                Collection<EffectResult> effectResults = Effect.apply(effect.getEffectType(), 0, 0, character, quantity, 0);
                ret.addAll(effectResults);
            }
        }

        if(log.isDebugEnabled()) {
            StringBuilder message = new StringBuilder();
            message.append("Applying out-of-combat ");
            message.append(combatAction.getActionType() == 's' ? "skill \"" : "item \"");
            message.append(action.getName());
            message.append("\" to character \"");
            message.append(character.getName());
            message.append("\", with the following results {");
            for(Iterator<EffectResult> iterator = ret.iterator(); iterator.hasNext();) {
                EffectResult effectResult = iterator.next();
                message.append(effectResult.getType());
                message.append(":");
                message.append(effectResult.getQuantity());
                if(iterator.hasNext()) {
                    message.append(", ");
                }
            }
            message.append("}.");
            log.debug(message.toString());
        }
        return ret;
    }

    
    @Override
    protected Class<?> getManagedClass() {
        return Adventure.class;
    }
}
