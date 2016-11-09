package restless.realms.server.web;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.CookieGenerator;

import restless.realms.server.account.Account;
import restless.realms.server.account.AccountDao;
import restless.realms.server.session.Session;
import restless.realms.server.session.SessionDao;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.WebUtils;

@Controller
public class RpxController {
    private static final Log log = LogFactory.getLog(RpxController.class);
    private static final String rpxApiKey = "d34f28d79ca342172e82bcbaf32881972e1055b0";
    private static final String baseUrl = "https://rpxnow.com/api/v2/auth_info";
    
    @Autowired
    private AccountDao accountDao;

    @Autowired
    private SessionDao sessionDao;
    
    @Autowired
    private CookieGenerator cookieGenerator;

    /** 
     * examples:
     * ERROR: {"err":{"msg":"Data not found","code":2},"stat":"fail"}
     * Google: {"profile":{"googleUserId":"104491357979698078685","verifiedEmail":"restlessdata@gmail.com","name":{"givenName":"Restless","familyName":"Realms","formatted":"Restless Realms"},"displayName":"restlessdata","preferredUsername":"restlessdata","providerName":"Google","identifier":"https:\/\/www.google.com\/accounts\/o8\/id?id=AItOawk3Dyk4yMIwToJBQ43Loy3lcMJ69DvXJRE","email":"restlessdata@gmail.com"},"stat":"ok"}
     * Yahoo: {"profile":{"verifiedEmail":"kyle_u@yahoo.com","name":{"formatted":"Kyle Unverferth"},"photo":"https:\/\/a248.e.akamai.net\/sec.yimg.com\/i\/identity\/nopic_48.gif","displayName":"Kyle Unverferth","preferredUsername":"Kyle Unverferth","utcOffset":"-05:00","gender":"male","providerName":"Yahoo!","identifier":"https:\/\/me.yahoo.com\/a\/SbDREWNigYud6gcfaNz2nnjRsw--#db27d","email":"kyle_u@yahoo.com"},"stat":"ok"}
     */
    
    @RequestMapping("/rpx")
    public ModelAndView rpx(
            HttpServletRequest request, 
            HttpServletResponse response, 
            @RequestParam(required=false, value="token") String token
    ) {
        ModelAndView ret = null;
        log.debug(request.getMethod() + " received for RPX. Token: " + token);
        try {
            if(token == null || token.length() == 0) {
                throw new IllegalStateException("Empty OpenID token.");
            } else {
                String content = WebUtils.getUrlContents(baseUrl + "?apiKey=" + rpxApiKey + "&token=" + token);
                log.debug("RPX Content: " + content);
                JsonNode rootNode = JsonUtils.fromString(content);
                Iterator<String> fieldNames = rootNode.getFieldNames();
                while(fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode node = rootNode.get(fieldName);
                    if(fieldName.equals("profile")) {
                        String provider = node.get("providerName").getTextValue();
                        String identifier = node.get("identifier").getTextValue();

                        String name;
                        if(node.get("name") != null) {
                            name = node.get("name").get("formatted").getTextValue();
                        } else {
                            name = node.get("displayName").getTextValue();
                        }

                        JsonNode emailNode = node.get("verifiedEmail");
                        if(emailNode == null) {
                            emailNode = node.get("email");
                        }
                        String email;
                        if(emailNode == null) {
                            email = name + "@" + provider;
                        } else {
                            email = emailNode.getTextValue();
                        }
                        
                        log.debug("Handling RPX request for " + provider + ":" + identifier + " (" + name + ", " + email + ").");
                        Account account = accountDao.getByIdentifier(identifier);
                        if(account == null) {
                            account = accountDao.createAccount(provider, identifier, email, name, content);
                        }
                        Session session = sessionDao.create(account.getId());
                        cookieGenerator.addCookie(response, session.getId());
                        if(!account.isEnabled()) {
                            throw new IllegalAccessError("Unable to sign in with this account.");
                        }
                        response.sendRedirect("index.html");
                    } else if(fieldName.equals("err")) {
                        throw new IllegalStateException(node.get("msg").getTextValue());
                    } else if(fieldName.equals("stat")) {
                        String stat = node.getTextValue();
                        if(stat.equals("ok")) {
                            // noop
                        } else if(stat.equals("fail")) {
                            log.warn("Status of \"" + stat + "\" present in root jsonNode.");
                        } else {
                            log.warn("Unknown status \"" + stat + "\" present in root jsonNode.");
                        }
                    } else {
                        log.warn("Unknown field name \"" + fieldName + "\" present in root jsonNode.");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing RPX OpenID " + request.getMethod() + ".", e);
            ret = new ModelAndView("splash");
            ret.addObject("state", "signin");
            ret.addObject("error", "Error: " + e.getMessage());
        }
        return ret;
    }
}