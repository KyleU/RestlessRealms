package restless.realms.server.combat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.combat.action.CombatAction;
import restless.realms.server.combat.action.CombatActionDao;
import restless.realms.server.effect.EffectTarget;
import restless.realms.server.equipment.EquipmentDao;
import restless.realms.server.item.ItemDao;
import restless.realms.server.mob.Mob;
import restless.realms.server.mob.MobArchetype;
import restless.realms.server.mob.MobDao;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.session.Session;
import restless.realms.server.skill.SkillDao;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.JsonUtils.ResponseStatus;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/combat/**")
public class CombatController {
    private static final Log log = LogFactory.getLog(CombatController.class);
    
    @Autowired
    private MobDao mobDao;

    @Autowired
    private SkillDao skillDao;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private CombatDao combatDao;

    @Autowired
    private CombatActionDao combatActionDao;
    
    @Autowired
    private EquipmentDao equipmentDao;

    public CombatController() {

    }

    @RequestMapping("load")
    public ModelAndView load(HttpServletRequest request) {
        Session session = RequestUtils.getSession(request);
        CombatState combatState = combatDao.getCombatState(session);
        Map<String,Object> ret = getClientRepresentation(combatState);
        int roundNumber = combatState.getCombat().getActiveRoundNumber();
        if(roundNumber > 0) {
            int minRoundNumber = (roundNumber < 10) ? 0 : (roundNumber - 10);
            Map<Integer, Integer> cooldownCache = combatActionDao.getCooldowns(combatState.getCombat().getId(), 0, minRoundNumber);
            ret.put("cooldownCache", cooldownCache);
        }
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("action")
    public ModelAndView action(
            HttpServletRequest request, 
            @RequestParam(value="target") int target, 
            @RequestParam(value="type") String type, 
            @RequestParam(value="id") Integer id
    ) {
        Session session = RequestUtils.getSession(request);
        
        CombatAction combatAction = new CombatAction(null, -1, target);
        combatAction.setActionType("skill".equals(type) ? 's' : "item".equals(type) ? 'i' : '?');
        combatAction.setActionId(id);
        
        CombatState combatState;
        try {
            combatState = combatDao.onPlayerAction(session, combatAction);
        } catch(IllegalArgumentException e) {
            Map<String, Object> model = new LinkedHashMap<String, Object>();
            model.put("code", e.getClass().getSimpleName());
            model.put("message", e.getMessage());

            return JsonUtils.getModelAndView(ResponseStatus.ERROR, model);
        } catch(CannotAcquireLockException e) {
            log.error("Encountered CannotAcquireLockException when performing combat action. Retrying.");
            combatState = combatDao.onPlayerAction(session, combatAction);
        }

        List<Map<String, Object>> actions = new ArrayList<Map<String, Object>>(); 
        List<String> actionNames = new ArrayList<String>(); 
        for(CombatAction action : combatState.getActiveRound().getActions()) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("source", action.getSource());
            map.put("target", action.getTarget());
            map.put("type", action.getActionType());
            map.put("id", action.getActionId());
            map.put("results", action.getEffectResults());
            actions.add(map);
            if(action.getActionType() == 's') {
                String skillName = skillDao.get(action.getActionId()).getName();
                actionNames.add(skillName);
            } else {
                String itemName = itemDao.get(action.getActionId()).getName();
                actionNames.add(itemName);
            }
        }

        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("roundNumber", combatState.getActiveRound().getRoundNumber());
        ret.put("state", combatState.getActiveRound().getState());
        ret.put("actions", actions);
        ret.put("actionNames", actionNames);
        List<Map<String, Object>> stats = new ArrayList<Map<String, Object>>();
        for(EffectTarget ally : combatState.getAllies()) {
            stats.add(ally.getClientRepresentation());
        }
        for(int enemyIndex = 0; enemyIndex < combatState.getEnemies().size(); enemyIndex++) {
            EffectTarget enemy = combatState.getEnemyMob(enemyIndex);
            stats.add(enemy.getClientRepresentation());
        }
        ret.put("stats", stats);
        return JsonUtils.getModelAndView(ret);
    }

    private Map<String, Object> getClientRepresentation(CombatState combatState) {
        Map<String,Object> ret = new LinkedHashMap<String, Object>();

        ret.put("roundNumber", combatState.getCombat().getActiveRoundNumber());
        if(combatState.getIntroKey() != null) {
            ret.put("intro", combatState.getIntroKey());
        }

        ret.put("state", combatState.getActiveRound().getState());
        
        List<Map<String, Object>> allies = new ArrayList<Map<String, Object>>();
        for(PlayerCharacter pc : combatState.getAllies()) {
            allies.add(pc.getClientRepresentation());
        }
        ret.put("allies", allies);
        
        List<Map<String, Object>> enemies = new ArrayList<Map<String, Object>>();
        for(int enemyIndex = 0; enemyIndex < combatState.getEnemies().size(); enemyIndex++) {
            EffectTarget enemy = combatState.getEnemies().get(enemyIndex);
            if(enemy instanceof Mob) {
                Mob mob = (Mob)enemy;
                Map<String, Object> mobMap = mob.getClientRepresentation();
                MobArchetype archetype = mobDao.getArchetype(mob.getArchetype());
                mobMap.put("type", archetype.getId());
                mobMap.put("image", archetype.getImage());
                mobMap.put("name", archetype.getName());
                mobMap.put("immunities", archetype.getImmunities());
                mobMap.put("resistances", archetype.getResistances());
                mobMap.put("weaknesses", archetype.getWeaknesses());
                enemies.add(mobMap);
            } else {
                PlayerCharacter pc = (PlayerCharacter)enemy;
                Mob mob = combatState.getEnemyMob(enemyIndex);
                Map<String, Object> mobMap = mob.getClientRepresentation();
                mobMap.put("type", "player");
                mobMap.put("image", "!" + equipmentDao.getPaperdollString(pc));
                mobMap.put("name", pc.getName());
                mobMap.put("immunities", null);
                mobMap.put("resistances", null);
                mobMap.put("weaknesses", null);
                enemies.add(mobMap);
            }
        }
        ret.put("enemies", enemies);
        
        return ret;    
    }
}
