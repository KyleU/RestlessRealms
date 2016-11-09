package restless.realms.client.bestiary;

import java.util.ArrayList;
import java.util.List;

import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.layout.PanelLocation;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.NavigationPanel;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class BestiaryPanel extends NavigationPanel {
    private static BestiaryPanel instance;
    
    private String mobToSelect;
    private List<String> bodyguardNames;
    private MobDisplayPanel mobDisplayPanel;

    private ServiceCallback callback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            leftPanel.clear();

            bodyguardNames = new ArrayList<String>();
            JsArrayString bodyguards = result.getStringArray("bodyguards");
            for(int i = 0; i < bodyguards.length(); i++) {
                String bodyguard = bodyguards.get(i);
                bodyguardNames.add(bodyguard);
            }
            
            ScriptObject mobs = result.getObject("mobs");
            JsArrayString keys = mobs.keys();
            for(int i = 0; i < keys.length(); i++) {
                String key = keys.get(i);
                addLink(key, mobs.get(key), null);
            }
            if(mobToSelect == null) {
                mobDisplayPanel.clear();
            } else {
                onNavigation(mobToSelect);
                mobToSelect = null;
            }
            ClientState.getLayout().showPanel("bestiary");
        }
    };
    
    private ServiceCallback mobCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            ScriptObject mob = result.getObject("mob");
            boolean hired = bodyguardNames.contains(mob.get("id"));
            mobDisplayPanel.setMob(mob, result.getInt("numKilled"), hired);
        }
    };
    
    private BestiaryPanel() {
        super("bestiary", "Bestiary");
    
        mobDisplayPanel = new MobDisplayPanel();
        setRightPanel(mobDisplayPanel);
        
        addExitButton(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ClientState.getLayout().showPanel("pcinfo");
            }
        });
    }

    @Override
    protected void onNavigation(String key) {
        select(key);
        instance.mobDisplayPanel.clear();
        ServiceManager.call("character", "bestiary", instance.mobCallback, "id", key);
    }

    public static void show(String mobKey) {
        if(instance == null) {
            instance = new BestiaryPanel();
            ClientState.getLayout().addPanel(PanelLocation.MAIN, "bestiary", instance);
        }
        instance.mobToSelect = mobKey;
        ServiceManager.call("character", "bestiary", instance.callback);
    }
}