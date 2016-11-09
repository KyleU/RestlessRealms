package restless.realms.client.layout;

import restless.realms.client.ClientManager;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.widget.Link;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class MainNavigation extends Composite {
    private Link worldmapLink;
    private Link guideLink;
    private Link questsLink;
    private Link signoutLink;

    public MainNavigation() {
        HorizontalPanel links = new HorizontalPanel();
        initWidget(links);
        links.getElement().setId("mainnavigation");

        worldmapLink = new Link("World Map", false, null, new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                ClientManager.send(MessageType.WORLD_MAP_OPEN);
            }
        });
        links.add(worldmapLink);

        guideLink = new Link("Help", false, null, new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                ClientManager.send(MessageType.GUIDE_OPEN);
            }
        });
        links.add(guideLink);

        questsLink = new Link("Quests", false, null, new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                ClientManager.send(MessageType.QUESTS_OPEN);
            }
        });
        links.add(questsLink);

        signoutLink = new Link("Sign Out", false, null, new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                ClientManager.send(MessageType.SIGNOUT_OPTIONS);
            }
        });
        links.add(signoutLink);
    }
    
    public void activate(String key) {
        worldmapLink.deactivate();
        guideLink.deactivate();
        questsLink.deactivate();
        signoutLink.deactivate();
        
        if(key == null) {
            // no-op
        } else if("worldmap".equals(key)) {
            worldmapLink.activate();
        } else if("guide".equals(key)) {
            guideLink.activate();            
        } else if("quests".equals(key)) {
            questsLink.activate();
        } else if("signout".equals(key)) {
            signoutLink.activate();
        } else {
            assert false;
        }
    }
}