package restless.realms.server.web;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import restless.realms.server.mail.MailDao;
import restless.realms.server.session.Session;

@Controller
public class ProblemsController {
    @Autowired
    private MailDao mailDao;

    private static final Log log = LogFactory.getLog(ProblemsController.class);

    public ProblemsController() {
    }

    @RequestMapping("/problems")
    public String problems(HttpServletRequest request, @RequestParam(value = "detail", required = false) String detail) {
        Session session = null;
        try {
            session = RequestUtils.getSession(request);
        } catch(Exception e) {
            // no session
        }

        String ret;
        if(detail == null || detail.trim().length() == 0) {
            log.info("Problem form being served. Request: " +getRequestDump(request));
            ret = "problems";
        } else {
            log.error("Problem submission \"" + detail.trim() + "\");");

            log.error("Problematic Request: " + getRequestDump(request));

            mailDao.send(session == null || session.getCharacterName() == null ? "unknown" : session.getCharacterName(), "Kyle", detail);
            ret = "redirect:index.html?error=Thanks for your feedback.";
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private static String getRequestDump(HttpServletRequest request) {
        String requestDump = request.getMethod() + " " + request.getRequestURI();
        
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            requestDump += "\n  " + name + ": " + value;
        }
        return requestDump;
    }
}