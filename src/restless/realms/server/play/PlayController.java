package restless.realms.server.play;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.account.Account;
import restless.realms.server.account.AccountDao;
import restless.realms.server.account.AdventurePointsScheduler;
import restless.realms.server.adventure.Adventure;
import restless.realms.server.adventure.AdventureArchetype;
import restless.realms.server.adventure.AdventureDao;
import restless.realms.server.adventure.AdventureFactory;
import restless.realms.server.equipment.Equipment;
import restless.realms.server.equipment.EquipmentDao;
import restless.realms.server.exception.NotSignedInException;
import restless.realms.server.facebook.FacebookUtils;
import restless.realms.server.inventory.Inventory;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.mail.MailDao;
import restless.realms.server.perk.Perk;
import restless.realms.server.perk.PerkDao;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.profession.Profession;
import restless.realms.server.profession.ProfessionDao;
import restless.realms.server.session.Session;
import restless.realms.server.session.SessionDao;
import restless.realms.server.skill.Skill;
import restless.realms.server.skill.SkillDao;
import restless.realms.server.tip.Tip;
import restless.realms.server.tip.TipDao;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/play/**")
public class PlayController {
    private static final Log log = LogFactory.getLog(PlayController.class);
    
    @Autowired
    private SessionDao sessionDao;
    
    @Autowired
    private AccountDao accountDao;

    @Autowired
    private PlayerCharacterDao playerDao;

    @Autowired
    private InventoryDao inventoryDao;
    
    @Autowired
    private EquipmentDao equipmentDao;

    @Autowired
    private SkillDao skillDao;

    @Autowired
    private PerkDao perkDao;

    @Autowired
    private AdventureDao adventureDao;
    
    @Autowired
    private AdventureFactory adventureFactory;

    @Autowired
    private ProfessionDao professionDao;

    @Autowired
    private MailDao mailDao;
    
    @Autowired
    private TipDao tipDao;
    
    @Autowired
    StatisticsDao statisticsDao;

    public PlayController() {
		
	}
	
    @RequestMapping("play")
    public ModelAndView play(HttpServletRequest request, HttpServletResponse response) {
        Session session = RequestUtils.getSession(request);

        PlayerCharacter playerCharacter = playerDao.get(session.getCharacterName());
        if(playerCharacter == null) {
            String message = "Invalid character name \"" + session.getCharacterName() + "\".";
            log.error(message);
            throw new NotSignedInException(message);
        }

        Account account = accountDao.get(session.getAccountId());
        if(account == null) {
            sessionDao.signout(session.getId());
            String message = "Invalid account \"" + session.getAccountId() + "\" associated with session \"" + session.getId() + "\".";
            log.error(message);
            throw new NotSignedInException(message);
        }

        if(account.getProvider().equals("Facebook")) {
            Long userId = FacebookUtils.getFacebookConnectUserId(request);
            if(userId != null) {
                Object userIdString = "" + userId;
                if(!account.getIdentifier().equals(userIdString)) {
                    sessionDao.signout(session.getId());
                    String message = "Different facebook account associated to session.";
                    log.warn(message);
                    throw new NotSignedInException(message);
                }
            } else {
                log.error("Unable to read Facebook UID for session " + session.getId() + ".");
            }
        } else {
            
        }
        
        if(!session.getAccountId().equals(account.getId())) {
            String message = "Attempt by session " + session.getId() + " to sign in with someone else's player, \"" + playerCharacter.getName() + "\".";
            log.error(message);
            sessionDao.signout(session.getId());
            throw new NotSignedInException(message);
        }
        
        Map<String, Object> gameState = new LinkedHashMap<String, Object>();

        Map<String, Object> accountDetails = new LinkedHashMap<String, Object>();
        accountDetails.put("accountId", account.getId());
        accountDetails.put("administrator", account.isAdmin());
        accountDetails.put("adventurePoints", account.getAdventurePoints());
        accountDetails.put("provider", account.getProvider());
        
        gameState.put("accountDetails", accountDetails);
        
        Profession profession = professionDao.getProfession(playerCharacter.getProfession());
        gameState.put("profession", getClientRepresentation(profession));
        
        Adventure activeAdventure = null;
        Integer activeAdventureId = playerCharacter.getActiveAdventureId();
        if(activeAdventureId != null) {
            gameState.put("adventure", activeAdventureId);
            if(activeAdventureId > 0) {
                activeAdventure = adventureDao.getAdventure(activeAdventureId);
            }
        }
        if(activeAdventure != null) {
            gameState.put("adventureStatus", activeAdventure.getStatus());
        }
        
        Inventory i = inventoryDao.get(playerCharacter.getName());
        gameState.put("currency", i.getCurrency());
        gameState.put("tokens", i.getTokens());
        gameState.put("inventory", i.getItems());

        Equipment e = equipmentDao.get(playerCharacter.getName());
        gameState.put("equipment", e);

        List<Skill> skills = skillDao.getSkillset(playerCharacter.getName());
        gameState.put("skills", skills);

        List<Perk> perks = perkDao.getPerkset(playerCharacter.getName());
        gameState.put("perks", perks);

        boolean firstTime = (playerCharacter.getLastSignedIn() == null);
        gameState.put("showHelp", firstTime);
        
        Collection<AdventureArchetype> archetypes = adventureFactory.getAdventureArchetypes().values();
        gameState.put("adventures", archetypes);

        PlayerCharacter pc = playerDao.updateSignedIn(playerCharacter.getName());
        gameState.put("player", pc);

        Date next = AdventurePointsScheduler.getNext();
        Date now = new Date();
        gameState.put("nextAdventurePointSeconds", ((next.getTime() - now.getTime()) / 1000) + 30);
        
        gameState.put("numMessages", mailDao.getCount(playerCharacter.getName()));

        Tip tipText = tipDao.get();
        gameState.put("tip", tipText.getContent());

        statisticsDao.increment(playerCharacter.getName(), "play");
        
        return JsonUtils.getModelAndView(gameState);
    }

    private Map<String, Object> getClientRepresentation(Profession p) {
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("id", p.getId());
        ret.put("name", p.getName());
        ret.put("hitpointsPerLevel", p.getHitpointsPerLevel());
        ret.put("manaPerLevel", p.getManaPerLevel());
        return ret;
    }
}
