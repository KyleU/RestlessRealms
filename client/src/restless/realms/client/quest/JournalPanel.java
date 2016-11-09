package restless.realms.client.quest;

import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.layout.PanelLocation;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.NavigationPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;

public class JournalPanel extends NavigationPanel {
    private static JournalPanel instance;
    
    JsArray<ScriptObject> pages;
    
    private HTML body;

    private ServiceCallback callback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            leftPanel.clear();

            pages = result.getArray("journal");
            for(int i = 0; i < pages.length(); i++) {
                ScriptObject page = pages.get(i);
                addLink(((Integer)i).toString(), page.get("name"), null);
            }
            ClientState.getLayout().showPanel("journal");
        }
    };
    
    private JournalPanel() {
        super("journal", "Kale's Journal");
    
        ScrollPanel bodyContainer = new ScrollPanel();
        
        body = new HTML("");
        body.setStylePrimaryName("journalentry");
        bodyContainer.add(body);
        setRightPanel(bodyContainer);
        
        addExitButton(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ClientState.getLayout().showPanel("quests");
            }
        });
        
        clear();
    }

    @Override
    protected void onNavigation(String key) {
        select(key);
        instance.body.setText(pages.get(Integer.parseInt(key)).get("journal"));
    }

    public static void show() {
        if(instance == null) {
            instance = new JournalPanel();
            ClientState.getLayout().addPanel(PanelLocation.MAIN, "journal", instance);
        }
        
        instance.clear();
        ServiceManager.call("character", "journal", instance.callback);
    }

    private void clear() {
        select(null);
        body.setHTML("<p>The Bremin and Kizmek kingdoms battled for a hundred years for control of the world. What they left in their wake was shattered landscapes, scorched earth, and millions dead. After the events of the Great War, the survivors established an Outpost on both continents and struggled to rebuild their lives.</p>" +
                "<p>However, with the world in its current state, something stirred beneath the blood stained surface of the Earth and corrupted man and beast alike. A researcher, Kale Dorvek, took a team of colleagues and veteran soldiers through these brutal lands to investigate the source of the corruption.</p>" + 
                "<p>These are his journal pages, which you'll find scattered throughout various locations. Read them, learn the state of the world, what has happened, and what's to come. Follow in his footsteps and discover what you can, for it's your only link to the Realms past, present, and future.</p>"
        );
    }
}