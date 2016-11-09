package restless.realms.server.achievement;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.session.Session;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/achievement/**")
public class AchievementController {
    @Autowired
    private AchievementDao achievementDao;

    @RequestMapping("list")
    public ModelAndView list(HttpServletRequest request) {
        Session s = RequestUtils.getSession(request);

        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("totalPossibleScore", achievementDao.getTotalPossibleScore());
        ret.put("achievements", achievementDao.getPlayerAchievements(s.getCharacterName()));
        
        return JsonUtils.getModelAndView(ret);
    }
}
