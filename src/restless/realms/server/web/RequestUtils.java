package restless.realms.server.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;

import restless.realms.server.exception.NotSignedInException;
import restless.realms.server.session.Session;

public class RequestUtils {
    private static final Log log = LogFactory.getLog(RequestUtils.class);

    public static Session getSession(HttpServletRequest request) {
        return getSession(request, true);
    }
    
    public static Session getSession(HttpServletRequest request, boolean requireCharacter) {
        Session session = (Session)request.getAttribute(WebInterceptor.SESSION_REQUEST_ATTRIBUTE_KEY);
        if(session == null) {
            String sessionId = (String)MDC.get("session");
            
            String message = "Invalid session \"" + sessionId + "\".";
            log.error(message);
            throw new NotSignedInException(message);
        }
        if(!session.isActive()) {
            String message = "Inactive session \"" + session.getId() + "\".";
            log.info(message);
            throw new NotSignedInException(message);
        }
        if(requireCharacter && session.getCharacterName() == null) {
            String message = "No character selected.";
            log.info(message);
            throw new NotSignedInException(message);
        }

        return session;
    }
    
    public static String getCookieValue(String name, HttpServletRequest request) {
        String ret = null;
        if(request.getCookies() != null) {
            for(Cookie cookie : request.getCookies()) {
                if(name.equals(cookie.getName())) {
                    ret = cookie.getValue();
                }
            }
        }
        return ret;
    }
}
