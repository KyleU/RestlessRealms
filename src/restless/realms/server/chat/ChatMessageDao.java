package restless.realms.server.chat;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.messaging.MessageType;
import restless.realms.server.messaging.MessagingUtils;
import restless.realms.server.playercharacter.statistics.StatisticsDao;

@Repository
public class ChatMessageDao extends AbstractDao<ChatMessage> {
    private static final int ROW_LIMIT = 20;
    
    private HibernateTemplate rowLimitedTemplate;

    @Autowired
    StatisticsDao statisticsDao;
    
	@SuppressWarnings("unchecked")
    public List<ChatMessage> list(int latestKnownIndex) {
        if(rowLimitedTemplate == null) {
            rowLimitedTemplate = new HibernateTemplate(template.getSessionFactory());
            rowLimitedTemplate.setMaxResults(ROW_LIMIT);
        }

	    Date aWhileAgo = new Date();
	    aWhileAgo.setTime(aWhileAgo.getTime() - (1000 * 60 * 60 * 3));
	    List<ChatMessage> messages = rowLimitedTemplate.findByNamedQuery("chatMessage.get", latestKnownIndex, aWhileAgo);
	    Collections.reverse(messages);
		return messages;
	}

	@Transactional(isolation=Isolation.REPEATABLE_READ)
	public void post(String characterName, String channel, String message) {
	    ChatMessage chatMessage = new ChatMessage(null, channel, characterName, message, new Date());
	    template.save(chatMessage);
	    statisticsDao.increment(characterName, "chat-" + channel);
	    MessagingUtils.broadcast(MessageType.CHAT, chatMessage);
	}

    @Override
    protected Class<?> getManagedClass() {
        return ChatMessage.class;
    };
}
