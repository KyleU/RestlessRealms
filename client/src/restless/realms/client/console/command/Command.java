package restless.realms.client.console.command;


public abstract class Command {
    public String name;
    public String[] aliases;
    public String description;
    
    public abstract void handle(String params);

    public boolean answersTo(String name) {
        if(name.equals(this.name)) {
            return true;
        }
        if(aliases != null) {
            for(String alias : aliases) {
                if(name.equals(alias)) {
                    return true;
                }
            }
        }
        return false;
    }
}
