package restless.realms.test.server.player;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import restless.realms.server.exception.InsufficientFundsException;
import restless.realms.server.inventory.Inventory;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.item.Item;
import restless.realms.server.item.ItemDao;
import restless.realms.test.server.IntegrationTestCase;

public class InventoryTest extends IntegrationTestCase {
    @Autowired
    private InventoryDao inventoryDao;
    
    @Autowired
    private ItemDao itemDao;
    
    @Test
    public void testGet() {
        Inventory i = inventoryDao.get("Test User");
        Assert.assertEquals(500, i.getCurrency());
        Assert.assertEquals("Rusty Sword", i.getItems().get(0).getName());
    }

    @Test
    public void testPurchases() {
        Item item = itemDao.get(1000);
        Inventory inventory = inventoryDao.get("Test User");
        int initialInvSize = inventory.getItems().size();
        Assert.assertEquals(500, inventory.getCurrency());
        inventoryDao.buyItem("Test User", "armory", item.getId());

        Assert.assertEquals(initialInvSize + 1, inventory.getItems().size());
        Assert.assertEquals(450, inventory.getCurrency());
    }

    @Test
    public void testPurchaseTransactionality() {
        Inventory inv = inventoryDao.get("Test User");
        int initialInvSize = inv.getItems().size();
        Assert.assertEquals(500, inv.getCurrency());

        Item item = itemDao.get(2000);

        for(int i = 0; i < 10; i++) {
            inv = inventoryDao.buyItem("Test User", "armory", item.getId());
            Assert.assertEquals(i + 6, inv.getItems().size());
        }
        Assert.assertEquals(0, inv.getCurrency());

        try {
            inv = inventoryDao.buyItem("Test User", "armory", item.getId());
            throw new IllegalStateException("Expected InsufficientFundsException");
        } catch(InsufficientFundsException e) {
            // expected
        }
        inventoryDao.sellItem("Test User", 6, item.getId());
        inventoryDao.sellItem("Test User", 6, item.getId());
        inventoryDao.sellItem("Test User", 6, item.getId());

        Assert.assertEquals(initialInvSize + 7, inv.getItems().size());
        Assert.assertEquals(75, inv.getCurrency());

        inventoryDao.buyItem("Test User", "armory", item.getId());
        try {
            inv = inventoryDao.buyItem("Test User", "armory", item.getId());
            throw new IllegalStateException("Expected InsufficientFundsException");
        } catch(InsufficientFundsException e) {
            // expected
        }

        Assert.assertEquals(13, inv.getItems().size());
        Assert.assertEquals(25, inv.getCurrency());
    }

    @Test
    public void testSellItem() {
        Inventory inv = inventoryDao.get("Test User");
        int invSize = inv.getItems().size();
        int currency = inv.getCurrency();
        inventoryDao.sellItem("Test User", 0, inv.getItems().get(0).getId());
        Assert.assertEquals(invSize - 1, inv.getItems().size());
        Assert.assertEquals(currency + 25, inv.getCurrency());
        
        inv = inventoryDao.get("Test User");
        Assert.assertEquals(invSize - 1, inv.getItems().size());
        Assert.assertEquals(currency + 25, inv.getCurrency());
    }
}
