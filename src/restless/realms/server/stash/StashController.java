package restless.realms.server.stash;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.session.Session;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.JsonUtils.ResponseStatus;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/stash/**")
public class StashController {
    @Autowired
    private StashDao stashDao;
    
    @RequestMapping("list")
    public ModelAndView list(HttpServletRequest request) {
        Session s = RequestUtils.getSession(request);
        
        Stash stash = stashDao.get(s.getAccountId());
        return JsonUtils.getModelAndView(ResponseStatus.OK, stash);
    }

    @RequestMapping("deposit")
    public ModelAndView deposit(HttpServletRequest request, @RequestParam("item") Integer itemId, @RequestParam("index") int index) {
        Session s = RequestUtils.getSession(request);
        stashDao.depositItem(s.getCharacterName(), itemId, index);
        return JsonUtils.getModelAndView(ResponseStatus.OK, null);
    }

    @RequestMapping("withdraw")
    public ModelAndView withdraw(HttpServletRequest request, @RequestParam("item") Integer itemId, @RequestParam("index") int index) {
        Session s = RequestUtils.getSession(request);
        stashDao.withdrawItem(s.getCharacterName(), itemId, index);
        return JsonUtils.getModelAndView(ResponseStatus.OK, null);
    }

    @RequestMapping("depositgold")
    public ModelAndView depositgold(HttpServletRequest request, @RequestParam("gold") int gold) {
        Session s = RequestUtils.getSession(request);
        stashDao.depositCurrency(s.getCharacterName(), gold);
        return JsonUtils.getModelAndView(ResponseStatus.OK, null);
    }

    @RequestMapping("withdrawgold")
    public ModelAndView withdrawgold(HttpServletRequest request, @RequestParam("gold") int gold) {
        Session s = RequestUtils.getSession(request);
        stashDao.withdrawCurrency(s.getCharacterName(), gold);
        return JsonUtils.getModelAndView(ResponseStatus.OK, null);
    }
}