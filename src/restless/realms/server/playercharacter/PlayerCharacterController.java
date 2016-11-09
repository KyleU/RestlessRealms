package restless.realms.server.playercharacter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.combat.pvp.PvpCombatDao;
import restless.realms.server.combat.pvp.PvpDefenses;
import restless.realms.server.equipment.EquipmentDao;
import restless.realms.server.mob.MobArchetype;
import restless.realms.server.mob.MobDao;
import restless.realms.server.playercharacter.statistics.Statistic;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.quest.progress.QuestProgressDao;
import restless.realms.server.session.Session;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.JsonUtils.ResponseStatus;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/character/**")
public class PlayerCharacterController {
    private Map<String, String> mobNames;
    
    @Autowired
    private PlayerCharacterDao playerCharacterDao;
    
    @Autowired
    private EquipmentDao equipmentDao;
    
    @Autowired
    private StatisticsDao statisticsDao;
    
    @Autowired
    private QuestProgressDao questProgressDao;

    @Autowired
    private MobDao mobDao;
    
    @Autowired
    private PvpCombatDao pvpCombatDao;

    @PostConstruct
    public void init() {
        mobNames = new LinkedHashMap<String, String>();
        List<MobArchetype> mobs = mobDao.getAllArchetypes();
        for(MobArchetype mobArchetype : mobs) {
            mobNames.put(mobArchetype.getId(), mobArchetype.getName());
        }
    }
    
    @RequestMapping("quickslots")
    public ModelAndView quickslots(HttpServletRequest request, @RequestParam("quickslots") String quickslots) {
        Session s = RequestUtils.getSession(request);
        String setQuickslotsReturn = playerCharacterDao.setQuickslots(s.getCharacterName(), quickslots);
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("quickslots", setQuickslotsReturn);
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("equip")
    public ModelAndView equip(HttpServletRequest request, @RequestParam("id") Integer id) {
        Session s = RequestUtils.getSession(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("equipment", equipmentDao.equip(s.getCharacterName(), id));
        PlayerCharacter pc = playerCharacterDao.get(s);
        ret.put("pc", pc.getClientRepresentation());
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("equipperk")
    public ModelAndView equipperk(HttpServletRequest request, @RequestParam("id") Integer id, @RequestParam("slot") int slot) {
        Session s = RequestUtils.getSession(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("equipment", equipmentDao.equipPerk(s.getCharacterName(), id, slot));
        PlayerCharacter pc = playerCharacterDao.get(s);
        ret.put("pc", pc.getClientRepresentation());
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("journal")
    public ModelAndView journal(HttpServletRequest request, @RequestParam(value="id", required=false) Integer id) {
        Session s = RequestUtils.getSession(request);
        Map<String, Object> ret = new HashMap<String, Object>();
        List<Map<String, String>> journalPages = questProgressDao.getJournalPages(s.getCharacterName());
        ret.put("journal", journalPages);
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("bestiary")
    public ModelAndView bestiary(HttpServletRequest request, @RequestParam(value="id", required=false) String id) {
        Session s = RequestUtils.getSession(request);
        Map<String, Object> ret = new HashMap<String, Object>();
        if(id == null) {
            List<String> killedIds = new ArrayList<String>();
            List<Statistic> playerStats = statisticsDao.getPlayerStats(s.getCharacterName());
            for(Statistic statistic : playerStats) {
                String statisticKey = statistic.getKey().getStatistic();
                if(statisticKey.startsWith("kill-")) {
                    killedIds.add(statisticKey.substring(5));
                }
            }
            
            Map<String, String> mobsKilled = new LinkedHashMap<String, String>();
            for(Entry<String, String> mob : mobNames.entrySet()) {
                if(killedIds.contains(mob.getKey())) {
                    mobsKilled.put(mob.getKey(), mob.getValue());
                }
            }
            ret.put("mobs", mobsKilled);
            
            PvpDefenses pvpDefenses = pvpCombatDao.getDefenses(playerCharacterDao.get(s));
            ret.put("bodyguards", pvpDefenses .getEnemies());
        } else {
            MobArchetype archetype = mobDao.getArchetype(id);
            if(archetype == null) {
                throw new IllegalArgumentException("Invalid mob \"" + id + "\".");
            }
            int numKilled = statisticsDao.get(s.getCharacterName(), "kill-" + id);
            if(numKilled == 0) {
                throw new IllegalArgumentException("You have not encountered mob \"" + id + "\".");
            }
            ret.put("numKilled", numKilled);
            ret.put("mob", archetype);
            statisticsDao.increment(s.getCharacterName(), "bestiary-view");
        }
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("get")
    public ModelAndView get(HttpServletRequest request) {
        Session s = RequestUtils.getSession(request);
        PlayerCharacter playerCharacter = playerCharacterDao.get(s);
        Map<String, Object> pc = playerCharacter.getClientRepresentation();
        pc.put("xp", playerCharacter.getXp());
        return JsonUtils.getModelAndView(ResponseStatus.OK, pc);
    }
}
