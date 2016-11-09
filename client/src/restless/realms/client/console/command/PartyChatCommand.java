package restless.realms.client.console.command;

import restless.realms.client.ClientState;


public class PartyChatCommand extends ChatCommand {
    public PartyChatCommand() {
        this.channel = "Party";
        this.name = "party";
        this.aliases = new String[]{"p"};
        this.description = "Party chat is pretty useless, since we don't support parties yet.";
    }
    
    @Override
    public void handle(String params) {
        if(params != null && params.trim().length() > 0) {
            super.handle(params);
        } else {
            if(AdminCommand.isAdmin()) {
                ClientState.getLayout().showPanel("party");
            }
        }
    }
}
