package restless.realms.client.console.command;

import restless.realms.client.console.CommandHandler;
import restless.realms.client.console.ConsoleChannel;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.util.AuditManager;

public class HelpCommand extends Command {
    private CommandHandler commandHandler;

    public HelpCommand(CommandHandler ch) {
        this.commandHandler = ch;
        this.name = "help";
        this.aliases = new String[]{"h", "?"};
        this.description = "Shows the list of available commands.";
    }

    @Override
    public void handle(String params) {
        String helpString;
        if(params != null && params.trim().length() > 0) {
            helpString = "The help command doesn't take parameters right now, sorry. We're working on it soon, though.";
            AuditManager.audit("help", params.trim());
        } else {
            helpString = "Available Commands:";
            for(Command c : commandHandler.getKnownCommands()) {
                if(c.description != null) {
                    helpString += "<br/>&nbsp;&nbsp;/" + c.name;
                    if(c.aliases != null) {
                        for(String alias : c.aliases) {
                            helpString += " or /" + alias;
                        }
                    }
                    helpString += " - " + c.description;
                }
            }
        }
        ConsoleUtils.log(ConsoleChannel.Help, helpString, true);
    }
    
}
