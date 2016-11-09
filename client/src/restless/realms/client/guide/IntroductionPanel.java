package restless.realms.client.guide;

import restless.realms.client.ClientState;
import restless.realms.client.layout.SizeConstants;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.WindowPanel;
import restless.realms.client.worldmap.AdventureIntroductionPanel;
import restless.realms.client.worldmap.MapLocation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;

public class IntroductionPanel extends WindowPanel<AbsolutePanel> {
    public IntroductionPanel() {
        super("introduction", new AbsolutePanel(), "Getting Started", null);
        body.setStyleName("introduction");
        ButtonPanel button = new ButtonPanel("Get Started", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for(MapLocation loc : ClientState.getMapLocations()) {
                    if("tutorial".equals(loc.getId())) {
                        AdventureIntroductionPanel.show(loc);
                    }
                }
            }
        }, 2);
        body.add(button, SizeConstants.BUTTON_LEFT, SizeConstants.BUTTON_TOP);
	}
}
