package restless.realms.server.playercharacter;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
import restless.realms.server.chat.ChatMessageDao;
import restless.realms.server.combat.action.CombatAction;
import restless.realms.server.database.AbstractDao;
import restless.realms.server.database.FieldLengths;
import restless.realms.server.effect.EffectType;
import restless.realms.server.equipment.Equipment;
import restless.realms.server.equipment.EquipmentBonuses;
import restless.realms.server.equipment.EquipmentDao;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.item.Item;
import restless.realms.server.item.ItemDao;
import restless.realms.server.item.ItemType;
import restless.realms.server.perk.PerkDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.profession.Profession;
import restless.realms.server.profession.ProfessionDao;
import restless.realms.server.quest.progress.QuestProgressDao;
import restless.realms.server.session.Session;
import restless.realms.server.skill.SkillDao;
import restless.realms.server.util.ScaleOptions;

import com.google.common.base.Joiner;

@Repository
public class PlayerCharacterDao extends AbstractDao<PlayerCharacter> {
    private static final Log log = LogFactory.getLog(PlayerCharacterDao.class);
    private static final Pattern pattern = Pattern.compile("[a-zA-Z0-9 ^_\\-\\.]+");
    
    @Autowired
    private ProfessionDao professionDao;

    @Autowired
    private InventoryDao inventoryDao;

    @Autowired
    private ItemDao itemDao;
    
    @Autowired
    private PerkDao perkDao;
    
    @Autowired
    private SkillDao skillDao;
    
    @Autowired
    private ChatMessageDao chatMessageDao;

    @Autowired
    private EquipmentDao equipmentDao;

    @Autowired
    private AdventureDao adventureDao;

    @Autowired
    private QuestProgressDao questProgressDao;
    
    @Autowired
    private StatisticsDao statisticsDao;
    
    public int[] requiredXpByLevel;
    
    public PlayerCharacterDao() {
        requiredXpByLevel = new int[ScaleOptions.MAX_PLAYER_LEVEL + 1];
        for(int level = 0; level < requiredXpByLevel.length; level++) {
            //also in PlayerCharacterDisplay.setPlayerCharacter
            int requiredXp = (level * level * 6000) - 5000;
            requiredXpByLevel[level] = requiredXp;
        }
    }
    
    public void create(int accountId, String name, String professionName, String gender) {
        List<PlayerCharacter> pcs = getByAccount(accountId);
        for(PlayerCharacter playerCharacter : pcs) {
            if(playerCharacter.getProfession().equals(professionName)) {
                throw new IllegalArgumentException("You have already created a " + professionName + " (" + playerCharacter.getName() + ").");
            }
        }

        name = name.trim();
        if(name.length() == 0 || name.equalsIgnoreCase("name")) {
            throw new IllegalArgumentException("Please type a name.");
        }
        if(
                name.contains(">") || 
                name.contains("<") || 
                name.contains("-") || 
                name.contains("\"") || 
                name.contains("\'") || 
                name.contains("\\") || 
                name.contains("/") || 
                name.contains("?") || 
                name.contains(".") || 
                name.contains("+") || 
                name.contains("!") || 
                name.contains("@") || 
                name.contains("#") || 
                name.contains("%") || 
                name.contains("^") || 
                name.contains("&") || 
                name.contains("*") || 
                name.contains(":") || 
                name.contains(";")
        ) {
            throw new IllegalArgumentException("Your name may not contain special characters.");
        }
        if(!pattern.matcher(name).matches()) {
            throw new IllegalArgumentException("Your name may contain only common letters, numbers, and symbols.");
        }
        if(name.length() > FieldLengths.PLAYER_NAME) {
            throw new IllegalArgumentException("That name is too long (" + FieldLengths.PLAYER_NAME + " characters at most).");
        }
        if(get(name) != null) {
            throw new IllegalArgumentException("That name is already taken.");
        }
        
        PlayerCharacter pc = new PlayerCharacter(name, accountId);
        pc.setGender(gender.charAt(0));
        Profession profession = professionDao.getProfession(professionName);
        pc.setProfession(profession.getId());
        pc.setMaxHitpoints(profession.getInitialHitpoints());
        pc.setHitpoints(profession.getInitialHitpoints());
        pc.setMaxMana(profession.getInitialMana());
        pc.setMana(profession.getInitialMana());
        pc.setQuickslots(profession.getInitialQuickslots());

        template.save(pc);
        
        questProgressDao.accept(name, "welcome");
    }

    public PlayerCharacter get(Session session) {
        String characterName = session.getCharacterName();
        if(characterName == null) {
            throw new IllegalArgumentException("No player character selected.");
        }

        PlayerCharacter player = get(characterName);
        if(player == null) {
            throw new IllegalArgumentException("Invalid player \"" + characterName + "\".");
        }
        return player;
    }
    
    public PlayerCharacter get(String name) {
        return super.get(name);
    }

