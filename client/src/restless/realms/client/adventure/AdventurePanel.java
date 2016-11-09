package restless.realms.client.adventure;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.DialogBox;
import restless.realms.client.widget.DialogPanel;
import restless.realms.client.widget.NavigationPanel;
import restless.realms.client.worldmap.MapLocation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class AdventurePanel extends NavigationPanel {
    private static final String WARNING_TEXT = "Whoa, hold on a second. " +
    		"You're about to abandon your adventure. " +
    		"If you leave now, your progress in this adventure will not be saved and you'll have to start over if you decide to come back." + 
    		"You can select \"Nevermind\" and return to the adventure map." +            
    		"<br/><br/>Are you sure you want to leave?";
    
    private ButtonPanel abandonButton;
    private Compass compass;
    private AdventureMapPanel mapPanel;

    private ScriptObject adventure;
    
    public AdventurePanel() {
        super("adventure", "");
        body.setStylePrimaryName("adventure");
        
        abandonButton = new ButtonPanel("Abandon", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                DialogBox dialogBox = new DialogBox("Abandon Adventure", WARNING_TEXT, "Yes, I'm Sure", "Nevermind") {
                    @Override
                    public void onAction(String action) {
                        if("Yes, I'm Sure".equals(action)) {
                            ClientManager.send(MessageType.ENTER_LOCATION, "city");                            
                        } else if ("Nevermind".equals(action)) {
                            ClientManager.send(MessageType.WINDOW_CLOSE);
                        } else {
                            super.onAction(action);
                        }
                    }
                };
                DialogPanel.show(dialogBox );
            }
        }, 1);
        //absolutePanel.add(abandonButton, 45, 15);
        abandonButton.addStyleName("abandonbutton");
        leftPanel.add(abandonButton);

        compass = new Compass();
        //absolutePanel.add(compass, 42, 95);
        leftPanel.add(compass);        

        mapPanel = new AdventureMapPanel();
        setRightPanel(mapPanel);
    }

    @Override
    protected void onNavigation(String key) {
        assert false : key;
    }
    
    public void setAdventure(ScriptObject a) {
        if(this.adventure != null) {
            body.removeStyleDependentName(this.adventure.get("type"));
        }
        if(a == null) {
            mapPanel.setRooms(null);
            setWindowTitle("");
        } else {
            mapPanel.setRooms(a.getArray("rooms"));
            setWindowTitle(getName(a.get("type")));
            body.addStyleDependentName(a.get("type"));
        }
        this.adventure = a;
    }


    public void setActiveRoomIndex(String previousRoomState, int index, String type, String state) {
        mapPanel.setActiveRoomIndex(previousRoomState, index, type, state);
        int[] neighbors = mapPanel.getNeighbors(index);
        compass.setAvailableDirections(neighbors[0] > -1, neighbors[1] > -1, neighbors[2] > -1, neighbors[3] > -1);
    }

    public void disableCompass() {
        compass.setAvailableDirections(false, false, false, false);
    }

    private String getName(String type) {
        String ret = null;
        for(MapLocation location : ClientState.getMapLocations()) {
            if(location.getId().equals(type)) {
                ret = location.getName();
                break;
            }
        }
        assert ret != null;
        return ret;
    }

    public ScriptObject getAdventure() {
        return adventure;
    }

    public AdventureMapPanel getMapPanel() {
        return mapPanel;
    }
}
