package restless.realms.client.console.command;


public class GlobalChatCommand extends ChatCommand {
    public GlobalChatCommand() {
        this.channel = "Global";
        this.name = "chat";
        this.aliases = new String[]{"g"};
        this.description = "Global chat is viewable by everyone. Please don't spam us.";
    }
}
