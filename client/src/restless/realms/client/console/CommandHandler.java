package restless.realms.client.console;

import java.util.ArrayList;
import java.util.List;

import restless.realms.client.console.command.AdminCommand;
import restless.realms.client.console.command.Command;
import restless.realms.client.console.command.DuelCommand;
import restless.realms.client.console.command.GlobalChatCommand;
import restless.realms.client.console.command.HelpCommand;
import restless.realms.client.console.command.LocalChatCommand;
import restless.realms.client.console.command.PartyChatCommand;
import restless.realms.client.console.command.SignOutCommand;
import restless.realms.client.console.command.TellCommand;
import restless.realms.client.console.command.WhoCommand;

public class CommandHandler {
    private final List<Command> commands;

    public CommandHandler() {
        commands = new ArrayList<Command>();
        addCommand(new GlobalChatCommand());
        addCommand(new PartyChatCommand());
        addCommand(new LocalChatCommand());
        addCommand(new TellCommand());
        addCommand(new HelpCommand(this));
        addCommand(new SignOutCommand());
        addCommand(new WhoCommand());
        addCommand(new DuelCommand());
        addCommand(new AdminCommand());
    }
    
    public void handle(String command) {
        if(command.startsWith("/")) {
            String commandName;
            String params;
            int firstSpace = command.indexOf(' ');
            if(firstSpace == -1) {
                commandName = command.substring(1);
                params = null;
            } else {
                commandName = command.substring(1, command.indexOf(' '));
                params = command.substring(command.indexOf(' ')).trim();
            }
            
            Command c = findCommand(commandName);
            if(c == null) {
                throw new IllegalArgumentException("Unknown command \"" + commandName + "\".");
            }
            c.handle(params);
        } else {
            findCommand("local").handle(command);
        }
    }
    
    public void addCommand(Command c) {
        assert findCommand(c.name) == null : "Command \"" + c.name + "\" already registered.";
        commands.add(c);
    }

    public List<Command> getKnownCommands() {
        return commands;
    }

    private Command findCommand(String name) {
        Command ret = null;
        for(Command c : commands) {
            if(c.answersTo(name)) {
                ret = c;
                break;
            }
        }
        return ret;
    }
}
