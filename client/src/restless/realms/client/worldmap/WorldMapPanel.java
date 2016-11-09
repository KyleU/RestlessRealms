package restless.realms.client.worldmap;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.util.CommonEventHandlers;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;

public class WorldMapPanel extends WindowPanel<DeckPanel> {
    private AbsolutePanel bremin = new AbsolutePanel();
    private AbsolutePanel kizmek = new AbsolutePanel();

    public WorldMapPanel() {
	    super("worldmap", new DeckPanel(), "World Map", null);

	    addExitButton(CommonEventHandlers.CLICK_WINDOW_CLOSE);

        bremin.setStylePrimaryName("breminmap");
        body.add(bremin);
        
        kizmek.setStylePrimaryName("kizmekmap");
        body.add(kizmek);
        
        body.showWidget(0);
        
		for(MapLocation l : ClientState.getMapLocations()) {
		    String map = l.getMap();
            if(map.equals("bremin")) {
	            bremin.add(l, l.getMapX(), l.getMapY());
		    } else {
	            kizmek.add(l, l.getMapX(), l.getMapY());
		    }
        }
		
        HTML breminLink = new HTML();
        breminLink.setStylePrimaryName("maplink");
        breminLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                body.showWidget(1);
            }
        });
        bremin.add(breminLink, 598, 245);
        
        HTML kizmekLink = new HTML();
        kizmekLink.setStylePrimaryName("maplink");
        kizmekLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                body.showWidget(0);
            }
        });
        kizmek.add(kizmekLink, 598, 245);
        
	}

    public void show() {
        boolean playPerspective = "play".equals(ClientManager.getActivePerspective());
        exitButton.setVisible(!playPerspective);
        ClientState.getLayout().showPanel("worldmap");
        ClientState.getLayout().getMainNavigation().activate("worldmap");
    }
}
