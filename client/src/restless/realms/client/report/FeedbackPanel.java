package restless.realms.client.report;

import restless.realms.client.ServiceManager;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;

public class FeedbackPanel extends WindowPanel<AbsolutePanel> {
    private boolean hasFocus = false;
    private ButtonPanel submit;
    private TextArea textArea;
    private ServiceCallback callback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            textArea.setValue("Hey, thanks for the feedback.");
            Timer t = new Timer() {
                @Override
                public void run() {
                    textArea.setValue("");
                    submit.setEnabled(true);
                }
            };
            t.schedule(5000);
        }
        public void onFailure(String code, String message) {
            submit.setEnabled(true);
            super.onFailure(code, message);
        };
    };
    
	public FeedbackPanel() {
	    super("feedback", new AbsolutePanel(), "Feedback", null);
        ConsoleUtils.setFeedbackPanel(this);

	    Label caption = new Label("It only gets better from here, but not without your help. Be blunt. Be brutal. Blow our minds or crush our souls.");
	    caption.setStylePrimaryName("caption");
        this.body.add(caption , 5, 0);
        
        textArea = new TextArea();
        textArea.addFocusHandler(new FocusHandler(){
            @Override
            public void onFocus(FocusEvent event) {
                hasFocus = true;
            }
        });

        textArea.addBlurHandler(new BlurHandler(){
            @Override
            public void onBlur(BlurEvent event) {
                hasFocus = false;
            }
        });
        this.body.add(textArea, 5, 50);
        
        ClickHandler clickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String feedback = textArea.getValue().trim();
                if(feedback.length() == 0) {
                    ConsoleUtils.error("Not very useful feedback, but... thanks for trying?");
                } else {
                    submit.setEnabled(false);
                    ServiceManager.call("beta", "feedback", callback, "feedback", feedback);
                }
            }
        };
        submit = new ButtonPanel("Send Feedback", clickHandler , 2);
        body.add(submit, 96, 185);
        
        addTitleIcon(150, "img/icon/filters.png", 0, "Chat", "console");
        addTitleIcon(170, "img/icon/filters.png", 16, "Combat Log", "combatlog");
        addTitleIcon(190, "img/icon/filters.png", 32, "Feedback", null);
        addTitleIcon(210, "img/icon/filters.png", 48, "Facebook", "facebook");
	}

    public boolean hasFocus() {
        return hasFocus;
    }
}
