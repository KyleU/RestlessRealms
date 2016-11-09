package restless.realms.client.layout;

import java.util.HashMap;
import java.util.Map;

import restless.realms.client.belt.BeltPanel;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class MainLayout {
	private final PanelInfo main;
	private final PanelInfo bottomLeft;
    private final PanelInfo bottomCenter;
	private final PanelInfo bottomRight;
	
    private Map<String, PanelLocation> panelLocations;

    private BeltPanel beltPanel;
    private MainNavigation mainNavigation;
    
    private String mainWindowPanelKey;
    
	public MainLayout() {
	    panelLocations = new HashMap<String, PanelLocation>();
	    
		main = new PanelInfo();
		main.deckPanel.setHeight("330px");
		main.deckPanel.setWidth("750px");
		RootPanel.get("container-main").add(main.deckPanel);
		
		bottomLeft = new PanelInfo();
		bottomLeft.deckPanel.setWidth("230px");
		bottomLeft.deckPanel.setHeight("250px");
		RootPanel.get("container-bottomleft").add(bottomLeft.deckPanel);

        bottomCenter = new PanelInfo();
        bottomCenter.deckPanel.setWidth("262px");
        bottomCenter.deckPanel.setHeight("250px");
        RootPanel.get("container-bottomcenter").add(bottomCenter.deckPanel);

        bottomRight = new PanelInfo();
		bottomRight.deckPanel.setWidth("230px");
		bottomRight.deckPanel.setHeight("250px");
		RootPanel.get("container-bottomright").add(bottomRight.deckPanel);
	}

	public boolean containsPanel(String key) {
	    return panelLocations.containsKey(key);
	}
	
	public void addPanel(PanelLocation location, String key, Widget panel) {
	    if(panelLocations.containsKey(key)) {
	        throw new RuntimeException("Panel already registered with key \"" + key + "\".");
	    }
	    panelLocations.put(key, location);
		PanelInfo panelInfo = getPanelInfo(location);
		panelInfo.add(key, panel);
	}

    public Widget getPanel(String key) {
        PanelLocation location = panelLocations.get(key);
        PanelInfo panelInfo = getPanelInfo(location);
        Integer index = panelInfo.panelIndexes.get(key);
        
        return panelInfo.deckPanel.getWidget(index);
    }

    public void showPanel(String key) {
		PanelLocation location = panelLocations.get(key);
        if(location == PanelLocation.MAIN) {
            mainWindowPanelKey = key;
        }
        PanelInfo panelInfo = getPanelInfo(location);
		Integer index = panelInfo.panelIndexes.get(key);
		
		String colHeight = panelInfo.deckPanel.getElement().getStyle().getProperty("height");
        panelInfo.deckPanel.getWidget(index).setHeight(colHeight);
		panelInfo.deckPanel.showWidget(index);
	}

    public BeltPanel getBeltPanel() {
        return beltPanel;
    }
    
    public MainNavigation getMainNavigation() {
        return mainNavigation;
    }
    
    private PanelInfo getPanelInfo(PanelLocation location) {
		PanelInfo panelInfo = null;
		switch(location) {
		case MAIN:
			panelInfo = main;
			break;
        case BOTTOMLEFT:
            panelInfo = bottomLeft;
            break;
        case BOTTOMCENTER:
            panelInfo = bottomCenter;
            break;
		case BOTTOMRIGHT:
			panelInfo = bottomRight;
			break;
		default:
		    break;
		}
		return panelInfo;
	}

    public void showNavAndBelt() {
        if(this.beltPanel == null) {
            this.beltPanel = new BeltPanel();
            RootPanel.get("beltcontainer").add(beltPanel);
            
            this.mainNavigation = new MainNavigation();
            RootPanel.get("pageheader").add(mainNavigation);
        } else {
            beltPanel.getElement().getStyle().setProperty("display", "block");
            DOM.getElementById("mainnavigation").getStyle().setProperty("display", "block");
        }
    }

    public String getMainWindowPanelKey() {
        return mainWindowPanelKey;
    }
}
