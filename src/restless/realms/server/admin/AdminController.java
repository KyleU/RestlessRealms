package restless.realms.server.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.account.Account;
import restless.realms.server.account.AccountDao;
import restless.realms.server.chat.ChatMessage;
import restless.realms.server.chat.ChatMessageDao;
import restless.realms.server.database.seed.SeedDataValidator;
import restless.realms.server.database.seed.merge.MergeDifference;
import restless.realms.server.database.seed.merge.MergeManager;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.item.Item;
import restless.realms.server.mob.MobArchetype;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.session.Session;
import restless.realms.server.skill.Skill;
import restless.realms.server.skill.SkillDao;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.JsonUtils.ResponseStatus;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/admin/**")
public class AdminController {
    @Autowired
    private AdminDao adminDao;
    
    @Autowired
    private AccountDao accountDao;

    @Autowired
    private PlayerCharacterDao playerCharacterDao;

    @Autowired
    private InventoryDao inventoryDao;

    @Autowired
    private SkillDao skillDao;

    @Autowired
    private HibernateTemplate template;
    
    @Autowired
    private MergeManager mergeManager;
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private ChatMessageDao chatMessageDao;
    
    @RequestMapping("list")
    public ModelAndView list(HttpServletRequest request) {
        checkRequest(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("stats", "Awesome Stats");
        ret.put("validate", "Validate Seed Data");
        ret.put("diff", "Show Differences");
        ret.put("merge", "MERGE!");
        ret.put("chats", "Chat List");
        ret.put("datastore", "Datastore Stats");
        ret.put("cache", "Cache Stats");
        ret.put("entity", "Entity Stats");
        ret.put("collection", "Collection Stats");
        ret.put("query", "Query Stats");
        ret.put("accounts", "Account List");
        ret.put("players", "Player List");
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("sandbox")
    public ModelAndView sandbox(HttpServletRequest request) {
        checkRequest(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        List<?> result = template.find("select x from Adventure a join a.rooms x where index(x) = 0");
        ret.put("result", result);
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("give")
    public ModelAndView give(HttpServletRequest request, 
            @RequestParam(value="item", required=false) Integer item,
            @RequestParam(value="skill", required=false) Integer skill
    )  {
        checkRequest(request);
        Session s = RequestUtils.getSession(request);
        if(item != null && item > 0) {
            inventoryDao.give(s.getCharacterName(), item);
        }
        if(skill != null && skill > 0) {
            skillDao.give(s.getCharacterName(), skill);
        }
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        List<?> result = template.find("select x from Adventure a join a.rooms x where index(x) = 0");
        ret.put("result", result);
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("stats")
    public ModelAndView stats(HttpServletRequest request, @RequestParam(value="name", required=false) String name) {
        checkRequest(request);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        if(name == null || name.length() == 0) {
            result = adminDao.stats();
        } else {
            result = adminDao.stats(name);
        }
        
        return JsonUtils.getModelAndView(result);
    }
    
    @RequestMapping("accounts")
    public ModelAndView listAccounts(HttpServletRequest request) {
        checkRequest(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        List<?> result = template.find("select a from Account a");
        ret.put("result", result);
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("zap")
    public ModelAndView zap(HttpServletRequest request, @RequestParam("id") int id) {
        checkRequest(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ChatMessage chat = template.get(ChatMessage.class, id);
        template.delete(chat);
        return JsonUtils.getModelAndView(ret);
    } 
    
    @RequestMapping("chat")
    public ModelAndView chat(HttpServletRequest request, @RequestParam("message") String message) {
        checkRequest(request);
        Session session = RequestUtils.getSession(request);
        chatMessageDao.post(session.getCharacterName(), "Admin", message);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        ret.put("status", "OK");
        return JsonUtils.getModelAndView(ret);
    } 
    
    @RequestMapping("data")
    public ModelAndView data(HttpServletRequest request) {
        checkRequest(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        List<?> result = template.find("select c from ChatMessage c");
        ret.put("result", result);
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("players")
    public ModelAndView listPlayers(HttpServletRequest request) {
        checkRequest(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        List<?> result = template.find("select pc from PlayerCharacter pc");
        ret.put("result", result);
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("datastore")
    public ModelAndView datastore(HttpServletRequest request) {
        checkRequest(request);
        Statistics stats = template.getSessionFactory().getStatistics();
        return JsonUtils.getModelAndView(ResponseStatus.OK, stats);
    }
    
    @RequestMapping("cache")
    public ModelAndView cache(HttpServletRequest request) {
        checkRequest(request);
        Map<String, Object> ret = new HashMap<String, Object>();
        List<SecondLevelCacheStatistics> cacheStats = new ArrayList<SecondLevelCacheStatistics>();
        Statistics stats = template.getSessionFactory().getStatistics();

        for(int i = 0; i < stats.getSecondLevelCacheRegionNames().length; i++) {
            String regionName = stats.getSecondLevelCacheRegionNames()[i];
            SecondLevelCacheStatistics statistics = stats.getSecondLevelCacheStatistics(regionName);
            cacheStats.add(statistics);
        }
        ret.put("Cache", cacheStats);
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("entity")
    public ModelAndView entity(HttpServletRequest request) {
        checkRequest(request);
        Map<String, Object> ret = new HashMap<String, Object>();
        List<EntityStatistics> entityStats = new ArrayList<EntityStatistics>();
        Statistics stats = template.getSessionFactory().getStatistics();

        for(int i = 0; i < stats.getEntityNames().length; i++) {
            String entityName = stats.getEntityNames()[i];
            EntityStatistics statistics = stats.getEntityStatistics(entityName);
            entityStats.add(statistics);
        }
        ret.put("Entities", entityStats);
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("collection")
    public ModelAndView collection(HttpServletRequest request) {
        checkRequest(request);
        Map<String, Object> ret = new HashMap<String, Object>();
        List<CollectionStatistics> collectionStats = new ArrayList<CollectionStatistics>();
        Statistics stats = template.getSessionFactory().getStatistics();

        for(int i = 0; i < stats.getCollectionRoleNames().length; i++) {
            String collectionName = stats.getCollectionRoleNames()[i];
            CollectionStatistics statistics = stats.getCollectionStatistics(collectionName);
            collectionStats.add(statistics);
        }
        ret.put("Collections", collectionStats);
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("query")
    public ModelAndView query(HttpServletRequest request) {
        checkRequest(request);
        Map<String, Object> ret = new HashMap<String, Object>();
        List<QueryStatistics> queryStats = new ArrayList<QueryStatistics>();
        Statistics stats = template.getSessionFactory().getStatistics();

        for(int i = 0; i < stats.getQueries().length; i++) {
            String queryName = stats.getQueries()[i];
            QueryStatistics statistics = stats.getQueryStatistics(queryName);
            queryStats.add(statistics);
        }
        ret.put("Queries", queryStats);
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("validate")
    public ModelAndView validate(HttpServletRequest request) {
        checkRequest(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();

        try {
            SeedDataValidator validator = new SeedDataValidator();
            validator.validate();
            Map<String, Exception> exceptions = validator.getExceptions();
            for(Entry<String, Exception> e : exceptions.entrySet()) {
                ret.put(e.getKey(), e.getValue().toString());
            }
            
            Map<Class<?>, Map<Serializable, Object>> map = validator.getUberCache();
            for(Class<?> c : map.keySet()) {
                String result = map.get(c).entrySet().size() + " records validated.";
                ret.put(c.getSimpleName(), result);
            }
            ret.put("unreferencedItems", validator.getUnreferencedIds(Item.class).toString());
            ret.put("unreferencedSkills", validator.getUnreferencedIds(Skill.class).toString());
            ret.put("unreferencedMobs", validator.getUnreferencedIds(MobArchetype.class).toString());
        } catch (Exception e) {
            ret.put(e.getClass().getSimpleName(), e.getMessage());
        }
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("diff")
    public ModelAndView diff(HttpServletRequest request) {
        checkRequest(request);
        SeedDataValidator validator = new SeedDataValidator();
        validator.validate();
        if(validator.getExceptions().size() > 0) {
            throw new IllegalStateException("Error importing seed data. (" + validator.getExceptions().entrySet().iterator().next().getKey() + ")");
        }
            
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        Map<Class<?>, Map<Serializable, Object>> map = validator.getUberCache();
        List<MergeDifference> differences = mergeManager.findDifferences(map);
        List<Map<String, Object>> differenceMaps = new ArrayList<Map<String,Object>>();
        for(MergeDifference difference : differences) {
            Map<String, Object> differenceMap = new LinkedHashMap<String, Object>();
            differenceMap.put("type", difference.getType().getSimpleName());
            differenceMap.put("id", difference.getId());
            differenceMap.put("property", difference.getPropertyName());
            differenceMap.put("current", difference.getCurrentValue());
            differenceMap.put("new", difference.getNewValue());
            differenceMaps.add(differenceMap);
        }
        ret.put("differences", differenceMaps);

        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("merge")
    public ModelAndView merge(HttpServletRequest request) {
        checkRequest(request);
        SeedDataValidator validator = new SeedDataValidator();
        validator.validate();
        if(validator.getExceptions().size() > 0) {
            throw new IllegalStateException("Error importing seed data. (" + validator.getExceptions().entrySet().iterator().next().getKey() + ")");
        }

        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        Map<Class<?>, Map<Serializable, Object>> map = validator.getUberCache();
        List<MergeDifference> differences = mergeManager.findDifferences(map);
        List<Map<String, Object>> differenceMaps = new ArrayList<Map<String,Object>>();
        for(MergeDifference difference : differences) {
            Map<String, Object> differenceMap = new LinkedHashMap<String, Object>();
            differenceMap.put("type", difference.getType().getSimpleName());
            differenceMap.put("id", difference.getId());
            differenceMap.put("property", difference.getPropertyName());
            String status;
            try {
                Object newObject = map.get(difference.getType()).get(difference.getId());
                status = mergeManager.applyMerge(difference, newObject);
            } catch(Exception e) {
                status = e.getClass().getSimpleName() + ":" + e.getMessage();
            }
            differenceMap.put("status", status );
            differenceMaps.add(differenceMap);
        }
        ret.put("differences", differenceMaps);

        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("aps")
    public ModelAndView aps(HttpServletRequest request, @RequestParam("name") String name, @RequestParam(required=false, value="value") Integer value) {
        checkRequest(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        PlayerCharacter playerCharacter = playerCharacterDao.get(name);
        int accountId = playerCharacter.getAccountId();
        int oldAps = accountDao.get(accountId).getAdventurePoints();
        accountDao.setAdventurePoints(accountId, oldAps + value);
        ret.put("aps", value);
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("recent")
    public ModelAndView recent(HttpServletRequest request) {
        checkRequest(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();
        Cache cache = cacheManager.getCache("restless.realms.server.account.RecentAccounts");
        ret.put("accounts", cache.getKeys());
        cache = cacheManager.getCache("restless.realms.server.player.RecentPlayers");
        ret.put("players", cache.getKeys());
        return JsonUtils.getModelAndView(ret);
    }
    
    private Account checkRequest(HttpServletRequest request) {
        Session s = RequestUtils.getSession(request);
        Account account = accountDao.get(s.getAccountId());
        if(!account.isAdmin()) {
            throw new IllegalStateException("You are not an administrator.");
        }
        return account;
    }
}