    @SuppressWarnings("unchecked")
    public List<PlayerCharacter> getByAccount(int accountId) {
        List<PlayerCharacter> ret = template.findByNamedQuery("player.getByAccount", accountId);
        return ret;
    }
    
    @Transactional
    public void disable(Integer accountId, String playerCharacterName) {
        PlayerCharacter character = get(playerCharacterName);
        if(!character.getAccountId().equals(accountId)) {
            throw new IllegalArgumentException("You can't delete a character that isn't yours, you naughty hacker.");
        }
        character.setEnabled(false);
        character.setName(character.getName());
    }

    @Transactional
    public PlayerCharacter updateSignedIn(String name) {
        PlayerCharacter playerCharacter = get(name);
        playerCharacter.setLastSignedIn(new Date());
        return playerCharacter;
    }

    @Transactional
    public String setQuickslots(String characterName, String quickslots) {
        String[] strings = quickslots.split(",");
        if(strings.length != 8) {
            throw new IllegalArgumentException("Invalid number of quickslots for \"" + quickslots + "\"");
        }

        PlayerCharacter character = get(characterName);

        for(int i = 0; i < strings.length; i++) {
            String quickslot = strings[i];
            if(!quickslot.equals(quickslot.trim())) {
                throw new IllegalArgumentException("Invalid quickslot \"" + quickslot + "\".");
            }
            if(!quickslot.equals("0")) {
                int actionId = Integer.parseInt(quickslot.substring(1));
                char quickslotType = quickslot.charAt(0);
                if(quickslotType == 'i') {
                    try {
                        inventoryDao.validateAction(character, actionId);
                    } catch(Exception e) {
                        quickslot = "0";
                    }
                    if(!quickslot.equals("0")) {
                        Item item = itemDao.get(actionId);
                        ItemType type = item.getType();
                        if(!ItemType.CONSUMABLE.equals(type)) {
                            String message = "You may not assign " + item.getName() + " to your quickslots, as it is ";
                            if(ItemType.WEAPON == type) {
                                message += "a weapon.";
                            } else {
                                message += "equipment for your " + type.toString().toLowerCase() + ".";
                            }
                            message += " Drag it to your portrait to equip.";
                            throw new IllegalArgumentException(message);
                        }
                    }
                } else if(quickslotType == 's') {
                    skillDao.validateAction(character, actionId);
                } else {
                    throw new IllegalArgumentException("Invalid quickslot \"" + quickslot + "\".");
                }
            }
        }
        String quickslotsString = Joiner.on(",").join(strings);
        character.setQuickslots(quickslotsString);
        return quickslotsString;
    }

