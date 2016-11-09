package restless.realms.server.quest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.quest.progress.QuestProgress;
import restless.realms.server.quest.progress.QuestProgressDao;
import restless.realms.server.session.Session;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.JsonUtils.ResponseStatus;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/quest/**")
public class QuestController {
    @Autowired
    private PlayerCharacterDao playerCharacterDao;
    
    @Autowired
    private QuestDao questDao;
    
    @Autowired
    private QuestProgressDao questProgressDao;
    
    @Autowired
    private StatisticsDao statisticsDao;
    
    @RequestMapping("current")
    public ModelAndView current(HttpServletRequest request) {
        Session s = RequestUtils.getSession(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        List<QuestProgress> questProgresses = questProgressDao.getActiveQuests(s.getCharacterName());
        
        List<Quest> quests = new ArrayList<Quest>(questProgresses.size());
        for(QuestProgress questProgress : questProgresses) {
            Quest quest = questDao.get(questProgress.getQuest());
            quests.add(quest);
        }

        Collections.sort(quests, new Comparator<Quest>() {
            @Override
            public int compare(Quest o1, Quest o2) {
                int ret = o1.getQualificationLevel() - o2.getQualificationLevel();
                if(ret == 0) {
                    ret = o1.getName().compareTo(o2.getName());
                }
                return ret;
            }
        });

        List<Map<String, Object>> questSummaries = new ArrayList<Map<String,Object>>();        
        for(Quest quest : quests) {
            Map<String, Object> questSummary = getQuestSummary(quest);
            QuestProgress questProgress = null;
            for(QuestProgress progress : questProgresses) {
                if(progress.getQuest().equals(quest.getId())) {
                    questProgress = progress;
                    break;
                }
            }
            questSummary.put("currentProgress", questProgress.getCurrentProgress());
            questSummary.put("completionQuantity", quest.getCompletionQuantity());
            questSummaries.add(questSummary);
        }
        ret.put("quests", questSummaries);
        return JsonUtils.getModelAndView(ret);
    }    

    @RequestMapping("available")
    public ModelAndView available(HttpServletRequest request) {
        Session s = RequestUtils.getSession(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        List<Quest> quests = questDao.getAvailableQuests(s.getCharacterName());
        List<Map<String, Object>> questSummaries = new ArrayList<Map<String,Object>>();
        for(Quest quest : quests) {
            questSummaries.add(getQuestSummary(quest));
        }
        ret.put("quests", questSummaries);
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("details")
    public ModelAndView details(HttpServletRequest request, @RequestParam("id") String id) {
        Session s = RequestUtils.getSession(request);
        PlayerCharacter pc = playerCharacterDao.get(s);
        QuestDetails details = questDao.getDetails(id, pc);
        return JsonUtils.getModelAndView(ResponseStatus.OK, details);
    }

    @RequestMapping("accept")
    public ModelAndView accept(HttpServletRequest request, @RequestParam("id") String id) {
        Session session = RequestUtils.getSession(request);
        QuestProgress progress = questProgressDao.accept(session.getCharacterName(), id);
        statisticsDao.increment(session.getCharacterName(), "quest-accept");
        
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        Quest quest = questDao.get(progress.getQuest());
        ret.put("id", quest.getId());
        ret.put("name", quest.getName());
        return JsonUtils.getModelAndView(ResponseStatus.OK, ret);
    }

    @RequestMapping("acceptall")
    public ModelAndView acceptall(HttpServletRequest request) {
        Session s = RequestUtils.getSession(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        List<Quest> quests = questDao.getAvailableQuests(s.getCharacterName());
        List<Map<String, Object>> acceptedQuestNames = new ArrayList<Map<String, Object>>();
        for(Quest quest : quests) {
            questProgressDao.accept(s.getCharacterName(), quest.getId());
            statisticsDao.increment(s.getCharacterName(), "quest-accept");
            Map<String, Object> questRepresentation = new HashMap<String, Object>(2);
            questRepresentation.put("id", quest.getId());
            questRepresentation.put("name", quest.getName());
            acceptedQuestNames.add(questRepresentation);
        }
        ret.put("accepted", acceptedQuestNames);
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("complete")
    public ModelAndView complete(HttpServletRequest request, @RequestParam("id") String id) {
        Session session = RequestUtils.getSession(request);
        Map<String, Object> result = questProgressDao.complete(session.getCharacterName(), id);
        statisticsDao.increment(session.getCharacterName(), "quest-complete");
        return JsonUtils.getModelAndView(result);
    }

    @RequestMapping("abandon")
    public ModelAndView abandon(HttpServletRequest request, @RequestParam("id") String id) {
        Session session = RequestUtils.getSession(request);
        Map<String, Object> result = questProgressDao.abandon(session.getCharacterName(), id);
        statisticsDao.increment(session.getCharacterName(), "quest-abandon");
        return JsonUtils.getModelAndView(result);
    }

    private Map<String, Object> getQuestSummary(Quest quest) {
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("id", quest.getId());
        ret.put("name", quest.getName());
        ret.put("suggestedLevel", quest.getSuggestedLevel());
        return ret;
    }    
}
