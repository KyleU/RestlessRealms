package restless.realms.client.widget;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VerticalIconPanel extends Composite {
    private static final int ICON_SIZE = 20;
    
    private final String iconUrl;
    private final VerticalPanel content;
	
	public VerticalIconPanel(String iconUrl) {
	    this.iconUrl = iconUrl;
	    this.content = new VerticalPanel();
	    content.setSpacing(5);
		initWidget(content);
	}
	
	public void addIcon(int x, int y, String title) {
	    Image i = new Image(iconUrl, x * ICON_SIZE, y * ICON_SIZE, ICON_SIZE, ICON_SIZE);
	    i.setTitle(title);
        content.add(i);
	}
	
	public void clear() {
	    content.clear();
	}
}
