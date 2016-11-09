package restless.realms.client.adventure;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.util.AuditManager;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

public class AdventureCallback extends ServiceCallback {
    @Override
    public void onSuccess(ScriptObject ret) {
        ClientManager.setPerspective("adventure");
        ClientState.setAdventurePoints(ret.getInt("remainingAdventurePoints"));
        ClientManager.send(MessageType.ADVENTURE, ret.getObject("adventure"), ret.getObject("activeRoom"));
        AuditManager.audit("adventure", "start", ret.getObject("adventure").get("type"), null);
    }
    
    @Override
    public void onFailure(String code, String message) {
        if(code.equals("InsufficientFundsException")) {
            ClientState.getLayout().showPanel("noadventures");
        } else {
            super.onFailure(code, message);
        }
    }
}
