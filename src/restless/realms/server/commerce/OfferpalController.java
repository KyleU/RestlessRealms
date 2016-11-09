package restless.realms.server.commerce;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.number.NumberFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.account.Account;
import restless.realms.server.account.AccountDao;
import restless.realms.server.mail.MailDao;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.JsonUtils.ResponseStatus;

@Controller
public class OfferpalController {
    
    private static final Log log = LogFactory.getLog(OfferpalController.class);
    private static final String offerpalSecretKey = "1152927549015423";

    @Autowired
    private AccountDao accountDao;

    @Autowired
    PlayerCharacterDao playerCharacterDao;
    
    @Autowired
    MailDao mailDao;
    
    @Autowired
    StatisticsDao statisticsDao;
    
    private static final NumberFormatter cashFormatter = new NumberFormatter("#,###.00");
    
    @RequestMapping("/offerpal.json")
    public ModelAndView offerpal(HttpServletRequest request, HttpServletResponse response, 
            @RequestParam(required=false, value="id") String id,
            @RequestParam(required=false, value="snuid") Integer snuid, 
            @RequestParam(required=false, value="currency") Integer currency,
            @RequestParam(required=false, value="verifier") String verifier, 
            @RequestParam(required=false, value="affl") String affiliate,
            @RequestParam(required=false, value="error") Integer error) {
        String magicString = id + ":" + snuid + ":" + currency + ":" + offerpalSecretKey;
        log.debug("Request received for offerpal. Magic String: " + magicString);

        MessageDigest m;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch(NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        m.reset();
        m.update(magicString.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String hashtext = bigInt.toString(16);
        // Now we need to zero pad it if you actually want the full 32 chars.
        while(hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        
        if(hashtext.equals(verifier)) {
            Account account = accountDao.get(snuid);
            if(account == null) {
                throw new IllegalArgumentException("No such account \"" + snuid + "\".");
            }
            int newAps = account.getAdventurePoints() + currency;
            accountDao.setAdventurePoints(account.getId(), newAps);
            
            List<PlayerCharacter> playerCharacters = playerCharacterDao.getByAccount(account.getId());
            String playerNames = "";
            for(PlayerCharacter playerCharacter : playerCharacters) {
                statisticsDao.increment(playerCharacter.getName(), "purchase-aps", currency);
                if(playerNames.length() > 0) {
                    playerNames += ", ";
                }
                playerNames += playerCharacter.getName();
            }
            float cash = (float)currency / 3;
            String cashString = "$" + cashFormatter.print(cash, Locale.US);
            String message = "Successful crediting of " + currency + " APs to account " + account.getId() + " (" + playerNames + "), for a total of " + newAps + " APs. You're now " + cashString + " richer.";
            mailDao.send("Offerpal", "Kyle", message);
            mailDao.send("Offerpal", "Dan", message);
            log.info(message);
        } else {
            String message = "Error crediting account. (\"" + snuid + "\": " + hashtext + " != " + verifier + ")";
            mailDao.send("Offerpal", "Kyle", message);
            log.error(message);
        }

        return JsonUtils.getModelAndView(ResponseStatus.OK, null);
    }
}