package restless.realms.server.room;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.client.layout.SizeConstants;
import restless.realms.server.adventure.Adventure;
import restless.realms.server.adventure.AdventureDao;
import restless.realms.server.adventure.AdventureFactory;
import restless.realms.server.database.AbstractDao;
import restless.realms.server.inventory.Inventory;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.item.Item;
import restless.realms.server.item.ItemType;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.quest.progress.QuestProgressDao;
import restless.realms.server.treasure.Treasure;
import restless.realms.server.treasure.TreasureDao;

@Repository
public class RoomDao extends AbstractDao<Room> {
    @Autowired
    private AdventureDao adventureDao;

    @Autowired
    private AdventureFactory adventureFactory;
    
    @Autowired
    private TreasureDao treasureDao;
    
    @Autowired
    private QuestProgressDao questProgressDao;
    
    @Autowired
    private InventoryDao inventoryDao;
    
    @Autowired
    StatisticsDao statisticsDao;

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void startRoom(String playerCharacter, Room room) {
        if(!RoomState.NEW.equals(room.getState())) {
            throw new IllegalStateException("Room state is " + room.getState() + ", not NEW.");
        }
        room.setState(RoomState.ACTIVE);
        if(room.getType().equals(RoomType.LOOT)) {
            char code = adventureFactory.getMap(room.getAdventure()).getLocations().get(room.getRoomIndex()).getCode();
            String tableCode = room.getAdventure().getType() + "-" + code;
            Treasure treasure = treasureDao.create(tableCode);
            for(Item item : treasure.getItems()) {
                if(item.getType() == ItemType.QUEST) {
                    if(questProgressDao.needsQuestItem(playerCharacter, item)) {
                        room.getContents().add(item);
                    }
                } else {
                    room.getContents().add(item);
                }
            }
            if(treasure.getCurrency() > 0) {
                inventoryDao.addCurrency(playerCharacter, treasure.getCurrency());
            }
            if(treasure.getTokens() > 0) {
                inventoryDao.addTokens(playerCharacter, treasure.getTokens());
            }
        }
        statisticsDao.increment(playerCharacter, "room-" + room.getType());
    }
    
    public Room getRoom(Integer adventureId, int roomIndex) {
        Room ret = (Room)template.findByNamedQuery("room.getByIndex", adventureId, roomIndex).get(0);
        return ret ;
    }
    
    @Transactional
    public List<Item> getContents(Integer adventureId) {
        Adventure a = adventureDao.getAdventure(adventureId);
        Room room = getRoom(a.getId(), a.getActiveRoomIndex());
        List<Item> ret = new ArrayList<Item>();
        for(Item item : room.getContents()) {
            ret.add(item);
        }
        return ret;
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Inventory loot(String playerName, int adventureId, int itemId, int itemIndex) {
        Inventory inv = inventoryDao.get(playerName);

        Adventure a = adventureDao.getAdventure(adventureId);
        Room room = getRoom(a.getId(), a.getActiveRoomIndex());
        
        Item item = room.getContents().get(itemIndex);
        if(item == null) {
            throw new IllegalStateException("Invalid item index " + itemIndex + " for room " + room.getId() + ".");
        }
        if(!item.getId().equals(itemId)) {
            throw new IllegalStateException("Item at index " + itemIndex + " for room " + room.getId() + " does not have id \"" + itemId + "\".");
        }

        if(!item.getType().equals(ItemType.QUEST)) {
            int currentCount = inventoryDao.getCount(playerName, itemId);
            if(currentCount >= SizeConstants.MAX_ITEM_STACK) {
                throw new IllegalArgumentException("You may only hold " + SizeConstants.MAX_ITEM_STACK + " items of any one type.");
            }
        }
        
        room.getContents().remove(itemIndex);
        inv.getItems().add(item);
        questProgressDao.incrementQuestProgressIfNeeded(playerName, item);
        
        statisticsDao.increment(playerName, "item-loot-" + item.getType().toString());
        statisticsDao.increment(playerName, "item-loot-" + item.getRarity());
        
        if(room.getContents().size() == 0) {
            room.setState(RoomState.COMPLETED);
        }
        return inv;
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Inventory lootall(String characterName, int adventureId) {
        Inventory inv = inventoryDao.get(characterName);

        Adventure a = adventureDao.getAdventure(adventureId);
        Room room = getRoom(a.getId(), a.getActiveRoomIndex());
        
        if(room.getContents() != null) {
            while(room.getContents().size() > 0) {
                Item item = room.getContents().remove(0);
                if(!item.getType().equals(ItemType.QUEST)) {
                    int currentCount = inventoryDao.getCount(characterName, item.getId());
                    if(currentCount >= SizeConstants.MAX_ITEM_STACK) {
                        throw new IllegalArgumentException("You may only hold " + SizeConstants.MAX_ITEM_STACK + " items of any one type.");
                    }
                }
                inv.getItems().add(item);
                questProgressDao.incrementQuestProgressIfNeeded(characterName, item);
                statisticsDao.increment(characterName, "item-loot-" + item.getType().toString());
                statisticsDao.increment(characterName, "item-loot-" + item.getRarity());
            }
        }
        
        room.setState(RoomState.COMPLETED);

        return inv;
    }
    
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void solvePuzzle(Integer adventureId, String solution) {
        Adventure a = adventureDao.getAdventure(adventureId);
        Room room = getRoom(a.getId(), a.getActiveRoomIndex());
        if(!room.getType().equals(RoomType.PUZZLE)) {
            throw new IllegalStateException("This room isn't a puzzle.");
        }
        if(!room.getState().equals(RoomState.ACTIVE)) {
            throw new IllegalStateException("This room is " + room.getState().toString().toLowerCase() + ".");
        }
        room.setState(RoomState.COMPLETED);
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void shrine(Integer adventureId) {
        Adventure a = adventureDao.getAdventure(adventureId);
        Room room = getRoom(a.getId(), a.getActiveRoomIndex());
        if(!room.getType().equals(RoomType.SHRINE)) {
            throw new IllegalStateException("This room isn't a shrine.");
        }
        if(!room.getState().equals(RoomState.ACTIVE)) {
            throw new IllegalStateException("This room is " + room.getState().toString().toLowerCase() + ".");
        }
        room.setState(RoomState.COMPLETED);

        for(PlayerCharacter pc : a.getParticipants()) {
            pc.setHitpoints(pc.getMaxHitpoints());
            pc.setMana(pc.getMaxMana());
        }
    }

    @Override
    protected Class<?> getManagedClass() {
        return Room.class;
    }
}
