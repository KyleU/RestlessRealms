package restless.realms.server.equipment;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.effect.Effect;
import restless.realms.server.item.Item;
import restless.realms.server.item.ItemDao;
import restless.realms.server.perk.Perk;
import restless.realms.server.perk.PerkDao;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.profession.ProfessionDao;

@Repository
public class EquipmentDao extends AbstractDao<Equipment> {
    private static final Log log = LogFactory.getLog(EquipmentDao.class);
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private ItemDao itemDao;

    @Autowired
    private PerkDao perkDao;

    @Autowired
    private PlayerCharacterDao playerDao;

    @Autowired
    private ProfessionDao professionDao;

    @Autowired
    private StatisticsDao statisticsDao;
    
    private Cache cache;

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Equipment get(String playerName) {
        Equipment result = template.get(Equipment.class, playerName);
        if(result == null) {
            PlayerCharacter p = playerDao.get(playerName);
            if(p == null) {
                IllegalStateException e = new IllegalStateException("Player does not exist!");
                log.error("No player found.", e);
                throw e;
            }
            log.debug("Creating equipment for player \"" + p.getName() + "\"");
            result = new Equipment();
            result.setPlayerName(playerName);
            List<Integer> initialEquipmentIds = professionDao.getProfession(p.getProfession()).getInitialEquipmentIds();

            result.setHead(initialEquipmentIds.get(0));
            result.setChest(initialEquipmentIds.get(1));
            result.setLegs(initialEquipmentIds.get(2));
            result.setAccessory(initialEquipmentIds.get(3));
            result.setWeapon(initialEquipmentIds.get(4));
            
            template.save(result);
            EquipmentBonuses bonuses = calculateBonuses(result);
            playerDao.applyBonuses(p, bonuses);
        }
        return result;
	}
    
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Equipment equip(String playerName, Integer itemId) {
        Equipment equipment = get(playerName);
        PlayerCharacter playerCharacter = playerDao.get(playerName);
        
        Item item = itemDao.get(itemId);
        if(item == null) {
            throw new IllegalArgumentException("Item \"" + itemId + "\" does not exist.");
        }
        if(item.getRequiredProfession() != null) {
            if(!item.getRequiredProfession().equals(playerCharacter.getProfession())) {
                throw new IllegalArgumentException("Only " + item.getRequiredProfession().toLowerCase() + "s may equip " + item.getName() + ".");
            }
        }
        if(item.getMinLevel() > playerCharacter.getLevel()) {
            throw new IllegalArgumentException("You must be level " + item.getMinLevel() + " to equip " + item.getName() + ".");
        }
        switch(item.getType()) {
        case HEAD:
            equipment.setHead(itemId);
            break;
        case CHEST:
            equipment.setChest(itemId);
            break;
        case LEGS:
            equipment.setLegs(itemId);
            break;
        case ACCESSORY:
            equipment.setAccessory(itemId);
            break;
        case WEAPON:
            equipment.setWeapon(itemId);
            break;
        default:
            throw new IllegalArgumentException(item.getName() + " is a " + item.getType().toString().toLowerCase() + ", and cannot be equipped.");
        }
        
        EquipmentBonuses bonuses = calculateBonuses(equipment);
        playerDao.applyBonuses(playerCharacter, bonuses);
        
        statisticsDao.increment(playerName, "equip-item");
        
        return equipment;
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Equipment equipPerk(String playerName, Integer perkId, int slot) {
        Equipment equipment = get(playerName);
        PlayerCharacter playerCharacter = playerDao.get(playerName);
        
        Perk perk = perkDao.get(perkId);
        if(perk == null) {
            throw new IllegalArgumentException("Item \"" + perkId + "\" does not exist.");
        }
        if(perk.getMinLevel() > playerCharacter.getLevel()) {
            throw new IllegalArgumentException("You must be level " + perk.getMinLevel() + " to equip " + perk.getName() + ".");
        }
        switch(slot) {
        case 0:
            if(perkId.equals(equipment.getPerkTwo())) {
                throw new IllegalArgumentException("You cannot equip this perk twice.");
            }
            equipment.setPerkOne(perkId);
            break;
        case 1:
            if(perkId.equals(equipment.getPerkOne())) {
                throw new IllegalArgumentException("You cannot equip this perk twice.");
            }
            equipment.setPerkTwo(perkId);
            break;
        default:
            throw new IllegalArgumentException("Invalid perk index " + slot);
        }
        
        EquipmentBonuses bonuses = calculateBonuses(equipment);
        playerDao.applyBonuses(playerCharacter, bonuses);

        statisticsDao.increment(playerName, "equip-perk");
        
        return equipment;
    }

    public EquipmentBonuses getBonuses(String playerName) {
        if(cache == null) {
            cache = cacheManager.getCache("restless.realms.server.equipment.EquipmentBonuses");
        }
        Element element = cache.get(playerName);
        if(element == null || element.getObjectValue() == null) {
            calculateBonuses(get(playerName));
            element = cache.get(playerName);
        }
        return (EquipmentBonuses)element.getObjectValue();
    }

    private EquipmentBonuses calculateBonuses(Equipment equipment) {
        if(cache == null) {
            cache = cacheManager.getCache("restless.realms.server.equipment.EquipmentBonuses");
        }

        EquipmentBonuses bonuses = new EquipmentBonuses();
        add(equipment.getHead(), bonuses, false);
        add(equipment.getChest(), bonuses, false);
        add(equipment.getLegs(), bonuses, false);
        add(equipment.getAccessory(), bonuses, false);
        
        add(equipment.getPerkOne(), bonuses, true);
        add(equipment.getPerkTwo(), bonuses, true);
        
        Integer weaponId = equipment.getWeapon();
        if(weaponId == null || weaponId.equals(0)) {
            bonuses.setWeaponEffects(null);
        } else {
            Item weapon = itemDao.get(weaponId);
            bonuses.setWeaponEffects(weapon.getEffects());
        }
        
        log.debug("Calculating equipment bonuses for \"" + equipment.getPlayerName() + "\".");
        cache.put(new Element(equipment.getPlayerName(), bonuses));
        return bonuses;
    }

    private void add(Integer id, EquipmentBonuses bonuses, boolean isPerk) {
        if(id != null && id > 0) {
            List<Effect> effects;
            if(isPerk) {
                Perk perk = perkDao.get(id);
                effects = perk.getEffects();
            } else {
                Item item = itemDao.get(id);
                effects = item.getEffects();
            }
            for(Effect effect : effects) {
                switch(effect.getEffectType()) {
                case HEALING:
                    bonuses.setHitpoints(bonuses.getHitpoints() + effect.getMinQuantity());
                    break;
                case REPLENISH:
                    bonuses.setMana(bonuses.getMana() + effect.getMinQuantity());
                    break;
                case PHYSICAL:
                    bonuses.setPhysical(bonuses.getPhysical() + effect.getMinQuantity());
                    break;
                case FIRE:
                    bonuses.setFire(bonuses.getFire() + effect.getMinQuantity());
                    break;
                case ICE:
                    bonuses.setIce(bonuses.getIce() + effect.getMinQuantity());
                    break;
                case ELECTRIC:
                    bonuses.setElectric(bonuses.getElectric() + effect.getMinQuantity());
                    break;
                default:
                    throw new IllegalArgumentException("Effect type \"" + effect.getEffectType() + "\" does not apply to equipment.");
                }
            }
        }
    }

    @Override
    protected Class<?> getManagedClass() {
        return Equipment.class;
    }

    public String getPaperdollString(PlayerCharacter pc) {
        Equipment equipment = get(pc.getName());
        String gender = pc.getGender() == 'M' ? "male" : "female";
        
        String ret = "";
        ret += gender + "/model/default";
        ret += ",";
        ret += getPaperdollString(gender, equipment.getLegs());
        ret += ",";
        ret += getPaperdollString(gender, equipment.getChest());
        ret += ",";
        ret += getPaperdollString(gender, equipment.getHead());
        ret += ",";
        ret += getPaperdollString(gender, equipment.getAccessory());
        ret += ",";
        ret += getPaperdollString(gender, equipment.getWeapon());
        return ret;
    }

    private String getPaperdollString(String gender, Integer itemId) {
        String ret = gender + "/";
        Item i = itemDao.get(itemId);
        if(i == null) {
            ret += "background/empty";
        } else {
            ret += i.getType().toString().toLowerCase() + "/" + i.getIcon().getX() + "-" + i.getIcon().getY();
        }
        return ret;
    }
}