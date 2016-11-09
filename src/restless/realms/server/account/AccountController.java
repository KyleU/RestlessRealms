package restless.realms.server.account;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.session.Session;
import restless.realms.server.session.SessionDao;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/account/**")
public class AccountController {
    @Autowired
    private SessionDao sessionDao;
    
    @Autowired
    private AccountDao accountDao;
    
    @Autowired 
    private InventoryDao inventoryDao;
       
    @RequestMapping("signout")
    public ModelAndView signout(HttpServletRequest request, HttpServletResponse response) {
        Session s = (Session)request.getAttribute("rrsession");
        if(s != null) {
            sessionDao.signout(s.getId());
        }
        
        return JsonUtils.getModelAndView(null);
    }

    @RequestMapping("characterselect")
    public ModelAndView characterselect(HttpServletRequest request) {
        Session s = RequestUtils.getSession(request);
        sessionDao.setCharacterName(s.getId(), null);
        return JsonUtils.getModelAndView(null);
    }

    @RequestMapping("aps")
    public ModelAndView aps(HttpServletRequest request) {
        Session s = RequestUtils.getSession(request);

        Map<String, Object> ret = new LinkedHashMap<String, Object>();

        Account account = accountDao.get(s.getAccountId());
        ret.put("aps", account.getAdventurePoints());
        
        Date now = new Date();
        Date next = AdventurePointsScheduler.getNext();
        ret.put("nextadventurepointseconds", (next.getTime() - now.getTime()) / 1000);
        
        int currency = inventoryDao.getCurrency(s.getCharacterName());
        ret.put("currency", currency);

        return JsonUtils.getModelAndView(ret);
    }
}
