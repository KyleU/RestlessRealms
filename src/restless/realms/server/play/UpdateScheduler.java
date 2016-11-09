package restless.realms.server.play;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import restless.realms.server.messaging.MessageType;
import restless.realms.server.messaging.MessagingUtils;
import restless.realms.server.tip.TipDao;
import restless.realms.server.util.Version;

@Service
public class UpdateScheduler {
    @Autowired
    private TipDao tipDao;
    
    public void fire() {
        Map<String, Object> msg = new LinkedHashMap<String, Object>();
        msg.put("version", Version.VERSION);
        msg.put("tip", tipDao.get().getContent());
        MessagingUtils.broadcast(MessageType.UPDATE, msg);
    }
}
