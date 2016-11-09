package restless.realms.client.console.command;

import restless.realms.client.ServiceManager;
import restless.realms.client.util.CommonServiceCallbacks;

public abstract class ChatCommand extends Command {
    protected String channel;
    
    @Override
    public void handle(String params) {
        if(params != null && params.trim().length() > 0) {
            ServiceManager.call("chat", "post", CommonServiceCallbacks.NO_OP, "channel", channel, "message", params.trim());
        }
    }
}
