package restless.realms.client.console;

import restless.realms.client.animation.ScrollToBottomAnimation;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class CombatLogPanel extends WindowPanel<ScrollPanel> {
    private final FlexTable logTable = new FlexTable();
    private int nextRow = 0;
    private int maxRows = 250;

    public CombatLogPanel() {
        super("combatlog", new ScrollPanel(), "Combat Log", null);
        body.add(logTable);
		
        addTitleIcon(150, "img/icon/filters.png", 0, "Chat", "console");
        addTitleIcon(170, "img/icon/filters.png", 16, "Combat Log", null);
        addTitleIcon(190, "img/icon/filters.png", 32, "Feedback", "feedback");
        addTitleIcon(210, "img/icon/filters.png", 48, "Facebook", "facebook");
    }
	
    public void add(String message, boolean asHtml) {
        String channelStyleClass = "channel-combat";

        if(nextRow >= maxRows) {
            logTable.removeRow(0);
            nextRow = maxRows - 1;
        }
        
        int row = nextRow++;

        Widget messageDiv = new HTML();
        if(asHtml) {
            messageDiv.getElement().setInnerHTML(message);
        } else {
            messageDiv.getElement().setInnerText(message);
        }
        messageDiv.setStylePrimaryName(channelStyleClass );
        logTable.setWidget(row, 0, messageDiv);
        new ScrollToBottomAnimation(body.getElement()).run(1000);
	}

	public void clear() {
	    logTable.clear();
	}
}