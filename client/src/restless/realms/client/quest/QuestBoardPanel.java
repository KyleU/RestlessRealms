package restless.realms.client.quest;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.util.AuditManager;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class QuestBoardPanel extends AbstractQuestList {
    public QuestBoardPanel() {
        super("questboard", "The Tavern");
        unavailableText = "No more quests available. Why not work on the ones you have?";

        addExitButton(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                ClientManager.send(MessageType.ENTER_LOCATION, "city");
            }
        });
        
        detailsCallback = new ServiceCallback() {
            @Override
            public void onSuccess(ScriptObject result) {
                questDetailPanel.setQuestDetails(result, "accept");
            }
        };

    }
    
    @Override
    public void load() {
        ServiceManager.call("quest", "available", loadCallback);
    }

    @Override
    public void show() {
        activeLink = null;
        clear();
        ClientState.getLayout().showPanel("questboard");
        AuditManager.audit("city", "questboard", null, null);
        load();
    }
}