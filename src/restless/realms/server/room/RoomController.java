package restless.realms.server.room;

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

import restless.realms.server.inventory.Inventory;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.item.Item;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.session.Session;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.JsonUtils.ResponseStatus;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/room/**")
public class RoomController {
    @Autowired
    private PlayerCharacterDao playerCharacterDao;
    
    @Autowired
    private InventoryDao inventoryDao;
    
    @Autowired
    private RoomDao roomDao;
    
    @RequestMapping("contents")
    public ModelAndView contents(HttpServletRequest request) {
        Session session = RequestUtils.getSession(request);
        PlayerCharacter playerCharacter = playerCharacterDao.get(session);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        List<Item> contents = roomDao.getContents(playerCharacter.getActiveAdventureId());
        ret.put("contents", contents);
        ret.put("playerCurrency", inventoryDao.getCurrency(playerCharacter.getName()));
        ret.put("playerTokens", inventoryDao.getTokens(playerCharacter.getName()));
        ret.put("playerXp", playerCharacter.getXp());
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("lootitem")
    public ModelAndView lootitem(HttpServletRequest request, @RequestParam(value="itemId") int itemId, @RequestParam(value="itemIndex") int itemIndex) {
        Session session = RequestUtils.getSession(request);
        PlayerCharacter playerCharacter = playerCharacterDao.get(session);

        Inventory inv = roomDao.loot(playerCharacter.getName(), playerCharacter.getActiveAdventureId(), itemId, itemIndex);
        List<Item> contents = roomDao.getContents(playerCharacter.getActiveAdventureId());

        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("playerItems", inv.getItems());
        ret.put("roomContents", contents);
        return JsonUtils.getModelAndView(ResponseStatus.OK, ret);
    }

    @RequestMapping("lootall")
    public ModelAndView lootall(HttpServletRequest request) {
        Session session = RequestUtils.getSession(request);
        PlayerCharacter playerCharacter = playerCharacterDao.get(session);

        Inventory inv = roomDao.lootall(playerCharacter.getName(), playerCharacter.getActiveAdventureId());

        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("playerItems", inv.getItems());
        ret.put("roomContents", new ArrayList<Integer>());
        return JsonUtils.getModelAndView(ResponseStatus.OK, ret);
    }

    @RequestMapping("solvepuzzle")
    public ModelAndView solvepuzzle(HttpServletRequest request, @RequestParam("solution") String solution) {
        Session session = RequestUtils.getSession(request);
        PlayerCharacter playerCharacter = playerCharacterDao.get(session);

        roomDao.solvePuzzle(playerCharacter.getActiveAdventureId(), solution);
        return JsonUtils.getModelAndView(null);
    }

    @RequestMapping("shrine")
    public ModelAndView shrine(HttpServletRequest request) {
        Session session = RequestUtils.getSession(request);

        PlayerCharacter playerCharacter = playerCharacterDao.get(session);
        roomDao.shrine(playerCharacter.getActiveAdventureId());
        playerCharacter = playerCharacterDao.get(session);
        
        return JsonUtils.getModelAndView(playerCharacter.getClientRepresentation());
    }
}
