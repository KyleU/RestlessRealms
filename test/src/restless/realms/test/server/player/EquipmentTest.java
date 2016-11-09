package restless.realms.test.server.player;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import restless.realms.server.equipment.Equipment;
import restless.realms.server.equipment.EquipmentBonuses;
import restless.realms.server.equipment.EquipmentDao;
import restless.realms.test.server.IntegrationTestCase;

public class EquipmentTest extends IntegrationTestCase {
    @Autowired
    private EquipmentDao equipmentDao;
    
    @Test
    public void testGet() {
        Equipment equipment = equipmentDao.get("Test User");
        Assert.assertNotNull(equipment);
    }

    @Test
    public void testGetBonuses() {
        EquipmentBonuses equipmentBonuses = equipmentDao.getBonuses("Test User");
        Assert.assertEquals(10, equipmentBonuses.getHitpoints());
        Assert.assertEquals(5, equipmentBonuses.getMana());
    }
}