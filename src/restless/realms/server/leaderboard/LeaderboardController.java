package restless.realms.server.leaderboard;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.util.JsonUtils;

@Controller
@RequestMapping("/leaderboard/**")
public class LeaderboardController {
    @Autowired
    private LeaderboardDao leaderboardDao;

    public LeaderboardController() {

    }

    @RequestMapping("list")
    public ModelAndView list() {
        Map<String,Object> ret = new LinkedHashMap<String, Object>();
        for(LeaderboardType type : LeaderboardType.values()) {
            ret.put(type.toString(), type.getTitle());
        }
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("get")
    public ModelAndView get(@RequestParam("id") String id) {
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        LeaderboardType type = LeaderboardType.valueOf(id);
        ret.put("title", type.getTitle());
        ret.put("valueTitle", type.getValueTitle());
        Leaderboard leaderboard = leaderboardDao.get(type);
        ret.put("leaderboard", leaderboard);
        return JsonUtils.getModelAndView(ret);
    }
}