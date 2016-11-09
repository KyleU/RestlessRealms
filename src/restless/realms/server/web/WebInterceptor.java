package restless.realms.server.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.CookieGenerator;

import restless.realms.server.session.Session;
import restless.realms.server.session.SessionDao;

public class WebInterceptor implements HandlerInterceptor {
    static final String SESSION_REQUEST_ATTRIBUTE_KEY = "rrsession";

    private static final String SESSION_REQUEST_HEADER_KEY = "X-Restless-Realms-Session";
    private static final String SESSION_REQUEST_HEADER_KEY_LOWERCASE = "x-restless-realms-session";
    private static final String OLD_SESSION_REQUEST_HEADER_KEY = "rrsession";

    private static final String COMPACT_PRIVACY_POLICY = "CP=\"NOI CURa ADMa DEVa TAIa OUR BUS IND UNI COM NAV INT\"";
    private static final Log log = LogFactory.getLog(WebInterceptor.class);
    
    @Autowired
    private SessionDao sessionDao;

    @Autowired
    private CookieGenerator cookieGenerator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String sessionId = getSessionId(request);
        MDC.put("session", sessionId == null ? "" : sessionId);

        Session session = null;
        if(sessionId == null) {
            MDC.put("character", "");
            MDC.put("account", "");
        } else {
            session = sessionDao.get(sessionId);
            if(session == null) {
                MDC.put("character", "");
                MDC.put("account", "");
            } else {
                request.setAttribute(SESSION_REQUEST_ATTRIBUTE_KEY, session);
                String characterName = session.getCharacterName() == null ? "" : session.getCharacterName();
                MDC.put("character", characterName);
                String accountId = session.getAccountId() == null ? "" : session.getAccountId().toString();
                MDC.put("account", accountId);
            }
        }

        if(log.isInfoEnabled()) {
            String message = "Handling request for \"" + request.getServletPath() + "\".";
            log.info(message);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        MDC.remove("character");
        MDC.remove("account");
        MDC.remove("session");
        response.addHeader("P3P", COMPACT_PRIVACY_POLICY);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception arg3) throws Exception {
    }

    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    public String getSessionId(HttpServletRequest request) {
        String sessionId = request.getHeader(SESSION_REQUEST_HEADER_KEY);
        if(sessionId == null) {
            sessionId = request.getHeader(OLD_SESSION_REQUEST_HEADER_KEY);
            if(sessionId == null) {
                sessionId = request.getHeader(SESSION_REQUEST_HEADER_KEY_LOWERCASE);
            }
        }
        if(sessionId == null) {
            if(request.getCookies() != null) {
                for(Cookie cookie : request.getCookies()) {
                    if(cookieGenerator.getCookieName().equals(cookie.getName())) {
                        sessionId = cookie.getValue();
                    }
                }
            }
        }
        return sessionId;
    }
}
