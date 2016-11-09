package restless.realms.client.mail;

import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.layout.PanelLocation;
import restless.realms.client.util.GwtUtils;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.NavigationPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MailPanel extends NavigationPanel {
    private static MailPanel instance;
    private static String lastObservedPanel;
    private static long lastObserverServerTime;

    private DeckPanel deckPanel;
    
    private ScrollPanel mailListContainer;
    private VerticalPanel mailList;
    
    private ComposePanel composePanel;
    private ViewMessagePanel viewMessagePanel;
    private boolean focus;

    private ServiceCallback viewDetailsCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            deckPanel.showWidget(2);
            viewMessagePanel.setMessage(result.getObject("message"), result.getArray("attachments"));
        }
    };

    private ServiceCallback callback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            lastObserverServerTime = result.getLong("serverTime");

            JsArray<ScriptObject> messages = result.getArray("messages");
            if(messages.length() == 0) {
                Label label = new Label("No messages");
                mailList.add(label);
                MessagesLink.getInstance().setNumMessages(0);
            } else {
                int numUnreadMessages = 0;
                for(int i = 0; i < messages.length(); i++) {
                    ScriptObject message = messages.get(i);
                    addMessage(message);
                    String readTimestamp = message.get("readTimestamp");
                    if(readTimestamp == null || "undefined".equals(readTimestamp)) {
                        numUnreadMessages++;
                    }
                }
                if(!"sent".equals(activeLink)) {
                    MessagesLink.getInstance().setNumMessages(numUnreadMessages);
                }
            }
        }

        private void addMessage(final ScriptObject message) {
            final boolean sentItems = activeLink.equals("sent");
            final boolean messageUnread = message.get("readTimestamp") == null || "undefined".equals(message.get("readTimestamp"));

            StringBuilder html = new StringBuilder();
            html.append("<div class=\"message\">");

            long sentTimestamp = message.getLong("sentTimestamp");
            html.append("<div class=\"messagedelta\">" + GwtUtils.getTimeDescription(lastObserverServerTime - sentTimestamp) + "</div>");

            if(!sentItems && messageUnread ) {
                html.append("<span style=\"font-weight:bold;\">");
            }

            String name;
            if(sentItems) {
                html.append("To: ");
                name = message.get("toName");
            } else {
                html.append("From: ");
                name = message.get("fromName");
            }
            html.append("<span class=\"messagename\">");
            html.append(name);
            html.append("</span> - ");
            
            html.append(GwtUtils.escapeHtml(message.get("content")));

            if(!sentItems && messageUnread) {
                html.append("</span>");
            }
            
            html.append("</div>");
            
            HTML messageDiv = new HTML(html.toString());
            messageDiv.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if(!sentItems && messageUnread) {
                        MessagesLink.getInstance().setNumMessages(MessagesLink.getInstance().getNumMessages() - 1);
                    }
                    ServiceManager.call("mail", "view", viewDetailsCallback, "id", message.getInt("id"));
                }
            });
            
            mailList.add(messageDiv);
        }
    };
    
    private MailPanel() {
        super("inbox", "Messages");
        deckPanel = new DeckPanel();
        setRightPanel(deckPanel);
        
        composePanel = new ComposePanel(this);
        deckPanel.add(composePanel);
        
        mailListContainer = new ScrollPanel();
        mailListContainer.setStylePrimaryName("maillist");
        mailList = new VerticalPanel();
        mailListContainer.add(mailList);
        deckPanel.add(mailListContainer);

        viewMessagePanel = new ViewMessagePanel(this);
        deckPanel.add(viewMessagePanel);
        
        addExitButton(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                assert lastObservedPanel != null;
                ClientState.getLayout().showPanel(lastObservedPanel);
            }
        });
        
        addLink("compose", "Compose", null);
        addLink("inbox", "Inbox", null);
        addLink("sent", "Sent Items", null);
    }
    
    public static void show(String action, String playerName) {
        if(instance == null) {
            instance = new MailPanel();
            ClientState.getLayout().addPanel(PanelLocation.MAIN, "mail", instance);
        }

        String mainWindowPanelKey = ClientState.getLayout().getMainWindowPanelKey();
        if(!"mail".equals(mainWindowPanelKey)) {
            lastObservedPanel = mainWindowPanelKey;
        }
        ClientState.getLayout().showPanel("mail");

        instance.onNavigation(action);
        if("compose".equals(action)) {
            instance.composePanel.show(playerName);
        }
    }
    
    @Override
    protected void onNavigation(String key) {
        select(key);
        setWindowTitle(key);
        if("compose".equals(key)) {
            composePanel.clear();
            deckPanel.showWidget(0);
        } else {
            mailList.clear();
            deckPanel.showWidget(1);
            ServiceManager.call("mail", key, instance.callback);
        }
    }
    
    public String getActiveLink() {
        return activeLink;
    }

    public static MailPanel getInstance() {
        return instance;
    }
    
    public static boolean hasFocus() {
        return instance == null ? false : instance.focus;
    }
    
    public void setFocus(boolean focus) {
        this.focus = focus;
    }
}
