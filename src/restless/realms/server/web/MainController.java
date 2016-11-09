package restless.realms.server.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.CookieGenerator;

import restless.realms.server.account.Account;
import restless.realms.server.account.AccountDao;
import restless.realms.server.exception.NotSignedInException;
import restless.realms.server.facebook.FacebookUtils;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.session.Session;
import restless.realms.server.session.SessionDao;

import com.google.code.facebookapi.FacebookJsonRestClient;

@Controller
public class MainController {
    private static final Log log = LogFactory.getLog(MainController.class);
    
    @Autowired
    private SessionDao sessionDao;
    
    @Autowired
    private AccountDao accountDao;
    
    @Autowired
    private PlayerCharacterDao playerCharacterDao;
    
    @Autowired
    private CookieGenerator cookieGenerator;

    public MainController() {
    }
    
    @RequestMapping("/index")
    public ModelAndView index(
            HttpServletRequest request, 
            HttpServletResponse response, 
            @RequestParam(required=false, value="activate") String activate,
            @RequestParam(required=false, value="create") String create,
            @RequestParam(required=false, value="error") String error
    ) {
        ModelAndView ret = new ModelAndView("splash");
        String state = null;
        
        String sessionId = RequestUtils.getCookieValue("rrsession", request);
        Session session = null;
        
        if(sessionId != null && sessionId.length() > 0) {
            session = sessionDao.get(sessionId);
            if(session != null && !session.isActive()) {
                //inactive session
                session = null;
            }
        }
        
        Long facebookUserId = FacebookUtils.getFacebookConnectUserId(request);
        Account account = null;
        if(facebookUserId != null) {
            account = accountDao.getByIdentifier("" + facebookUserId);
            if(session != null && !session.getAccountId().equals(account.getId())) {
                session = null;
            }
            if(session == null) {
                if(account == null) {
                    log.info("Creating account for facebookUserId " + facebookUserId);
                    FacebookJsonRestClient client = FacebookUtils.getFacebookConnectClient(request);
                    if(client == null) {
                        throw new NotSignedInException("You are not connected with Facebook.");
                    }
                    Map<String, Object> clientInfo = FacebookUtils.getUserInfo(client, facebookUserId);
                    account = accountDao.createAccount(clientInfo);
                }
                session = sessionDao.create(account.getId());
                cookieGenerator.addCookie(response, session.getId());
            }
            if(!account.isEnabled()) {
                throw new IllegalAccessError("Unable to sign in with this account.");
            }
        }

        if(session == null) {
            state = "signin";
        } else {
            if(activate != null) {
                PlayerCharacter playerCharacter = playerCharacterDao.get(activate);
                if(playerCharacter == null) {
                    throw new IllegalArgumentException("That character does not exist.");
                }
                if(!playerCharacter.getAccountId().equals(session.getAccountId())) {
                    throw new IllegalArgumentException("That's not your character.");
                }
                sessionDao.setCharacterName(session.getId(), playerCharacter.getName());
                session.setCharacterName(playerCharacter.getName());
                try {
                    log.info("Handling index request for session " + sessionId + " with state \"activate\" for player \"" + playerCharacter.getName() + ".");
//                    response.sendRedirect("play.html?gwt.codesvr=dev.restlessrealms.com:9997");
                    response.sendRedirect("play.html");
                    return null;
                } catch(IOException e) {
                    // no op
                }
            } else {            
                if(session.getCharacterName() != null) {
                    ret.addObject("activeCharacter", session.getCharacterName());
                }
                
                List<PlayerCharacter> characters = playerCharacterDao.getByAccount(session.getAccountId());
                if(create != null) {
                    state = "create";
                    ret.addObject("profession", create);
                } else {
                    state = "characters";
                    for(PlayerCharacter playerCharacter : characters) {
                        if(ret.getModelMap().containsKey(playerCharacter.getProfession())) {
                            throw new IllegalStateException("Only one character of type \"" + playerCharacter.getProfession() + "\" is allowed.");
                        }
                        ret.addObject(playerCharacter.getProfession(), playerCharacter);
                    }
                }
            }
        }
        
        ret.addObject("state", state);
        
        if(error != null && error.length() > 0) {
            ret.addObject("error", error);
        }
        
        log.info("Handling index request for session " + sessionId + " with state \"" + state + "\".");
        return ret;
    }
    
    @RequestMapping("/create")
    public String create(
            HttpServletRequest request,
            @RequestParam("name") String name,
            @RequestParam("profession") String profession,
            @RequestParam("gender") String gender
    ) {
        String sessionId = RequestUtils.getCookieValue("rrsession", request);
        Session session = sessionDao.get(sessionId);
        playerCharacterDao.create(session.getAccountId(), name, profession, gender);
        return "redirect:index.html"; 
    }

    @RequestMapping("/delete")
    public String delete(
            HttpServletRequest request,
            @RequestParam("name") String name
    ) {
        String sessionId = RequestUtils.getCookieValue("rrsession", request);
        Session session = sessionDao.get(sessionId);
        playerCharacterDao.disable(session.getAccountId(), name);
        return "redirect:index.html"; 
    }

    @RequestMapping("/play")
    public String play(HttpServletResponse response) {
        return "play";
    }

    @RequestMapping("/privacy.html")
    public String privacy(HttpServletResponse response) {
        return "privacy";
    }

    @RequestMapping("/facebook")
    public String facebook(HttpServletRequest request, HttpServletResponse response) {
        return "play";
    }
}