    public void applyBonuses(PlayerCharacter pc, EquipmentBonuses bonuses) {
        Profession profession = professionDao.getProfession(pc.getProfession());
        
        int hp = profession.getInitialHitpoints() + (profession.getHitpointsPerLevel() * (pc.getLevel() - 1));
        int mana = profession.getInitialMana() + (profession.getManaPerLevel() * (pc.getLevel() - 1));
        pc.setMaxHitpoints(hp + bonuses.getHitpoints());
        pc.setMaxMana(mana + bonuses.getMana());

        boolean healFully = false;
        if(pc.getActiveAdventureId() == null) {
            healFully = true;
        } else {
            Adventure adventure = adventureDao.getAdventure(pc.getActiveAdventureId());
            if(adventure == null || adventure.getStatus() != Status.ACTIVE) {
                healFully = true;
            }
        }
        
        if(healFully) {
            pc.setHitpoints(hp + bonuses.getHitpoints());
            pc.setMana(mana + bonuses.getMana());
        }
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void addXp(PlayerCharacter pc, int xp) {
        int originalXp = pc.getXp();
        int originalLevel = pc.getLevel();
        int requiredXp = requiredXpByLevel[originalLevel];
        int newXp;
        if(pc.getLevel() == ScaleOptions.MAX_PLAYER_LEVEL) {
            newXp = 0;
        } else {
            newXp = originalXp + xp;
        }
        if(newXp >= requiredXp) {
            int newLevel = originalLevel + 1;
            pc.setLevel(newLevel);
            if(newLevel == ScaleOptions.MAX_PLAYER_LEVEL) {
                newXp = 0;
            } else {
                newXp = newXp - requiredXp;
            }
            applyBonuses(pc, equipmentDao.getBonuses(pc.getName()));
            pc.setHitpoints(pc.getMaxHitpoints());
            pc.setMana(pc.getMaxMana());
            
            log.info("Ding! \"" + pc.getName() + "\" just reached level " + newLevel + "!");
            chatMessageDao.post(pc.getName(), "Region", " just reached level " + newLevel + "!");
            statisticsDao.increment(pc.getName(), "level-up");
        }
        pc.setXp(newXp);
    }
    
    public void validateAction(CombatAction combatAction, String characterName) {
        PlayerCharacter character = get(characterName);

        if(combatAction.getActionType() == 'i') {
            inventoryDao.validateAction(character, combatAction.getActionId());
            Item item = itemDao.get(combatAction.getActionId());
            ItemType type = item.getType();
            if(!ItemType.CONSUMABLE.equals(type)) {
                throw new IllegalArgumentException("You may not use \"" + item.getName() + "\" in combat or assign it to your quickslots, as it is equipment for your " + type.toString().toLowerCase() + ".");
            }
        } else if(combatAction.getActionType() == 's') {
            skillDao.validateAction(character, combatAction.getActionId());
        } else {
            throw new IllegalArgumentException("Invalid combat action.");
        }
    }

    public Map<String, Object> getInfo(String name) {
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        
        name = getProperName(name);
        PlayerCharacter pc = template.get(PlayerCharacter.class, name);
        if(pc == null || !pc.isEnabled()) {
            throw new IllegalArgumentException("There is no character named \"" + name + "\".");
        }
        Map<String, Object> pcMap = pc.getClientRepresentation();
        pcMap.put("level", pc.getLevel());
        pcMap.put("gender", pc.getGender());
        ret.put("playerCharacter", pcMap);
        
        
        Equipment e = equipmentDao.get(pc.getName());
        Action[] actions = new Action[7];
        actions[0] = e.getHead() == null ? null : itemDao.get(e.getHead());
        actions[1] = e.getChest() == null ? null : itemDao.get(e.getChest());
        actions[2] = e.getLegs() == null ? null : itemDao.get(e.getLegs());
        actions[3] = e.getAccessory() == null ? null : itemDao.get(e.getAccessory());
        actions[4] = e.getWeapon() == null ? null : itemDao.get(e.getWeapon());
        actions[5] = e.getPerkOne() == null ? null : perkDao.get(e.getPerkOne());
        actions[6] = e.getPerkTwo() == null ? null : perkDao.get(e.getPerkTwo());
        ret.put("equipment", actions);
  
        ret.put("kills", statisticsDao.get(pc.getName(), "deal-DEATH"));
        ret.put("deaths", statisticsDao.get(pc.getName(), "receive-DEATH"));
        ret.put("adventure-complete", statisticsDao.get(pc.getName(), "adventure-complete"));
        
        EquipmentBonuses bonuses = equipmentDao.getBonuses(pc.getName());
        ret.put("physical-bonus", bonuses.getBonus(EffectType.PHYSICAL));
        ret.put("fire-bonus", bonuses.getBonus(EffectType.FIRE));
        ret.put("ice-bonus", bonuses.getBonus(EffectType.ICE));
        ret.put("electric-bonus", bonuses.getBonus(EffectType.ELECTRIC));
        
        ret.put("duelscore", statisticsDao.get(pc.getName(), "duelscore"));
        ret.put("pvp-offensive-win", statisticsDao.get(pc.getName(), "pvp-offensive-win"));
        ret.put("pvp-offensive-loss", statisticsDao.get(pc.getName(), "pvp-offensive-loss"));
        ret.put("pvp-defensive-win", statisticsDao.get(pc.getName(), "pvp-defensive-win"));
        ret.put("pvp-defensive-loss", statisticsDao.get(pc.getName(), "pvp-defensive-loss"));
        
        long damageDealt = 0;
        damageDealt += statisticsDao.get(pc.getName(), "deal-PHYSICAL");
        damageDealt += statisticsDao.get(pc.getName(), "deal-FIRE");
        damageDealt += statisticsDao.get(pc.getName(), "deal-ICE");
        damageDealt += statisticsDao.get(pc.getName(), "deal-ELECTRIC");
        ret.put("damage-dealt", damageDealt);

        long damageTaken = 0;
        damageTaken += statisticsDao.get(pc.getName(), "receive-PHYSICAL");
        damageTaken += statisticsDao.get(pc.getName(), "receive-FIRE");
        damageTaken += statisticsDao.get(pc.getName(), "receive-ICE");
        damageTaken += statisticsDao.get(pc.getName(), "receive-ELECTRIC");
        ret.put("damage-taken", damageTaken);
        
        return ret;
    }

    @SuppressWarnings("unchecked")
    public String getProperName(String name) {
        List<String> names = template.findByNamedQuery("player.getProperName", name);
        return names.size() == 0 ? name : names.get(0);
    }

    @SuppressWarnings("unchecked")
    public Iterator<PlayerCharacter> iterate() {
        return template.iterate("from PlayerCharacter pc order by pc.name");
    }

    @Override
    protected Class<?> getManagedClass() {
        return PlayerCharacter.class;
    }

    @SuppressWarnings("unchecked")
    public List<String> getNamesByLevel(int level) {
        if(level < 1 || level > ScaleOptions.MAX_PLAYER_LEVEL) {
            throw new IllegalStateException("Level must be between 1 and " + ScaleOptions.MAX_PLAYER_LEVEL + ".");
        }
        List<String> ret = template.findByNamedQuery("player.getNamesByLevel", level);
        return ret ;
    }
}