package restless.realms.server.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.equipment.Equipment;
import restless.realms.server.equipment.EquipmentDao;
import restless.realms.server.inventory.Inventory;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.item.Item;
import restless.realms.server.item.ItemDao;
import restless.realms.server.perk.Perk;
import restless.realms.server.perk.PerkDao;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.profession.ProfessionDao;
import restless.realms.server.profession.SkillTree;
import restless.realms.server.session.Session;
import restless.realms.server.skill.Skill;
import restless.realms.server.skill.SkillDao;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.JsonUtils.ResponseStatus;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/shop/**")
public class ShopController {
    @Autowired
    private ShopDao shopDao;

    @Autowired
    private ItemDao itemDao;
    
    @Autowired
    private InventoryDao inventoryDao;

    @Autowired
    private SkillDao skillDao;
    
    @Autowired
    private PerkDao perkDao;
    
    @Autowired
    private PlayerCharacterDao playerCharacterDao;
    
    @Autowired
    private EquipmentDao equipmentDao;
    
    @Autowired
    private ProfessionDao professionDao;
    
    @RequestMapping("list")
    public ModelAndView list(HttpServletRequest request, @RequestParam("merchant") String merchant) {
        Session s = RequestUtils.getSession(request);
        
        Map<String, Object> ret = new HashMap<String, Object>();
        PlayerCharacter pc = playerCharacterDao.get(s);
        Shop shop = shopDao.get(merchant);
        ret.put("name", shop.getName());
        ret.put("items", shop.getItemsForProfession(pc.getProfession()));
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("skills")
    public ModelAndView skills(HttpServletRequest request) {
        Session s = RequestUtils.getSession(request);
        Map<String, Object> ret = new HashMap<String, Object>();
        PlayerCharacter p = playerCharacterDao.get(s.getCharacterName());
        SkillTree skillTree = professionDao.getSkillTree(p.getProfession());
        
        
        List<Integer> knownSkillsIds = new ArrayList<Integer>();
        List<Skill> skillset = skillDao.getSkillset(p.getName());
        for(Skill skill : skillset) {
            knownSkillsIds.add(skill.getId());
        }
        
        Map<Integer, List<Skill>> skills = new LinkedHashMap<Integer, List<Skill>>();
        for(Entry<Integer, List<Integer>> entry : skillTree.getSkillIdsByLevel().entrySet()) {
            for(Integer skillId : entry.getValue()) {
                Integer level = entry.getKey();
                if(!skills.containsKey(level)) {
                    skills.put(level, new ArrayList<Skill>());
                }
                Skill skill = skillDao.get(skillId);
                if(!knownSkillsIds.contains(skill.getId())) {
                    skills.get(level).add(skill);
                }
            }
        }
        
        ret.put("skills", skills);
        
        List<Integer> knownPerkIds = new ArrayList<Integer>();
        List<Perk> perkset = perkDao.getPerkset(p.getName());
        for(Perk knownPerk : perkset) {
            knownPerkIds.add(knownPerk.getId());
        }
        
        List<Perk> allPerks = perkDao.getForSale();
        List<Perk> availablePerks = new ArrayList<Perk>();
        for(Perk perk : allPerks) {
            if(perk.getMsrp() > 0) {
                if(!knownPerkIds.contains(perk.getId())) {
                    availablePerks.add(perk);
                }
            }
        }
        
        ret.put("perks", availablePerks);
        
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("train")
    public ModelAndView train(HttpServletRequest request, @RequestParam("skill") Integer skillId) {
        Session s = RequestUtils.getSession(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        List<Skill> skills = skillDao.train(s.getCharacterName(), skillId, false);
        ret.put("skills", skills);
        Integer currency = inventoryDao.getCurrency(s.getCharacterName());
        ret.put("currency", currency);
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("trainperk")
    public ModelAndView trainperk(HttpServletRequest request, @RequestParam("perk") Integer perkId) {
        Session s = RequestUtils.getSession(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        List<Perk> perks = perkDao.train(s.getCharacterName(), perkId);
        ret.put("perks", perks);
        Integer tokens = inventoryDao.getTokens(s.getCharacterName());
        ret.put("tokens", tokens);
        Equipment equipment = equipmentDao.get(s.getCharacterName());
        ret.put("equipment", equipment);
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("iteminfo")
    public ModelAndView iteminfo(HttpServletRequest request, @RequestParam("name") String itemName) {
        Item i = itemDao.getByName(itemName);
        if(i == null) {
            throw new IllegalArgumentException("There is no item with name \"" + itemName + "\".");
        }
        return JsonUtils.getModelAndView(ResponseStatus.OK, i);
    }

    @RequestMapping("buy")
    public ModelAndView buy(HttpServletRequest request, @RequestParam("merchant") String merchant, @RequestParam("item") Integer itemId) {
        Session s = RequestUtils.getSession(request);
        Inventory inventory = inventoryDao.buyItem(s.getCharacterName(), merchant, itemId);
        return JsonUtils.getModelAndView(ResponseStatus.OK, inventory);
    }

    @RequestMapping("buyall")
    public ModelAndView buyall(HttpServletRequest request, @RequestParam("merchant") String merchant, @RequestParam("item") Integer itemId) {
        Session s = RequestUtils.getSession(request);
        Inventory inventory = inventoryDao.buyItem(s.getCharacterName(), merchant, itemId);
        while(true) {
            try {
                inventory = inventoryDao.buyItem(s.getCharacterName(), merchant, itemId);
            } catch(Exception e) {
                // no op, expected eventually
                break;
            }
        }
        return JsonUtils.getModelAndView(ResponseStatus.OK, inventory);
    }

    @RequestMapping("sell")
    public ModelAndView sell(HttpServletRequest request, @RequestParam("index") Integer index, @RequestParam("item") Integer itemId) {
        Session s = RequestUtils.getSession(request);
        Inventory inventory = inventoryDao.sellItem(s.getCharacterName(), index, itemId);
        return JsonUtils.getModelAndView(ResponseStatus.OK, inventory);
    }

    @RequestMapping("selltrash")
    public ModelAndView selltrash(HttpServletRequest request) {
        Session s = RequestUtils.getSession(request);
        Inventory inventory = inventoryDao.sellAllTrash(s.getCharacterName());
        return JsonUtils.getModelAndView(ResponseStatus.OK, inventory);
    }
}