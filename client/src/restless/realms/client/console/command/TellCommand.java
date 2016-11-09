package restless.realms.client.console.command;

import restless.realms.client.ServiceManager;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

public class TellCommand extends TargetedCommand {
    public TellCommand() {
        this.name = "tell";
        this.aliases = new String[]{"t", "w"};
        this.description = "Send a private message to a player.";
    }
    
    @Override
    public void handle(String playerName, String params) {
        ServiceCallback serviceCallback = new ServiceCallback() {
            @Override
            public void onSuccess(ScriptObject result) {
                ConsoleUtils.help("Tell successful.");
            }
        };
        
        if(params != null && params.trim().length() > 0) {
            ServiceManager.call("chat", "tell", serviceCallback , "name", playerName, "message", params.trim());
        }
    }
}
