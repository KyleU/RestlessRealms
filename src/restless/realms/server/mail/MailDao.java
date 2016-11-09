package restless.realms.server.mail;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.inventory.Inventory;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.item.Item;
import restless.realms.server.item.ItemDao;
import restless.realms.server.item.ItemType;
import restless.realms.server.messaging.MessageType;
import restless.realms.server.messaging.MessagingUtils;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;

@Repository
public class MailDao extends AbstractDao<MailMessage> {
    private static final Log log = LogFactory.getLog(MailDao.class);
    
    @Autowired
    private PlayerCharacterDao playerCharacterDao;

    @Autowired
    private InventoryDao inventoryDao;

    @Autowired
    private ItemDao itemDao;
    
    @SuppressWarnings("unchecked")
    public List<MailMessage> getByTo(String playerName) {
        List<MailMessage> ret = template.findByNamedQuery("mailMessage.getByTo", playerName);
        return ret;
    }
    
    @SuppressWarnings("unchecked")
    public List<MailMessage> getByFrom(String playerName) {
        List<MailMessage> ret = template.findByNamedQuery("mailMessage.getByFrom", playerName);
        return ret;
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Inventory send(String from, String to, String message) {
        return send(from, to, message, null, null, null, 0, 0, 0, false);
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void broadcast(String message, Integer attachment1, Integer attachment2, Integer attachment3, int currency, int tokens, int xp) {
        Iterator<PlayerCharacter> iterator = playerCharacterDao.iterate();
        while(iterator.hasNext()) {
            PlayerCharacter playerCharacter = iterator.next();
            send("Broadcast", playerCharacter.getName(), message, attachment1, attachment2, attachment3, currency, tokens, xp, false);
        }
    }
    
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Inventory send(String from, String to, String message, Integer attachment1, Integer attachment2, Integer attachment3, int currency, int tokens, int xp, boolean removeFromPlayer) {
        PlayerCharacter fromPlayerCharacter = playerCharacterDao.get(from);
        if(to.trim().length() == 0) {
            throw new IllegalArgumentException("You must enter a player name.");
        }
        if(message.trim().length() == 0) {
            throw new IllegalArgumentException("Surely you have more to say than that. Add some text.");
        }
        to = playerCharacterDao.getProperName(to);
        PlayerCharacter toPlayerCharacter = playerCharacterDao.get(to);
        if(toPlayerCharacter == null) {
            throw new IllegalArgumentException("Player \"" + to + "\" does not exist.");
        }

        MailMessage mailMessage = new MailMessage();
        mailMessage.setFromName(fromPlayerCharacter.getName());
        mailMessage.setToName(toPlayerCharacter.getName());
        mailMessage.setContent(message);

        mailMessage.setSentTimestamp(new Date());
        
        Inventory inventory = inventoryDao.get(from);
        int numAttachments = 0;
        if(attachment1 != null && attachment1 > 0) {
            numAttachments++;
            if(removeFromPlayer) {
                remove(inventory, attachment1);
            }
            mailMessage.setAttachment1(attachment1);
        }
        if(attachment2 != null && attachment2 > 0) {
            numAttachments++;
            if(removeFromPlayer) {
                remove(inventory, attachment2);
            }
            mailMessage.setAttachment2(attachment2);
        }
        if(attachment3 != null && attachment3 > 0) {
            numAttachments++;
            if(removeFromPlayer) {
                remove(inventory, attachment3);
            }
            mailMessage.setAttachment3(attachment3);
        }
        if(removeFromPlayer) {
            if(inventory.getCurrency() < currency) {
                throw new IllegalStateException("You do not have " + currency + " to attach.");
            }
            inventory.setCurrency(inventory.getCurrency() - currency);
        }
        mailMessage.setCurrency(currency);
        mailMessage.setTokens(tokens);
        mailMessage.setXp(xp);
        
        template.save(mailMessage);
        
        log.info("Sent message from \"" + from + "\" to \"" + to + "\" with " + numAttachments + " attachments. Message: " + message);
        
        Map<String, Integer> numMessagesMap = new HashMap<String, Integer>(1);
        numMessagesMap.put("numMessages", getCount(to));
        MessagingUtils.send(to, MessageType.MESSAGES, numMessagesMap);
        
        return inventory;
    }

    @Transactional
    public MailMessage get(String characterName, int id) {
        MailMessage mailMessage = get(id);
        if(!mailMessage.getFromName().equalsIgnoreCase(characterName)) {
            if(!mailMessage.getToName().equalsIgnoreCase(characterName)) {
                throw new IllegalArgumentException("This is not your mail message.");
            }
        }
        if(mailMessage.getToName().equalsIgnoreCase(characterName)) {
            if(mailMessage.getReadTimestamp() == null) {
                log.info("Reading message " + id + " for the first time.");
                mailMessage.setReadTimestamp(new Date());
            }
        }
        return mailMessage;
    }

    @Transactional
    public Inventory takeAttachments(String characterName, int id) {
        MailMessage mailMessage = get(id);
        if(!mailMessage.getToName().equalsIgnoreCase(characterName)) {
            throw new IllegalArgumentException("This is not your mail message.");
        }
        if(mailMessage.isAttachmentsRetrieved()) {
            throw new IllegalArgumentException("This message has already had its attachments removed.");
        }
        
        Inventory inventory = inventoryDao.get(characterName);
        if(mailMessage.getAttachment1() != null) {
            Item item = itemDao.get(mailMessage.getAttachment1());
            inventory.getItems().add(item);
            log.info("Taking " + item.getName() + " from message " + id + ".");
        }
        if(mailMessage.getAttachment2() != null) {
            Item item = itemDao.get(mailMessage.getAttachment2());
            inventory.getItems().add(item);
            log.info("Taking " + item.getName() + " from message " + id + ".");
        }
        if(mailMessage.getAttachment3() != null) {
            Item item = itemDao.get(mailMessage.getAttachment3());
            inventory.getItems().add(item);
            log.info("Taking " + item.getName() + " from message " + id + ".");
        }
        if(mailMessage.getAttachment4() != null) {
            Item item = itemDao.get(mailMessage.getAttachment4());
            inventory.getItems().add(item);
            log.info("Taking " + item.getName() + " from message " + id + ".");
        }
        if(mailMessage.getAttachment5() != null) {
            Item item = itemDao.get(mailMessage.getAttachment5());
            inventory.getItems().add(item);
            log.info("Taking " + item.getName() + " from message " + id + ".");
        }
        
        inventory.setCurrency(inventory.getCurrency() + mailMessage.getCurrency());
        inventory.setTokens(inventory.getTokens() + mailMessage.getTokens());
        
        if(mailMessage.getXp() > 0) {
            playerCharacterDao.addXp(playerCharacterDao.get(characterName), mailMessage.getXp());
        }
        
        
        mailMessage.setAttachmentsRetrieved(true);
        
        return inventory;
    }

    @Transactional
    public void delete(String characterName, int id) {
        MailMessage mailMessage = get(id);
        if(!mailMessage.getToName().equalsIgnoreCase(characterName)) {
            throw new IllegalArgumentException("This is not your mail message.");
        }
        mailMessage.setDeleted(true);
        log.info("Deleting message " + id + ".");
    }

    public int getCount(String name) {
        Long count = (Long)template.findByNamedQuery("mailMessage.getUnreadCount", name).get(0);
        return (int)count.longValue();
    }

    private void remove(Inventory inventory, Integer itemId) {
        Item itemToRemove = null; 
        for(Iterator<Item> iterator = inventory.getItems().iterator(); iterator.hasNext();) {
            Item item = iterator.next();
            if(item.getId().equals(itemId)) {
                iterator.remove();
                itemToRemove = item;
                break;
            }
        }
        if(itemToRemove == null) {
            Item item = itemDao.get(itemId);
            if(item == null) {
                throw new IllegalStateException("Item " + itemId + " does not exist.");
            } else if(item.getType() == ItemType.CONSUMABLE) {
                throw new IllegalStateException("You can not mail consumable items.");
            } else if(item.getType() == ItemType.QUEST) {
                throw new IllegalStateException("You can not mail quest items.");
            } else {
                throw new IllegalArgumentException("You do not own " + item.getName() + ".");
            }
        }
    }

    @Override
    protected Class<?> getManagedClass() {
        return MailMessage.class;
    }
}
