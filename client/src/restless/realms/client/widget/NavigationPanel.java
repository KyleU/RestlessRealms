package restless.realms.client.widget;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class NavigationPanel extends WindowPanel<HorizontalPanel> {
	protected String activeLink;
    private Map<String, Link> links;
	
	private ScrollPanel scrollPanel;
	protected FlowPanel leftPanel;
	protected Widget rightPanel;

    public NavigationPanel(String id, String title) {
	    super(id, new HorizontalPanel(), title, null);
	    links = new HashMap<String, Link>();
	    
	    scrollPanel = new ScrollPanel();
	    scrollPanel.setStylePrimaryName("navleftpanel");
	    
	    leftPanel = new FlowPanel();
	    scrollPanel.add(leftPanel);
	    
	    body.add(scrollPanel);
	}
    
    public void setRightPanel(Widget rightPanel) {
        this.rightPanel = rightPanel;
        rightPanel.setStylePrimaryName("navrightpanel");
        body.add(rightPanel);
    }
    
    protected void addLink(final String key, final String text, final String hoverTitle) {
        Link link = new Link(text, false, hoverTitle, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onNavigation(key);
            }
        });
        links.put(key, link);
        leftPanel.add(link);
    }
    
    protected void select(String key) {
        if(activeLink != null) {
            assert(links.containsKey(activeLink));
            links.get(activeLink).deactivate();
        }
        if(key != null) {
            assert(links.containsKey(key));
            links.get(key).activate();
        }
        activeLink = key;
    }
    
    protected abstract void onNavigation(String key);
}
