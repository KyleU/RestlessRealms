package restless.realms.client.mail;

import restless.realms.client.audio.AudioManager;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class MessagesLink extends Composite {
    private static MessagesLink instance;
    
    private SimplePanel inboxLinkContainer;
    private Anchor inboxLink;
    private int numMessages;
    
    private MessagesLink() {
        inboxLinkContainer = new SimplePanel();
        initWidget(inboxLinkContainer);
        inboxLinkContainer.getElement().setId("inboxlink");
        inboxLink = new Anchor("", true);
        inboxLink.setStylePrimaryName("link");
        inboxLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                MailPanel.show("inbox", null);
            }
        });
        inboxLinkContainer.add(inboxLink);
    }
    
    public static void init() {
        instance = new MessagesLink();
        RootPanel.get("pageheader").add(instance);
    }
    
    public static MessagesLink getInstance() {
        return instance;
    }
    
    public void setNumMessages(int numMessages) {
        int oldNum = this.numMessages;
        this.numMessages = numMessages;
        if(numMessages == 0) {
            inboxLink.setHTML("Messages");
        } else {
            inboxLink.setHTML("Messages (<strong>" + numMessages + "</strong>)");
        }
        if(numMessages > oldNum) {
            AudioManager.play("beep");
        }
    }

    public int getNumMessages() {
        return numMessages;
    }
}
