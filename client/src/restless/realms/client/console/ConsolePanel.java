package restless.realms.client.console;

import restless.realms.client.animation.ScrollToBottomAnimation;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ConsolePanel extends WindowPanel<ScrollPanel> {
    private static boolean animated = false;

    private final FlexTable logTable = new FlexTable();
    private int nextRow = 0;
    private int maxRows = 250;

    private final TextBox consoleInput = new TextBox();
    private final Image submitButton;

    private CommandHandler commandHandler;
    private boolean hasFocus = false;
    
    
    public ConsolePanel() {
        super("console", new ScrollPanel(), "Chat", new AbsolutePanel());
        commandHandler = new CommandHandler();
        body.add(logTable);
		ConsoleUtils.setConsolePanel(this);
		
        consoleInput.addFocusHandler(new FocusHandler(){
            @Override
            public void onFocus(FocusEvent event) {
                hasFocus = true;
            }
        });

        consoleInput.addBlurHandler(new BlurHandler(){
            @Override
            public void onBlur(BlurEvent event) {
                hasFocus = false;
            }
        });

	    consoleInput.getElement().setId("consoleinput");
	    
	    AbsolutePanel apFooter = (AbsolutePanel)footer;
	    apFooter.add(consoleInput, 0, 0);

        ClickHandler clickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                processCommand();
            }
        };
        
        submitButton = new Image("img/interface/sendbutton.png");
	    submitButton.getElement().setId("consolebutton");
	    submitButton.addClickHandler(clickHandler);
	    apFooter.add(submitButton, 177, 2);
	    
        addTitleIcon(150, "img/icon/filters.png", 0, "Chat", null);
        addTitleIcon(170, "img/icon/filters.png", 16, "Combat Log", "combatlog");
        addTitleIcon(190, "img/icon/filters.png", 32, "Feedback", "feedback");
        addTitleIcon(210, "img/icon/filters.png", 48, "Facebook", "facebook");
    }
	    
    public void add(ConsoleChannel channel, String message, boolean asHtml) {
        String channelStyleClass = "channel-" + channel.toString().toLowerCase();
        Widget messageDiv = new HTML();
        if(asHtml) {
            messageDiv.getElement().setInnerHTML(message);
        } else {
            messageDiv.getElement().setInnerText(message);
        }
        messageDiv.setStylePrimaryName(channelStyleClass);
        add(messageDiv);
    }

    public void add(Widget w) {
        if(nextRow >= maxRows) {
            logTable.removeRow(0);
            nextRow = maxRows - 1;
        }
        
        int row = nextRow++;

        logTable.setWidget(row, 0, w);
        if(animated) {
            new ScrollToBottomAnimation(body.getElement()).run(1000);
        } else {
            body.scrollToBottom();
        }
    }

	public void processCommand() {
		String commandString = consoleInput.getValue().trim();
        consoleInput.setValue("");
        consoleInput.setFocus(false);
		if(commandString.length() > 0) {
			try {
			    commandHandler.handle(commandString);
			} catch(Exception e) {
			    ConsoleUtils.error(e.getMessage());
			}
		}
	}
	
	public void clear() {
	    logTable.clear();
	}
	
	public boolean hasFocus() {
	    return hasFocus;
	}
	
	public void focus() {
        consoleInput.setFocus(true);
    }
	
	public static void setAnimated(boolean animated) {
        ConsolePanel.animated = animated;
    }
    
	public void setText(String text) {
        consoleInput.setText(text);
    }
    
    public void blur() {
        consoleInput.setFocus(false);
    }
}