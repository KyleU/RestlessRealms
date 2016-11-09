package restless.realms.server.inventory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.client.layout.SizeConstants;
import restless.realms.server.database.AbstractDao;
import restless.realms.server.exception.InsufficientFundsException;
import restless.realms.server.item.Item;
import restless.realms.server.item.ItemDao;
import restless.realms.server.item.ItemType;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.profession.ProfessionDao;
import restless.realms.server.quest.progress.QuestProgressDao;
import restless.realms.server.util.ScaleOptions;

@Repository
public class InventoryDao extends AbstractDao<Inventory> {
    private static final Log log = LogFactory.getLog(InventoryDao.class);
    
    @Autowired
    private ItemDao itemDao;

    @Autowired
    private PlayerCharacterDao playerDao;

    @Autowired
    private ProfessionDao professionDao;
    
    @Autowired
    private QuestProgressDao questProgressDao;
    
    @Autowired
    private StatisticsDao statisticsDao;

    @Transactional
    public Inventory get(String playerName) {
        Inventory result = template.get(Inventory.class, playerName);
        if(result == null) {
            PlayerCharacter p = playerDao.get(playerName);
            if(p == null) {
                IllegalStateException e = new IllegalStateException("Player does not exist!");
                log.error("No player found.", e);
                throw e;
            }
            int startingGold = (600 * p.getLevel() * ScaleOptions.ECONOMY);
            log.debug("Creating inventory for player \"" + p.getName() + "\" with " + startingGold + " initial gold.");
            result = new Inventory();
            result.setPlayerName(playerName);
            result.setCurrency(startingGold);
            List<Item> startingEquipment = new ArrayList<Item>();
            List<Integer> initialItemIds = professionDao.getProfession(p.getProfession()).getInitialItemIds();
            for(Integer itemId : initialItemIds) {
                Item item = itemDao.get(itemId);
                if(item == null) {
                    throw new IllegalStateException("Initial items for profession \"" + p.getProfession() + "\" reference invalid item \"" + itemId + "\"");
                }
                startingEquipment.add(item);
            }
            result.setItems(startingEquipment);
            template.save(result);
        }
        
        result.getItems().size();
        return result;
	}

    // TODO Push this into HQL. 
    public int getCount(String playerCharacterName, Integer itemId) {
        int ret = 0;
        Inventory inventory = get(playerCharacterName);
        for(Item item : inventory.getItems()) {
            if(item.getId().equals(itemId)) {
                ret++;
            }
        }
        return ret;
    }

    public void validateAction(PlayerCharacter character, Integer itemId) {
        int count = getCount(character.getName(), itemId);
        if(count == 0) {
            log.error("Character \"" + character.getName() + "\" does not have item " + itemId + ".");
            throw new IllegalStateException("You do not own that item.");
        }
        
        Item item = itemDao.get(itemId);
        if(item.getMinLevel() > character.getLevel()) {
            log.error("Character \"" + character.getName() + "\" is level " + character.getLevel() + ", but must be level " + item.getMinLevel() + " to use item " + itemId + ".");
            throw new IllegalStateException("You must be level " + item.getMinLevel() + " to use " + item.getName() + ".");
        }
        if(item.getRequiredProfession() != null && !item.getRequiredProfession().equals(character.getProfession())) {
            log.error("Character \"" + character.getName() + "\" is a " + character.getProfession() + ", but must be a " + item.getRequiredProfession() + " to use item " + itemId + ".");
            throw new IllegalStateException("You must be a " + item.getRequiredProfession() + " to use a " + item.getName() + ".");
        }
    }

