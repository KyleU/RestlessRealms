package restless.realms.client.mail;

import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.belt.Quickslot;
import restless.realms.client.dragdrop.DragDropManager;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.ButtonPanel;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

public class ComposePanel extends Composite {
    private MailPanel mailPanel;
    
    private AbsolutePanel body;
    
    private TextBox to;
    private TextArea message;
    
    private Quickslot attachment1;
    private Quickslot attachment2;
    private Quickslot attachment3;
    
    private TextBox goldAttachment;
    
    private ButtonPanel send;
    
    private Label errorMessage;

    private FocusHandler focusHandler = new FocusHandler(){
        @Override
        public void onFocus(FocusEvent event) {
            mailPanel.setFocus(true);
        }
    };
    
    private BlurHandler blurHandler = new BlurHandler() {
        @Override
        public void onBlur(BlurEvent event) {
            mailPanel.setFocus(false);
        }
    };
    
    ServiceCallback sendCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            ClientState.setItems(result.getArray("items"));
            ClientState.setCurrency(result.getInt("currency"));
            MailPanel.show("sent", null);
        }
        
        @Override
        public void onFailure(String code, String message) {
            errorMessage.setText(message);
        };
    };

    ClickHandler sendHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            String goldAmount = goldAttachment.getValue();
            int gold = Integer.parseInt(goldAmount);
            ServiceManager.call("mail", "send", sendCallback, 
                    "to", to.getText(), 
                    "message", message.getText(),
                    "attachment1", attachment1.getId(),
                    "attachment2", attachment2.getId(),
                    "attachment3", attachment3.getId(),
                    "currency", gold
            );
        }
    };

    public ComposePanel(MailPanel mailPanel) {
        this.mailPanel = mailPanel;
        
        body = new AbsolutePanel();
        initWidget(body);
        
        body.add(new Label("To:"), 5, 7);
        
        to = new TextBox();
        to.addFocusHandler(focusHandler);
        to.addBlurHandler(blurHandler);
        to.getElement().setId("to");
        body.add(to, 30, 5);
        
        body.add(new Label("Message:"), 5, 35);

        message = new TextArea();
        message.setHeight("120px");
        message.setWidth("537px");
        message.addFocusHandler(focusHandler);
        message.addBlurHandler(blurHandler);
        message.getElement().setId("message");
        body.add(message, 5, 55);

        body.add(new Label("Attachments:"), 5, 190);

        attachment1 = new Quickslot(-1);
        attachment1.clear();
        DragDropManager.registerAttachmentDroppable(attachment1);
        body.add(attachment1, 5, 210);

        attachment2 = new Quickslot(-1);
        attachment2.clear();
        DragDropManager.registerAttachmentDroppable(attachment2);
        body.add(attachment2, 55, 210);

        attachment3 = new Quickslot(-1);
        attachment3.clear();
        DragDropManager.registerAttachmentDroppable(attachment3);
        body.add(attachment3, 105, 210);
        
        Image goldbg = new Image("img/interface/gold-bg.png");
        body.add(goldbg, 155, 210);
        
        goldAttachment = new TextBox();
        goldAttachment.setValue("0");
        goldAttachment.setWidth("107px");
        body.add(goldAttachment, 205, 223);

        send = new ButtonPanel("Send", sendHandler, 1);
        body.add(send, 3, 270);
        
        errorMessage = new Label("", false);
        errorMessage.setStylePrimaryName("error");
        body.add(errorMessage, 103, 275);
    }
    
    public void show(String playerName) {
        clear();
        if(playerName != null && playerName.trim().length() > 0) {
            to.setText(playerName);
        }
    }
    
    public void clear() {
        to.setText("");
        message.setText("");
        attachment1.clear();
        attachment2.clear();
        attachment3.clear();
        errorMessage.setText("");
    }
}
