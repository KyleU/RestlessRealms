package restless.realms.client.console.command;

import restless.realms.client.ClientManager;
import restless.realms.client.perspective.MessageType;

public class SignOutCommand extends Command {
    public SignOutCommand() {
        this.name = "quit";
        this.aliases = new String[]{"signout"};
        this.description = "Sign out of Restless Realms.";
    }
    
    @Override
    public void handle(String params) {
        ClientManager.send(MessageType.SIGNOUT_OPTIONS);
    }
}
