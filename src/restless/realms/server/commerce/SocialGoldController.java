package restless.realms.server.commerce;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.number.NumberFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.mail.MailDao;
import restless.realms.server.perk.PerkDao;
import restless.realms.server.playercharacter.statistics.StatisticsDao;
import restless.realms.server.session.Session;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/socialgold/**")
public class SocialGoldController {
    private static final String OFFER_ID_BUY_APS = "d6jtdpivgomdlhelt2ez1ryxq";
    private static final Map<String, Integer> PERK_OFFER_IDS = new LinkedHashMap<String, Integer>();
    static {
        //Brilliant
        PERK_OFFER_IDS.put("ay74wxqz7egwhy3dmwd8bknjz", 10009);
        //Addict
        PERK_OFFER_IDS.put("behmd80377fvc3ac1thb5y2oh", 10001);
        //Fast Learner
        PERK_OFFER_IDS.put("bw446f4o49uasygbf8jm9kpzg", 10002);        
    };

    private static final Log log = LogFactory.getLog(SocialGoldController.class);

    private final String secretMerchantKey = "hm9lsuldr29lvepir9obbsl1v";
    private final String serverName = "api.sandbox.jambool.com";
    private final SocialGoldClient client = new SocialGoldClient(serverName, secretMerchantKey);
    private static final NumberFormatter cashFormatter = new NumberFormatter("#,###.00");
    
    @Autowired
    MailDao mailDao;
    
    @Autowired
    private PerkDao perkDao;
    
    @Autowired
    StatisticsDao statisticsDao;

    @RequestMapping("offers")
    public ModelAndView offers(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Session s = RequestUtils.getSession(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();

        String url = client.getBuyCurrencyURL(OFFER_ID_BUY_APS, s.getCharacterName());
        ret.put("aps", url);
        
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("perks")
    public ModelAndView perks(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Session s = RequestUtils.getSession(request);
        Map<String, Object> ret = new LinkedHashMap<String, Object>();

        for(Entry<String, Integer> entry : PERK_OFFER_IDS.entrySet()) {
            String url = client.getBuyCurrencyURL(entry.getKey(), s.getCharacterName());
            ret.put(url, perkDao.get(entry.getValue()));
        }
        
        return JsonUtils.getModelAndView(ret);
    }

    @RequestMapping("managesub")
    public ModelAndView managesub(HttpServletRequest request, HttpServletResponse response) {
        
        writeOk(response);
        return null;
    }

    @RequestMapping("success")
    public ModelAndView success(HttpServletRequest request, HttpServletResponse response) {
        writeOk(response);
        return null;
    }

    @RequestMapping("failure")
    public ModelAndView failure(HttpServletRequest request, HttpServletResponse response) {
        writeOk(response);
        return null;
    }

    @RequestMapping("cancel")
    public ModelAndView cancel(HttpServletRequest request, HttpServletResponse response) {
        writeOk(response);
        return null;
    }

    @RequestMapping("billing")
    public ModelAndView billing(HttpServletRequest request, HttpServletResponse response) {
        writeOk(response);
        return null;
    }

    @RequestMapping("error")
    public ModelAndView error(HttpServletRequest request, HttpServletResponse response) {
        writeOk(response);
        return null;
    }

    @RequestMapping("postback")
    public ModelAndView postback(HttpServletRequest request, HttpServletResponse response) {
        writeOk(response);
        return null;
    }

    @RequestMapping("reversal")
    public ModelAndView reversal(HttpServletRequest request, HttpServletResponse response) {
        writeOk(response);
        return null;
    }

    private void writeOk(HttpServletResponse response) {
        try {
            response.getOutputStream().write("OK".getBytes());
        } catch(IOException e) {
        }
    }
}