package restless.realms.server.stash;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.account.Account;
import restless.realms.server.account.AccountDao;
import restless.realms.server.database.AbstractDao;
import restless.realms.server.inventory.Inventory;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.item.Item;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;

@Repository
public class StashDao extends AbstractDao<Stash> {
    private static final Log log = LogFactory.getLog(StashDao.class);

    @Autowired
    private AccountDao accountDao;
    
    @Autowired
    private PlayerCharacterDao playerDao;

    @Autowired
    private InventoryDao inventoryDao; 
    
    @Autowired
    private StatisticsDao statisticsDao;

    @Transactional
    public Stash get(Integer accountId) {
        Stash result = super.get(accountId);
        if(result == null) {
            Account a = accountDao.get(accountId);
            if(a == null) {
                IllegalStateException e = new IllegalStateException("Account does not exist!");
                log.error("No account found.", e);
                throw e;
            }
            log.debug("Creating stash for account \"" + a.getId() + "\".");
            result = new Stash();
            result.setAccountId(accountId);
            result.setCurrency(0);
            List<Item> startingStash = new ArrayList<Item>();
            for(int i = 0; i < Stash.NUM_ITEMS; i++) {
                startingStash.add(null);
            }
            result.setItems(startingStash);
            template.save(result);
        }
        return result;
	}

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void depositCurrency(String playerName, int currency) {
        if(currency <= 0) {
            throw new IllegalArgumentException("Use removeCurrency() instead.");
        }
        PlayerCharacter playerCharacter = playerDao.get(playerName);
        Stash stash = get(playerCharacter.getAccountId());

        log.debug("Depositing " + currency + " currency into stash for account \"" + playerCharacter.getAccountId() + "\".");
        inventoryDao.removeCurrency(playerName, currency);
        stash.setCurrency(stash.getCurrency() + currency);
        statisticsDao.increment(playerName, "deposit-gold", currency);
    }
    
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void withdrawCurrency(String playerName, int currency) {
        if(currency <= 0) {
            throw new IllegalArgumentException("Can't withdraw negative numbers.");
        }
        PlayerCharacter playerCharacter = playerDao.get(playerName);
        Stash stash = get(playerCharacter.getAccountId());

        log.debug("Withdrawing " + currency + " currency from stash for account \"" + playerCharacter.getAccountId() + "\".");
        stash.setCurrency(stash.getCurrency() - currency);
        inventoryDao.addCurrency(playerName, currency);
        statisticsDao.increment(playerName, "deposit-gold", currency);
    }
    
    @Transactional(isolation=Isolation.REPEATABLE_READ)
	public void depositItem(String playerName, Integer itemId, int index) {
        PlayerCharacter playerCharacter = playerDao.get(playerName);
		Stash stash = get(playerCharacter.getAccountId());

		if(stash.getItems().size() > index && stash.getItems().get(index) != null) {
		    throw new IllegalArgumentException("You already have an item in your stash at index " + index + ".");
		}
		
        Item removedItem = inventoryDao.removeItem(playerName, itemId);
        
        stash.getItems().set(index, removedItem);
        statisticsDao.increment(playerName, "deposit-item");
        if(log.isDebugEnabled()) {
            log.debug(playerName + " is depositing " + removedItem.getName() + " (" + removedItem.getId() + ") in their stash.");
        }
	}

	@Transactional(isolation=Isolation.REPEATABLE_READ)
    public void withdrawItem(String playerName, Integer itemId, int index) {
        PlayerCharacter playerCharacter = playerDao.get(playerName);
        Stash stash = get(playerCharacter.getAccountId());

        if(stash.getItems().get(index) == null) {
            throw new IllegalArgumentException("You have no item in your stash to withdraw from index " + index + ".");
        }
        
	    Item removedItem = stash.getItems().get(index);
	    stash.getItems().set(index, null);
        
        if(!itemId.equals(removedItem.getId())) {
            throw new IllegalArgumentException("Item at stash index " + index + " has id " + removedItem.getId() + ", but I expected " + itemId + ".");
        }
	    
        Inventory inventory = inventoryDao.get(playerName);
        inventory.getItems().add(removedItem);
        
        statisticsDao.increment(playerName, "withdraw-item");
        if(log.isDebugEnabled()) {
            log.debug(playerName + " is withdrawing " + removedItem.getName() + " (" + removedItem.getId() + ") from their stash.");
        }
	}

    @Override
    protected Class<?> getManagedClass() {
        return Stash.class;
    }
}