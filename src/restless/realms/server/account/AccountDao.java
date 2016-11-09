package restless.realms.server.account;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import restless.realms.server.database.AbstractDao;
import restless.realms.server.exception.InsufficientFundsException;


@Repository
public class AccountDao extends AbstractDao<Account> {
    private static final Log log = LogFactory.getLog(AccountDao.class);
    
    public AccountDao() {
    }
    
    public Account get(Integer id) {
        return super.get(id);
    }

    @SuppressWarnings("unchecked")
    public Account getByEmail(String email) {
        List<Account> result = template.findByNamedQuery("account.getByEmail", email);
        return result.size() == 0 ? null : result.get(0);
    }
    
    @SuppressWarnings("unchecked")
    public Account getByIdentifier(String identifier) {
        List<Account> result = template.findByNamedQuery("account.getByIdentifier", identifier);
        return result.size() == 0 ? null : result.get(0);
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Account createAccount(String provider, String identifier, String email, String name, String additionalInfo) {
        Account ret = new Account();
        
        ret.setProvider(provider);
        ret.setIdentifier(identifier);
        ret.setEmail(email);
        ret.setName(name);
        ret.setLocale("Unknown");
        if(additionalInfo == null) {
            additionalInfo = "";
        }
        if(additionalInfo.length() > 2500) {
            additionalInfo = additionalInfo.substring(0, 2490) + "...";
        }
        ret.setAdditionalInfo(additionalInfo);

        template.save(ret);
        
        return ret;
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Account createAccount(Map<String, Object> clientInfo) {
        Account ret = new Account();
        
        long facebookUserId = Long.parseLong(clientInfo.get("uid").toString());
        ret.setProvider("Facebook");
        ret.setIdentifier("" + facebookUserId);

        String email = clientInfo.get("proxied_email").toString();
        if(email == null || "null".equals(email)) {
            email = facebookUserId + "@facebook.com";
        }
        ret.setEmail(email);

        ret.setName((String)clientInfo.get("name"));
        ret.setLocale((String)clientInfo.get("locale"));
        String location = clientInfo.get("current_location").toString();
        if(location.equals("null")) {
            location = clientInfo.get("hometown_location").toString();
        }
        if(location.equals("null")) {
            location = "Unknown";
        }
        ret.setAdditionalInfo(location);
        try {
            ret.setTimezone((Integer)clientInfo.get("timezone"));
        } catch (Exception e) {
            log.error("Invalid timezone \"" + clientInfo.get("timezone") + "\".", e);
            ret.setTimezone(-1);
        }

        template.save(ret);
        
        return ret;
    }

    @Transactional
    public int spendAdventurePoints(Integer accountId, int amount) {
        Account account = get(accountId);
        if(account.getAdventurePoints() - amount < 0) {
            throw new InsufficientFundsException();
        }
        account.setAdventurePoints(account.getAdventurePoints() - amount);
        return account.getAdventurePoints();
    }

    @Transactional
    public void setAdventurePoints(Integer accountId, int value) {
        Account account = get(accountId);
        account.setAdventurePoints(value);
    }

    @Override
    protected Class<?> getManagedClass() {
        return Account.class;
    }
}
