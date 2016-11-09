package restless.realms.client.quest;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

public class QuestCompletionCallback extends ServiceCallback {
    @Override
    public void onSuccess(ScriptObject result) {
        QuestCompletionPanel questCompletionPanel = (QuestCompletionPanel)ClientState.getLayout().getPanel("questcompletion");
        questCompletionPanel.onResult(result);
    }
    
    @Override
    public void onFailure(String code, String message) {
        ClientManager.send(MessageType.QUESTS_OPEN);
        super.onFailure(code, message);
    }
}
