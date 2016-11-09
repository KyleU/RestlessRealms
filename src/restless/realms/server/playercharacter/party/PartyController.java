package restless.realms.server.playercharacter.party;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.session.Session;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.JsonUtils.ResponseStatus;
import restless.realms.server.web.BaseController;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/party/**")
public class PartyController extends BaseController {
    @Autowired
    private PartyDao partyDao;
    
    @RequestMapping("start") 
    public ModelAndView start(HttpServletRequest request) {
        Session session = RequestUtils.getSession(request);
        Party party = partyDao.startParty(session.getCharacterName()); 
        return JsonUtils.getModelAndView(ResponseStatus.OK, party);
    }
     
    @RequestMapping("join") 
    public ModelAndView join(HttpServletRequest request, @RequestParam("id") Integer id) {
        Session session = RequestUtils.getSession(request);
        Party party = partyDao.joinParty(session.getCharacterName(), id); 
        return JsonUtils.getModelAndView(ResponseStatus.OK, party);
    }
    
    @RequestMapping("leave") 
    public ModelAndView leave(HttpServletRequest request) {
        Session session = RequestUtils.getSession(request);
        partyDao.leaveParty(session.getCharacterName()); 
        return JsonUtils.getModelAndView(ResponseStatus.OK, null);
    }
}