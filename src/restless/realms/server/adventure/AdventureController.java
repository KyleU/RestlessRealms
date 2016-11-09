package restless.realms.server.adventure;

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

import restless.realms.server.account.AccountDao;
import restless.realms.server.adventure.map.AdventureMap;
import restless.realms.server.adventure.map.AdventureMapLocation;
import restless.realms.server.combat.action.CombatAction;
import restless.realms.server.effect.EffectResult;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.room.Room;
import restless.realms.server.room.RoomState;
import restless.realms.server.session.Session;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.JsonUtils.ResponseStatus;
import restless.realms.server.web.BaseController;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/adventure/**")
public class AdventureController extends BaseController {
    @Autowired
    private AccountDao accountDao;
    
    @Autowired
    private PlayerCharacterDao playerCharacterDao;

    @Autowired
    private InventoryDao inventoryDao;

    @Autowired
    private AdventureDao adventureDao;
    
    @Autowired
    private AdventureFactory adventureFactory;
    
    @Autowired
    private StatisticsDao statisticsDao;
    
    @RequestMapping("start") 
    public ModelAndView start(HttpServletRequest request, @RequestParam("type") String type) {
        Map<String,Object> model = new LinkedHashMap<String, Object>();
        Session session = RequestUtils.getSession(request);

        PlayerCharacter playerCharacter = playerCharacterDao.get(session);
        Adventure activeAdventure = null;
        if(playerCharacter.getActiveAdventureId() != null) {
            activeAdventure = adventureDao.getAdventure(playerCharacter.getActiveAdventureId());
        }
        ResponseStatus status = null;
        if(activeAdventure != null) {
            status = ResponseStatus.ERROR;
            model.put("code", "AlreadyOnAdventure");
            model.put("message", "You are already on an adventure.");
            model.put("adventureId", activeAdventure.getId());
            model.put("attemptedType", type);
        } else {
            status = ResponseStatus.OK;

            Adventure adventure = adventureDao.startAdventure(session, type);
            model.put("adventure", getClientRepresentation(adventure, true));

            Room activeRoom = adventure.getRooms().get(adventure.getActiveRoomIndex());
            model.put("activeRoom", getClientRepresentation(activeRoom));

            int adventurePoints = accountDao.get(session.getAccountId()).getAdventurePoints();
            model.put("remainingAdventurePoints", adventurePoints);
        }
        if(!"tutorial".equals(type)) {
            statisticsDao.increment(session.getCharacterName(), "adventure-start");
        }
        return JsonUtils.getModelAndView(status, model);
    }
    
    @RequestMapping("resume")
    public ModelAndView resume(HttpServletRequest request) {
        Map<String,Object> model = new LinkedHashMap<String, Object>();
        Session session = RequestUtils.getSession(request);

        Adventure adventure = adventureDao.resumeAdventure(session.getCharacterName());
        if(adventure == null) {
            throw new IllegalStateException("No active adventure.");
        }
        model.put("adventure", getClientRepresentation(adventure, true));
        
        Room activeRoom = adventure.getRooms().get(adventure.getActiveRoomIndex());
        model.put("activeRoom", getClientRepresentation(activeRoom));
        
        int adventurePoints = accountDao.get(session.getAccountId()).getAdventurePoints();
        model.put("remainingAdventurePoints", adventurePoints);

        return JsonUtils.getModelAndView(model);
    }

    @RequestMapping("abandon")
    public ModelAndView abandon(HttpServletRequest request) {
        Session session = RequestUtils.getSession(request);
        PlayerCharacter pc = adventureDao.abandonAdventure(session.getCharacterName());
        statisticsDao.increment(session.getCharacterName(), "adventure-abandon");
        return JsonUtils.getModelAndView(ResponseStatus.OK, pc);
    }

    @RequestMapping("complete")
    public ModelAndView complete(HttpServletRequest request) {
        Session session = RequestUtils.getSession(request);
        adventureDao.completeAdventure(session.getCharacterName());
        PlayerCharacter playerCharacter = playerCharacterDao.get(session);
        Map<String, Object> ret = playerCharacter.getClientRepresentation();
        ret.put("xp", playerCharacter.getXp());

        ret.put("currency", inventoryDao.getCurrency(playerCharacter.getName()));
        ret.put("tokens", inventoryDao.getTokens(playerCharacter.getName()));
        statisticsDao.increment(session.getCharacterName(), "adventure-complete");
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("move")
    public ModelAndView move(HttpServletRequest request, @RequestParam("roomIndex") Integer roomIndex) {
        Session session = RequestUtils.getSession(request);
        PlayerCharacter playerCharacter = playerCharacterDao.get(session);
        Room[] room = adventureDao.move(playerCharacter.getActiveAdventureId(), roomIndex);
        Map<String, Object> ret = getClientRepresentation(room[1]);
        ret.put("previousRoomState", room[0].getState());
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("action")
    public ModelAndView action(
            HttpServletRequest request, 
            @RequestParam(value="type") String type, 
            @RequestParam(value="id") Integer id
    ) {
        Session session = RequestUtils.getSession(request);
        
        CombatAction combatAction = new CombatAction(null, 0, 0);
        combatAction.setActionType("skill".equals(type) ? 's' : "item".equals(type) ? 'i' : '?');
        combatAction.setActionId(id);
        
        List<EffectResult> effects = adventureDao.onAction(session, combatAction);

        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("effects", effects);
        ret.put("player", playerCharacterDao.get(session).getClientRepresentation());
        return JsonUtils.getModelAndView(ret);
    }
    
    private Map<String,Object> getClientRepresentation(Adventure adventure, boolean includeRooms) {
        Map<String,Object> ret = new LinkedHashMap<String, Object>();
        
        ret.put("id", adventure.getId());
        ret.put("status", adventure.getStatus());
        ret.put("type", adventure.getType());
        ret.put("activeRoomIndex", adventure.getActiveRoomIndex());
        if(includeRooms) {
            AdventureMap map = adventureFactory.getMap(adventure);
            List<Map<String, Object>> rooms = new ArrayList<Map<String,Object>>();
            for(int i = 0; i < map.getLocations().size(); i++) {
                AdventureMapLocation location = map.getLocations().get(i);
                
                Map<String, Object> r = new LinkedHashMap<String, Object>();
                r.put("x", location.getX());
                r.put("y", location.getY());

                if(i < adventure.getRooms().size()) {
                    Room room = adventure.getRooms().get(i);
                    if(!RoomState.NEW.equals(room.getState())) {
                        r.put("type", room.getType());
                    }
                    r.put("state", room.getState());
                } else {
                    r.put("state", RoomState.NEW);
                }
                rooms.add(r);
            }
            ret.put("rooms", rooms );
        }
        return ret;
    }

    private Map<String,Object> getClientRepresentation(Room room) {
        Map<String,Object> ret = new LinkedHashMap<String, Object>();
        ret.put("index", room.getRoomIndex());
        ret.put("type", room.getType());
        ret.put("state", room.getState());
        return ret;
    }
}