    public int getCurrency(String playerName) {
        List<?> result = template.findByNamedQuery("inventory.getCurrency", playerName);
        Integer ret = null;
        if(result.size() == 0) {
            Inventory inventory = get(playerName);
            ret = inventory.getCurrency();
        } else {
            ret = (Integer)result.get(0);   
        }
        return ret;
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void addCurrency(String playerName, int currency) {
        if(currency <= 0) {
            throw new IllegalArgumentException("Use removeCurrency() instead.");
        }
        log.debug("Increasing currency of \"" + playerName + "\" by " + currency + " gold.");
        Inventory inventory = get(playerName);
        inventory.setCurrency(inventory.getCurrency() + currency);
        statisticsDao.increment(playerName, "receive-gold", currency);
    }
    
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void removeCurrency(String playerName, int currency) {
        if(currency <= 0) {
            throw new IllegalArgumentException("Can't remove negative numbers.");
        }
        log.debug("Decreasing currency of \"" + playerName + "\" by " + currency + " gold.");
        Inventory inventory = get(playerName);
        inventory.setCurrency(inventory.getCurrency() - currency);
        statisticsDao.increment(playerName, "spend-gold", currency);
    }
    
    public int getTokens(String playerName) {
        List<?> result = template.findByNamedQuery("inventory.getTokens", playerName);
        Integer ret = null;
        if(result.size() == 0) {
            Inventory inventory = get(playerName);
            ret = inventory.getTokens();
        } else {
            ret = (Integer)result.get(0);   
        }
        return ret;
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void addTokens(String playerName, int tokens) {
        if(tokens <= 0) {
            throw new IllegalArgumentException("Use removeTokens() instead.");
        }
        log.debug("Increasing tokens of \"" + playerName + "\" by " + tokens + ".");
        Inventory inventory = get(playerName);
        inventory.setTokens(inventory.getTokens() + tokens);
        statisticsDao.increment(playerName, "receive-token", tokens);
    }
    
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void removeTokens(String playerName, int tokens) {
        log.debug("Decreasing tokens of \"" + playerName + "\" by " + tokens + ".");
        Inventory inventory = get(playerName);
        inventory.setTokens(inventory.getTokens() - tokens);
        statisticsDao.increment(playerName, "spend-token", tokens);
    }
    
    @Transactional(isolation=Isolation.REPEATABLE_READ)
	public Inventory buyItem(String playerName, String merchant, Integer itemId) {
		Inventory inventory = get(playerName);

		int currentCount = getCount(playerName, itemId);
		if(currentCount >= SizeConstants.MAX_ITEM_STACK) {
		    throw new IllegalArgumentException("You may only hold " + SizeConstants.MAX_ITEM_STACK + " items of any one type.");
		}
		
		Item item = itemDao.get(itemId);
        if(item == null) {
            throw new IllegalArgumentException("Invalid Item");
        }
		
		int balance = inventory.getCurrency();
		int debitAmount = item.getMsrp();
		
		if(debitAmount > balance) {
            log.debug(inventory.getPlayerName() + " attempted to purchase " + item.getName() + " (" + item.getId() + ") for " + item.getMsrp() + "g, but only has " + inventory.getCurrency() + ".");
		    throw new InsufficientFundsException();
		}

		if(log.isDebugEnabled()) {
            log.debug(inventory.getPlayerName() + " is purchasing " + item.getName() + " (" + item.getId() + ") for " + item.getMsrp() + "g.");
        }
        inventory.setCurrency(balance - debitAmount);
        inventory.getItems().add(item);
        questProgressDao.incrementQuestProgressIfNeeded(playerName, item);
        statisticsDao.increment(playerName, "item-buy");
        statisticsDao.increment(playerName, "gold-spend", debitAmount);

        return inventory;
	}

	@Transactional(isolation=Isolation.REPEATABLE_READ)
    public Inventory sellItem(String playerName, int inventoryIndex, Integer itemId) {
        Inventory inventory = get(playerName);
        if(inventory.getItems().size() < inventoryIndex + 1) {
            throw new IllegalArgumentException("Invalid inventory index.");
        }
	    
	    Item item = inventory.getItems().get(inventoryIndex);
        
        if(!itemId.equals(item.getId())) {
            throw new IllegalArgumentException("Item at inventory index " + inventoryIndex + " has id " + item.getId() + ", but I expected " + itemId + ".");
        }
        if(item.getType() == ItemType.QUEST) {
            throw new IllegalArgumentException("You cannot sell quest items.");
        }
	    
        int balance = inventory.getCurrency();
        int creditAmount = (int)Math.round(item.getMsrp() * 0.10);
        
        if(log.isDebugEnabled()) {
            log.debug("Selling " + inventory.getPlayerName() + "'s " + item.getName() + " (" + item.getId() + ") for " + creditAmount + "g.");
        }
        inventory.setCurrency(balance + creditAmount);
        inventory.getItems().remove(inventoryIndex);
        questProgressDao.decrementQuestProgressIfNeeded(playerName, item);
        
        statisticsDao.increment(playerName, "item-sell");

        return inventory;
	}
	
    @Transactional
	public Inventory sellAllTrash(String characterName) {
        Inventory inventory = get(characterName);

        int numItemsSold = 0;
        
        List<Item> items = inventory.getItems();
        for(int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if(ItemType.TRASH.equals(item.getType())) {
                int balance = inventory.getCurrency();
                int creditAmount = (int)Math.round(item.getMsrp() * 0.50);
                
                if(log.isDebugEnabled()) {
                    log.debug("Selling " + inventory.getPlayerName() + "'s " + item.getName() + " (" + item.getId() + ") for " + item.getMsrp() + "g.");
                }
                inventory.setCurrency(balance + creditAmount);
                items.remove(i--);
                numItemsSold++;
            }
        }
        
        statisticsDao.increment(inventory.getPlayerName(), "item-sell", numItemsSold);
        return inventory;
    }

    @Transactional
    public void consumeItem(String playerName, Integer itemId) {
        removeItem(playerName, itemId);
        statisticsDao.increment(playerName, "item-consume");
    }

    @Transactional
    public Item removeItem(String playerName, Integer itemId) {
        Inventory inventory = get(playerName);
        int matchingIndex = -1;
        List<Item> items = inventory.getItems();
        for(int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if(item.getId().equals(itemId)) {
                matchingIndex = i; 
                break;
            }
        }
        if(matchingIndex == -1) {
            throw new IllegalArgumentException("You have no item with an id of \"" + itemId + "\"");
        } else {
            return items.remove(matchingIndex);
        }
    }

	@Transactional
	public void give(String characterName, Integer itemId) {
        Inventory inventory = get(characterName);
        Item item = itemDao.get(itemId);
        log.info("Giving item \"" + item.getName() + " \" to player \"" + characterName + "\".");
        inventory.getItems().add(item);
    }

    @Override
    protected Class<?> getManagedClass() {
        return Inventory.class;
    }
}