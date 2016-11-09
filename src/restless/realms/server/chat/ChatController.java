package restless.realms.server.chat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.messaging.MessageType;
import restless.realms.server.messaging.MessagingUtils;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.session.Session;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.Version;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/chat/**")
public class ChatController {
    private static final Log log = LogFactory.getLog(ChatController.class);
    
    @Autowired
    private ChatMessageDao chatMessageDao;
    
    @Autowired
    private PlayerCharacterDao playerCharacterDao;

    public ChatController() {
    }
    
    @RequestMapping("list")
    public ModelAndView list(HttpServletRequest request, @RequestParam(value="latestKnownIndex", required=false) Integer latestKnownIndex) {
        RequestUtils.getSession(request);
        if(latestKnownIndex == null) {
            latestKnownIndex = 0;
        }
        List<ChatMessage> messages = chatMessageDao.list(latestKnownIndex);
        Map<String, Object> ret = null;
        ret = new HashMap<String, Object>();
        if(messages != null && messages.size() > 0) {
            ret.put("messages", messages);
        }
        ret.put("version", Version.VERSION);
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("post")
    public ModelAndView post(HttpServletRequest request, @RequestParam("channel") String channel, @RequestParam("message") String message) {
        if("Global".equals(channel) || "Party".equals(channel) || "Local".equals(channel)) {
            Session session = RequestUtils.getSession(request);
            String characterName = session.getCharacterName();
            chatMessageDao.post(characterName, channel, message);
            return JsonUtils.getModelAndView(null);
        } else {
            throw new IllegalArgumentException("Invalid channel \"" + channel + "\"");
        }
    }
    
    @RequestMapping("who")
    public ModelAndView who(HttpServletRequest request, @RequestParam(value="level", required=false) Integer level) {
        Map<String, Object> ret = new HashMap<String, Object>(1);
        if(level == null) {
            Set<String> activePlayers = MessagingUtils.getActivePlayers();
            ret.put("players", activePlayers);
            ret.put("status",  activePlayers.size() + " players online:");
        } else {
            List<String> names = playerCharacterDao.getNamesByLevel(level);
            int totalNames = names.size();
            if(names.size() > 50) {
                names = names.subList(0, 50);
            }
            ret.put("players", names);
            ret.put("status",  totalNames  + " level " + level + " players:");
        }
        return JsonUtils.getModelAndView(ret);
    }
    
    @RequestMapping("tell")
    public ModelAndView tell(HttpServletRequest request, @RequestParam("name") String name, @RequestParam("message") String message) {
        String author = RequestUtils.getSession(request).getCharacterName();
        ChatMessage chatMessage = new ChatMessage(0, "Tell", author, message, new Date());
        name = playerCharacterDao.getProperName(name);
        log.debug("Whisper from \"" + author + "\" to \"" + name + "\": " + message);
        MessagingUtils.send(name, MessageType.CHAT, chatMessage);
        return JsonUtils.getModelAndView(null);
    }
}
