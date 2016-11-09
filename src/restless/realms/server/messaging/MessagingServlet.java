package restless.realms.server.messaging;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atmosphere.cpr.AtmosphereResource;

import restless.realms.server.session.Session;
import restless.realms.server.session.SessionDao;
import restless.realms.server.util.SpringUtils;

import com.greencat.gwt.comet.server.GwtAtmosphereResource;
import com.greencat.gwt.comet.server.GwtAtmosphereServlet;

public class MessagingServlet extends GwtAtmosphereServlet {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(MessagingServlet.class);

    private static SessionDao sessionDao;
    
    public MessagingServlet() {
        super();
        computeHeartbeat(30 * 1000);
    }
    
    @Override
    public void doComet(GwtAtmosphereResource resource) throws ServletException, IOException {
        super.doComet(resource);
        AtmosphereResource<HttpServletRequest, HttpServletResponse> atmosphereResource = resource.getAtmosphereResource();
        String characterName = getCharacterName(atmosphereResource.getRequest());
        
        log.debug("Comet request for \"" + characterName + "\"");
        atmosphereResource.getBroadcaster().setID("restless");
        
        MessagingUtils.addResource(characterName, atmosphereResource);
    }

    @Override
    public void cometTerminated(GwtAtmosphereResource gwtAtmosphereResource, boolean flag) {
        super.cometTerminated(gwtAtmosphereResource, flag);
        MessagingUtils.removeResource(gwtAtmosphereResource.getAtmosphereResource());
    }
    
    private static String getCharacterName(HttpServletRequest request) {
        String sessionId = request.getParameter("rrsession");
        if(sessionId == null) {
            throw new IllegalStateException("No session id provided.");
        }
        if(sessionDao == null) {
            sessionDao = (SessionDao)SpringUtils.getApplicationContext().getBean("sessionDao");
        }
        Session session = sessionDao.get(sessionId);
        if(session == null) {
            throw new IllegalStateException("Invalid session \"" + sessionId + "\"."); 
        }
        if(!session.isActive()) {
            throw new IllegalStateException("Inactive session \"" + sessionId + "\"."); 
        }
        if(session.getCharacterName() == null) {
            throw new IllegalStateException("No character selected for session \"" + sessionId + "\"."); 
        }
        return session.getCharacterName();
    }
}