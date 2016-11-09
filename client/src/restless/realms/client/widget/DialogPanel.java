package restless.realms.client.widget;

import restless.realms.client.ClientState;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DialogPanel extends WindowPanel<VerticalPanel> {
    private static DialogPanel instance;

    private FlowPanel buttonPanels;
    private HTML content;
    private DialogBox dialogBox;

    public DialogPanel() {
        super("dialog", new VerticalPanel(), "", null);
        assert instance == null;
        instance = this;
        
        content = new HTML();
        content.setStylePrimaryName("dialogcontent");
        body.add(content);
        
        buttonPanels = new FlowPanel();
        buttonPanels.setStylePrimaryName("dialogbuttons");
        body.add(buttonPanels);
    }
    
    public static void show(DialogBox dialogBox) {
        assert instance != null;
        instance.showDialog(dialogBox);
    }
    
    private void showDialog(DialogBox dialogBox) {
        ClientState.getLayout().showPanel("dialog");
        
        clear();
        this.dialogBox = dialogBox;

        setWindowTitle(dialogBox.getTitle());
        content.getElement().setInnerHTML(dialogBox.getContentHtml());

        if(dialogBox.getButtons() != null) {
	        for(int i = 0; i < dialogBox.getButtons().length; i++) {
	            String button = dialogBox.getButtons()[i];
	            addButton(button);
	        }
        }
    }
    
    private void addButton(final String button) {
        ButtonPanel buttonPanel = new ButtonPanel(button, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                assert dialogBox != null;
                for(int i = 0; i < buttonPanels.getWidgetCount(); i++) {
                    ButtonPanel button = (ButtonPanel)buttonPanels.getWidget(i);
                    button.setEnabled(false);
                }
                dialogBox.onAction(button);
            }
        }, 2);
        buttonPanels.add(buttonPanel);
    }

    private void clear() {
        setTitle("");
        content.getElement().setInnerHTML("");
        buttonPanels.clear();
    }
}
