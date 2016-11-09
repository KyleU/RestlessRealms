package restless.realms.client.quest;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.util.CommonEventHandlers;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

public class QuestListPanel extends AbstractQuestList {
    public QuestListPanel() {
        super("quests", "Active Quests");
        unavailableText = "No current quests. Head to The Outpost to get new ones.";

        addExitButton(CommonEventHandlers.CLICK_WINDOW_CLOSE);

        detailsCallback = new ServiceCallback() {
            public void onSuccess(ScriptObject result) {
                String mode = ClientManager.getActivePerspective().equals("combat") ? "active" : "complete";
                questDetailPanel.setQuestDetails(result, mode);
            }
        };
    }

    @Override
    public void load() {
        ServiceManager.call("quest", "current", loadCallback);
    }

    @Override
    public void show() {
        clear();
        ClientState.getLayout().showPanel("quests");
        ClientState.getLayout().getMainNavigation().activate("quests");
        load();
    }
}