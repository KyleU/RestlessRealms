package restless.realms.server.perk;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.inventory.InventoryDao;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;

@Service
public class PerkDao extends AbstractDao<Perk> {
    private static final Log log = LogFactory.getLog(PerkDao.class);
    
    @Autowired
    private PlayerCharacterDao playerDao;

    @Autowired
    private InventoryDao inventoryDao;

    @Autowired
    private StatisticsDao statisticsDao;
    
    public Perk get(Integer id) {
        return super.get(id);
    }
    
    @SuppressWarnings("unchecked")
    public List<Perk> getPerkset(String playerName) {
        List<Perkset> result = template.findByNamedQuery("perkset.get", playerName);
        Perkset perkset = null;
        if(result.size() == 1) {
            perkset = result.get(0);
        } else if(result.size() == 0) {
            PlayerCharacter p = playerDao.get(playerName);
            if(p == null) {
                IllegalStateException e = new IllegalStateException("Player does not exist!");
                log.error("No player found.", e);
                throw e;
            }
            log.debug("Creating perkset for player \"" + p.getName() + "\"");
            perkset = new Perkset();
            perkset.setPlayerName(playerName);
            List<Perk> startingPerks = new ArrayList<Perk>();
            perkset.setPerks(startingPerks);
            template.save(perkset);
        }
        
        return perkset.getPerks();
    }
    
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public List<Perk> train(String playerName, Integer perkId) {
        PlayerCharacter pc = playerDao.get(playerName);
        
        List<Perk> perkset = getPerkset(playerName);
        Perk p = get(perkId);
        
        if(p == null) {
            throw new IllegalStateException("Invalid skill \"" + perkId + "\".");
        }
        if(pc.getLevel() < p.getMinLevel()) {
            throw new IllegalStateException("You must be level " + p.getMinLevel() + " to learn " + p.getName() + ".");
        }
        for(Perk perk : perkset) {
            if(perk.getId().equals(p.getId())) {
                throw new IllegalStateException("You already own \"" + p.getName() + "\".");
            }
        }

        int tokens = inventoryDao.getTokens(playerName);
        if(tokens < p.getMsrp()) {
            throw new IllegalStateException("You cannot afford this perk, as you only have " + tokens + " tokens.");
        }

        inventoryDao.removeTokens(playerName, p.getMsrp());

        statisticsDao.increment(playerName, "perk-buy");
        statisticsDao.increment(playerName, "token-spend", p.getMsrp());

        perkset.add(p);
        return perkset;
    }

    @SuppressWarnings("unchecked")
    public List<Perk> getForSale() {
        return template.findByNamedQuery("perk.getForSale");
    }

    @Override
    protected Class<?> getManagedClass() {
        return Perk.class;
    }
}