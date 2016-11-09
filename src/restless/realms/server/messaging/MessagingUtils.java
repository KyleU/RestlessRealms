package restless.realms.server.messaging;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;

import restless.realms.server.util.JsonUtils;

public class MessagingUtils {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(MessagingUtils.class);
    
    private static final Map<String, AtmosphereResource<HttpServletRequest, HttpServletResponse>> userChannels = new ConcurrentHashMap<String, AtmosphereResource<HttpServletRequest,HttpServletResponse>>();

    public static void addResource(String playerName, AtmosphereResource<HttpServletRequest, HttpServletResponse> resource) {
        AtmosphereResource<HttpServletRequest, HttpServletResponse> old = userChannels.put(playerName, resource);
        if(old != null) {
            Broadcaster b = BroadcasterFactory.getDefault().lookup(Broadcaster.class, "restless");
            
            String json = "{type:\"SIGNOUT\", message:{} }";
            b.broadcast(json, old);
            log.warn("Overwriting connection for player " + playerName + ".");
        }
    }
    
    public static void removeResource(AtmosphereResource<HttpServletRequest, HttpServletResponse> resource) {
        boolean removed = userChannels.values().remove(resource);
        if(removed) {
            log.debug("Resource removed");
        } else {
            log.warn("Untracked channel encountered.");
        }
    }
    
    public static void broadcast(MessageType type, Object message) {
        Broadcaster b = BroadcasterFactory.getDefault().lookup(Broadcaster.class, "restless");
        if(b == null) {
            log.info("No subscribers, ignoring broadcast.");
        } else {
            String json = "{type:\"" + type + "\", message:" + JsonUtils.toString(message) + "}";
            log.debug("Broadcast: " + json);
            b.broadcast(json);
        }
    }
    
    public static Set<String> getActivePlayers() {
        return Collections.unmodifiableSet(userChannels.keySet());
    }

    public static void send(String name, MessageType type, Object message) {
        AtmosphereResource<HttpServletRequest, HttpServletResponse> resource = userChannels.get(name);
        if(resource == null) {
            //throw new IllegalArgumentException(name + " is not logged in right now.");
        } else {
            log.debug("Sending message \"" + message + "\" to player \"" + name + "\".");
            Broadcaster b = BroadcasterFactory.getDefault().lookup(Broadcaster.class, "restless");
            String json = "{type:\"" + type + "\", message:" + JsonUtils.toString(message) + "}";
            b.broadcast(json, resource);
        }
    }
}
