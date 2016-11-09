package restless.realms.client.mail;

import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.action.ActionIcon;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.room.LevelUpPanel;
import restless.realms.client.util.CommonEventHandlers;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.ButtonPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;

public class ViewMessagePanel extends Composite {
    private MailPanel mailPanel;
    
    private AbsolutePanel body;
    
    private int messageId;
    private Label nameLabel;
    private String name;
    private HTML nameLink;
    
    private ScrollPanel messageScrollPanel;
    private Label message;
    
    private ActionIcon attachment1;
    private ActionIcon attachment2;
    private ActionIcon attachment3;
    
    private Label additionalAttachments;
    
    private ButtonPanel replyButton;
    private ClickHandler replyClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            MailPanel.show("compose", name);
        }
    };

    private ButtonPanel takeItemsButton;
    private ServiceCallback takeItemsCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            ClientState.setCurrency(result.getInt("currency"));
            ClientState.setTokens(result.getInt("tokens"));
            ClientState.setItems(result.getArray("items"));
            attachment1.clear();
            attachment2.clear();
            attachment3.clear();
            additionalAttachments.setText("");
            takeItemsButton.setVisible(false);
            
            int newXp = result.getInt("xp");
            int oldXp = PlayerCharacterCache.getInstance().getXp();
            if(newXp - oldXp > 0) {
                ScriptObject xp = ScriptObject.fromJson("{xp:" + newXp + "}");
                ClientState.setPlayerCharacter(xp);
            } else if(newXp - oldXp < 0) {
                LevelUpPanel.show(new Runnable() {
                    @Override
                    public void run() {
                        ClientState.getLayout().showPanel("inbox");
                    }
                });
            } else {
                //no xp, no op
            }

        }
    };
    
    private ClickHandler takeItemsClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            ServiceManager.call("mail", "take", takeItemsCallback, "id", messageId);
        }
    };

    private ButtonPanel deleteButton;
    private ServiceCallback deleteCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            MailPanel.show("inbox", null);
        }
    };
    private ClickHandler deleteClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            boolean delete = true;
            if(takeItemsButton.isVisible()) {
                delete = Window.confirm("Are you quite sure you want to delete this message without taking the attached items?");
            }
            if(delete) {
                ServiceManager.call("mail", "delete", deleteCallback, "id", messageId);
            }
        }
    };

    public ViewMessagePanel(MailPanel mailPanel) {
        this.mailPanel = mailPanel;
        
        body = new AbsolutePanel();
        initWidget(body);
        
        nameLabel = new Label();
        body.add(nameLabel, 5, 5);
        
        nameLink = new HTML();
        nameLink.setStylePrimaryName("messagename");
        body.add(nameLink, 50, 5);
        

        messageScrollPanel = new ScrollPanel();
        messageScrollPanel.setHeight("160px");
        messageScrollPanel.setWidth("537px");
        
        message = new Label();
        message.setStylePrimaryName("messagecontent");
        messageScrollPanel.add(message);
        body.add(messageScrollPanel, 5, 25);
        
        body.add(new Label("Attachments:"), 5, 190);

        attachment1 = new ActionIcon();
        attachment1.addMouseOverHandler(CommonEventHandlers.TOOLTIP_MOUSE_OVER_ABOVE);
        attachment1.addMouseOutHandler(CommonEventHandlers.TOOLTIP_MOUSE_OUT);
        attachment1.clear();
        body.add(attachment1, 5, 210);

        attachment2 = new ActionIcon();
        attachment2.addMouseOverHandler(CommonEventHandlers.TOOLTIP_MOUSE_OVER_ABOVE);
        attachment2.addMouseOutHandler(CommonEventHandlers.TOOLTIP_MOUSE_OUT);
        attachment2.clear();
        body.add(attachment2, 55, 210);

        attachment3 = new ActionIcon();
        attachment3.addMouseOverHandler(CommonEventHandlers.TOOLTIP_MOUSE_OVER_ABOVE);
        attachment3.addMouseOutHandler(CommonEventHandlers.TOOLTIP_MOUSE_OUT);
        attachment3.clear();
        body.add(attachment3, 105, 210);
        
        additionalAttachments = new Label();
        body.add(additionalAttachments, 155, 210);
        
        replyButton = new ButtonPanel("Reply", replyClickHandler, 1);
        body.add(replyButton, 5, 270);
        takeItemsButton = new ButtonPanel("Take Items", takeItemsClickHandler, 1);
        body.add(takeItemsButton, 105, 270);
        deleteButton = new ButtonPanel("Delete", deleteClickHandler, 1);
        body.add(deleteButton, 452, 270);
    }
    
    public void setMessage(ScriptObject messageObj, JsArray<ScriptObject> attachments) {
        clear();
        
        messageId = messageObj.getInt("id");
        
        boolean sentItems = "sent".equals(mailPanel.getActiveLink());
        if(sentItems) {
            nameLabel.setText("To:");
            name = messageObj.get("toName");
        } else {
            nameLabel.setText("From:");
            name = messageObj.get("fromName");
        }
        nameLink.setHTML("<span class=\"playerlink\" onclick=\"showPlayer('" + name + "');\">" + name + "</span>");
        message.setText(messageObj.get("content"));
        
        boolean hasAttachments = false;
        boolean showAttachments = sentItems || !messageObj.getBoolean("attachmentsRetrieved");
        if(showAttachments) {
            String attachmentText = "";
            if(attachments.get(0) != null) {
                attachment1.show("item", attachments.get(0), false);
                hasAttachments = true;
            }
            if(attachments.get(1) != null) {
                attachment2.show("item", attachments.get(1), false);
                hasAttachments = true;
            }
            if(attachments.get(2) != null) {
                attachment3.show("item", attachments.get(2), false);
                hasAttachments = true;
            }
            int currency = messageObj.getInt("currency");
            if(currency > 0) {
                attachmentText += currency + " Gold. ";
                hasAttachments = true;
            }
            int tokens = messageObj.getInt("tokens");
            if(tokens > 0) {
                attachmentText += tokens + " Tokens. ";
                hasAttachments = true;
            }
            int xp = messageObj.getInt("xp");
            if(xp > 0) {
                attachmentText += xp + " Experience. ";
                hasAttachments = true;
            }
            if(hasAttachments) {
                additionalAttachments.setText(attachmentText);
            }
        }
        
        replyButton.setVisible(!sentItems);
        takeItemsButton.setVisible(hasAttachments && !sentItems);
        deleteButton.setVisible(!sentItems);
    }
    
    public void clear() {
        messageId = 0;
        name = null;
        nameLink.setText("");
        message.setText("");
        attachment1.clear();
        attachment2.clear();
        attachment3.clear();
        additionalAttachments.setText("");
    }
}
