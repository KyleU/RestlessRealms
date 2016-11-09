package restless.realms.client.worldmap;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;

public class AdventureIntroductionPanel extends WindowPanel<AbsolutePanel> {
    private String locationId;
    private HTML bodyText;
    private ButtonPanel ok;
    
    public AdventureIntroductionPanel() {
        super("adventureintro", new AbsolutePanel(), "", null);
        bodyText = new HTML();
        bodyText.setWidth("740px");
        body.add(bodyText, 5, 5);
        
        ok = new ButtonPanel("", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ClientManager.send(MessageType.ENTER_LOCATION, locationId);
                clear();
            }
        }, 2);
        body.add(ok, 5, 260);
        ButtonPanel cancel = new ButtonPanel("Cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ClientState.setShowHelp(false);
                ClientManager.send(MessageType.WINDOW_CLOSE);
            }
        }, 2);
        body.add(cancel, 150, 260);
    }
    
    protected void clear() {
        setWindowTitle("");
        if(locationId != null) {
            body.removeStyleName("intro-" + locationId);
        }
        locationId = null;
        ok.setEnabled(false);
        bodyText.setHTML("Loading...");
    }

    private void setLocation(final MapLocation location) {
        String message = "<p>" + location.getDescription() + "</p>";

        ok.setEnabled(true);
        if(location.getMinLevel() > 0) {
            if(location.getMaxLevel() > 1) {
                message += "<p>Recommended for levels " + location.getMinLevel() + " - " + location.getMaxLevel() + ".</p>";
            }
            if(PlayerCharacterCache.getInstance().getLevel() < location.getMinLevel()) {
                message += "<p>" +
                "<img style=\"float:left;\" src=\"img/icon/warning.png\" />" +
                "<div style=\"float:left;padding-top:2px;padding-left:5px;\">You must be level " + location.getMinLevel() + " to start this adventure.</div>" +
                "</p>";
                ok.setEnabled(false);
            }
            ok.setText("Start New");
        } else {
            ok.setText("Enter Town");
        }
        
        if(ClientState.getAdventureId() != null) {
            if(location.getMinLevel() == 0) {
//                message += "<p>It will cost you one adventure point to resume your adventure.</p>";
                message += "<p>" +
                        "<img style=\"float:left;\" src=\"img/icon/warning.png\" />" +
                        "<div style=\"float:left;padding-top:2px;padding-left:5px;\">This will abandon your current adventure.</div>" +
                        "</p>";
            } else {
                message += "<p>" +
                		"<img style=\"float:left;\" src=\"img/icon/warning.png\" />" +
                		"<div style=\"float:left;padding-top:2px;padding-left:5px;\">This will start a new adventure.</div>" +
                		"</p>";
            }
        }
        
        setWindowTitle(location.getName());
        bodyText.setHTML(message);
        locationId = location.getId();
        body.addStyleName("intro-" + locationId);
    }

    public static void show(MapLocation location) {
        AdventureIntroductionPanel panel = (AdventureIntroductionPanel)ClientState.getLayout().getPanel("adventureintro");
        panel.clear();
        panel.setLocation(location);
        ClientState.getLayout().showPanel("adventureintro");
    }
}
