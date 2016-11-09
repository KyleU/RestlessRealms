package restless.realms.server.combat.pvp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.mob.MobArchetype;
import restless.realms.server.mob.MobDao;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.session.Session;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.JsonUtils.ResponseStatus;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/pvp/**")
public class PvpController {
    @Autowired
    private PlayerCharacterDao playerCharacterDao;
    
    @Autowired
    private PvpCombatDao pvpCombatDao;

    @Autowired
    private InventoryDao inventoryDao;
    
    @Autowired
    private StatisticsDao statisticsDao;
    
    @Autowired
    private MobDao mobDao;

    @RequestMapping("pcinfo")
    public ModelAndView pcinfo(HttpServletRequest request, @RequestParam(required=false, value="name") String name) {
        Session s = RequestUtils.getSession(request);

        if(name == null || name.trim().length() == 0) {
            name = s.getCharacterName();
        }
        
        Map<String, Object> ret = playerCharacterDao.getInfo(name);
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("pvpinfo")
    public ModelAndView pvpinfo(HttpServletRequest request, @RequestParam("name") String name) {
        Session s = RequestUtils.getSession(request);
        
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("allyName", s.getCharacterName());
        ret.put("enemyName", name);
        
        Map<String, Object> rewards = pvpCombatDao.getRewards(s.getCharacterName(), name);
        ret.put("rewards", rewards);
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("defenses")
    public ModelAndView defenses(HttpServletRequest request, @RequestParam(value="hax", required=false) Integer currency) {
        Session s = RequestUtils.getSession(request);
        
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        
        PlayerCharacter pc = playerCharacterDao.get(s);
        PvpDefenses defenses = pvpCombatDao.getDefenses(pc);
        ret.put("skills", defenses.getSkills());
        ret.put("enemies", defenses.getEnemies());
        List<String> names = new ArrayList<String>(3);
        List<Integer> levels = new ArrayList<Integer>(3);
        List<Integer> upkeeps = new ArrayList<Integer>(3);
        for(String enemy : defenses.getEnemies()) {
            MobArchetype mob = mobDao.getArchetype(enemy);
            names.add(mob.getName());
            levels.add(mob.getLevel());
            upkeeps.add(mob.getUpkeepCost());
        }
        ret.put("names", names);
        ret.put("levels", levels);
        ret.put("upkeeps", upkeeps);
        if(currency != null && currency > 0) {
            ret.put("currency", currency);
        }
        
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("setdefenses")
    public ModelAndView setdefenses(HttpServletRequest request, @RequestParam("defenses") String defenses) {
        Session s = RequestUtils.getSession(request);
        pvpCombatDao.setDefenses(s.getCharacterName(), defenses);
        return defenses(request, null);
    }
    
    @RequestMapping("setbodyguard")
    public ModelAndView setbodyguard(HttpServletRequest request, @RequestParam("type") String type) {
        Session s = RequestUtils.getSession(request);
        int currency = pvpCombatDao.setBodyguard(s.getCharacterName(), type);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("currency", currency);
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("clearbodyguard")
    public ModelAndView clearbodyguard(HttpServletRequest request, @RequestParam("type") String type) {
        Session s = RequestUtils.getSession(request);
        pvpCombatDao.clearBodyguard(s.getCharacterName(), type);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("start")
    public ModelAndView start(HttpServletRequest request, @RequestParam("enemy") String enemy) {
        Session session = RequestUtils.getSession(request);
        int combatId = pvpCombatDao.start(session, enemy);

        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("combatId", combatId);
        return JsonUtils.getModelAndView(ResponseStatus.OK, ret);
    }

    @RequestMapping("result")
    public ModelAndView result(HttpServletRequest request) {
        Session session = RequestUtils.getSession(request);
        PlayerCharacter playerCharacter = playerCharacterDao.get(session);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("playerDuelScore", statisticsDao.get(playerCharacter.getName(), "duelscore"));
        ret.put("playerCurrency", inventoryDao.getCurrency(playerCharacter.getName()));
        ret.put("playerTokens", inventoryDao.getTokens(playerCharacter.getName()));
        ret.put("playerXp", playerCharacter.getXp());
        return JsonUtils.getModelAndView(ret);
    }
}
