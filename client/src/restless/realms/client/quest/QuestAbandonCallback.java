package restless.realms.client.quest;

import restless.realms.client.ClientManager;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

public class QuestAbandonCallback extends ServiceCallback {
    @Override
    public void onSuccess(ScriptObject result) {
        ClientManager.send(MessageType.QUESTS_OPEN);
    }
    
    @Override
    public void onFailure(String code, String message) {
        ClientManager.send(MessageType.QUESTS_OPEN);
        super.onFailure(code, message);
    }
}
