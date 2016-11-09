package restless.realms.server.web;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.mail.MailDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.session.Session;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.JsonUtils.ResponseStatus;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.util.ServiceException;

@Controller
@RequestMapping("/beta/**")
public class BetaController {
    private static final String spreadsheetId = "0AtopThdjiSN1dFRaZTRwQWsyYTZuVmlEajVvUzhZWGc";
    private static final String feedbackWorksheetKey = "od7";
    private static final SpreadsheetService service = new SpreadsheetService("restless-realms-1");
    private static final String user = "restlessdata@gmail.com";
    private static final String pass = "omgWTFdragonz!";

    @Autowired
    private MailDao mailDao;
    
    @Autowired
    private StatisticsDao statisticsDao;
    
    private static final Log log = LogFactory.getLog(BetaController.class);
    
    public BetaController() {
    }
    
    @RequestMapping("feedback")
    public ModelAndView feedback(HttpServletRequest request, @RequestParam("feedback") String feedback) {
        Session session = null;
        try {
            session = RequestUtils.getSession(request);
        } catch(Exception e) {
            // no session
        }
        
        String playerName = session == null ? "unknown" : session.getCharacterName();
        log.info("Feedback from player \"" + playerName + "\": \"" + feedback + "\"");

        Map<String, String> rowContents = new LinkedHashMap<String, String>();
        rowContents.put("player", playerName.trim());
        rowContents.put("feedback", feedback.trim());
        rowContents.put("status", "new");
        
        addRow(feedbackWorksheetKey, rowContents);
        
        mailDao.send(playerName.trim(), "Kyle", "Feedback: " + feedback.trim());
        mailDao.send(playerName.trim(), "Dan", "Feedback: " + feedback.trim());
        
        statisticsDao.increment(playerName, "feedback");
        
        return JsonUtils.getModelAndView(ResponseStatus.OK, null);
    }

    private void addRow(String worksheetKey, Map<String, String> rowContents) {
        try {
            service.setUserCredentials(user, pass);
        
            URL worksheetUrl = new URL("http://spreadsheets.google.com/feeds/list/" + spreadsheetId + "/" + worksheetKey + "/private/full");
            
            ListFeed feed = service.getFeed(worksheetUrl, ListFeed.class);
            
            ListEntry newEntry = new ListEntry();
            for(Entry<String, String> cell : rowContents.entrySet()) {
                newEntry.getCustomElements().setValueLocal(cell.getKey(), cell.getValue());
            }
            feed.insert(newEntry);
        } catch(ServiceException e) {
            throw new RuntimeException(e);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}