package restless.realms.server.mail;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import restless.realms.server.account.Account;
import restless.realms.server.account.AccountDao;
import restless.realms.server.inventory.Inventory;
import restless.realms.server.item.Item;
import restless.realms.server.item.ItemDao;
import restless.realms.server.playercharacter.PlayerCharacter;
import restless.realms.server.playercharacter.PlayerCharacterDao;
import restless.realms.server.session.Session;
import restless.realms.server.util.JsonUtils;
import restless.realms.server.util.JsonUtils.ResponseStatus;
import restless.realms.server.web.RequestUtils;

@Controller
@RequestMapping("/mail/**")
public class MailController {
    @Autowired
    private AccountDao accountDao;
    
    @Autowired
    private MailDao mailDao;
    
    @Autowired
    private ItemDao itemDao;
    
    @Autowired
    private PlayerCharacterDao playerCharacterDao;
    
    @RequestMapping("send")
    public ModelAndView send(HttpServletRequest request,
        @RequestParam("to") String to,
        @RequestParam("message") String message,
        @RequestParam(value="attachment1", required=false) Integer attachment1,
        @RequestParam(value="attachment2", required=false) Integer attachment2,
        @RequestParam(value="attachment3", required=false) Integer attachment3,
        @RequestParam("currency") int currency
    ) {
        Session s = RequestUtils.getSession(request);
        Map<String, Object> ret = new HashMap<String, Object>();
        if("*".equals(to)) {
            Account account = accountDao.get(s.getAccountId());
            if(account.isAdmin()) {
                mailDao.broadcast(message, attachment1, attachment2, attachment3, currency, 0, 0);
            } else {
                throw new IllegalArgumentException("Player \"**\" does not exist.");
            }
        } else {
            Inventory inv = mailDao.send(s.getCharacterName(), to, message, attachment1, attachment2, attachment3, currency, 0, 0, true);
            ret.put("items", inv.getItems());
            ret.put("currency", inv.getCurrency());
        }
        return JsonUtils.getModelAndView(ResponseStatus.OK, ret);
    }

    @RequestMapping("inbox")
    public ModelAndView inbox(HttpServletRequest request) {
        Session s = RequestUtils.getSession(request);
        List<MailMessage> messages = mailDao.getByTo(s.getCharacterName());
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("messages", messages);
        ret.put("serverTime", new Date().getTime());
        return JsonUtils.getModelAndView(ResponseStatus.OK, ret);
    }

    @RequestMapping("sent")
    public ModelAndView sent(HttpServletRequest request) {
        Session s = RequestUtils.getSession(request);
        List<MailMessage> messages = mailDao.getByFrom(s.getCharacterName());
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("messages", messages);
        ret.put("serverTime", new Date().getTime());
        return JsonUtils.getModelAndView(ResponseStatus.OK, ret);
    }

    @RequestMapping("view")
    public ModelAndView view(HttpServletRequest request, @RequestParam("id") int id) {
        Session s = RequestUtils.getSession(request);
        MailMessage message = mailDao.get(s.getCharacterName(), id);
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("serverTime", new Date().getTime());

        ret.put("message", message);
        
        List<Item> attachments = new ArrayList<Item>();
        Integer attachment = message.getAttachment1();
        attachments.add(attachment == null || attachment == 0 ? null : itemDao.get(attachment));
        attachment = message.getAttachment2();
        attachments.add(attachment == null || attachment == 0 ? null : itemDao.get(attachment));
        attachment = message.getAttachment3();
        attachments.add(attachment == null || attachment == 0 ? null : itemDao.get(attachment));
        attachment = message.getAttachment4();
        attachments.add(attachment == null || attachment == 0 ? null : itemDao.get(attachment));
        attachment = message.getAttachment5();
        attachments.add(attachment == null || attachment == 0 ? null : itemDao.get(attachment));
        ret.put("attachments", attachments);
        
        return JsonUtils.getModelAndView(ResponseStatus.OK, ret);
    }

    @RequestMapping("take")
    public ModelAndView take(HttpServletRequest request, @RequestParam("id") int id) {
        Session s = RequestUtils.getSession(request);
        Inventory inv = mailDao.takeAttachments(s.getCharacterName(), id);

        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("items", inv.getItems());
        ret.put("currency", inv.getCurrency());
        ret.put("tokens", inv.getTokens());

        PlayerCharacter playerCharacter = playerCharacterDao.get(s);
        ret.put("xp", playerCharacter.getXp());
        
        return JsonUtils.getModelAndView(ResponseStatus.OK, ret);
    }

    @RequestMapping("delete")
    public ModelAndView delete(HttpServletRequest request, @RequestParam("id") int id) {
        Session s = RequestUtils.getSession(request);
        mailDao.delete(s.getCharacterName(), id);
        return JsonUtils.getModelAndView(ResponseStatus.OK, null);
    }

}

