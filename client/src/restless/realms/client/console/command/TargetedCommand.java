package restless.realms.client.console.command;


public abstract class TargetedCommand extends Command {
    @Override
    public void handle(String params) {
        if(params != null && params.trim().length() > 0) {
            params = params.trim();
            String playerName;
            String remainingParams;
            if(params.startsWith("\"")) {
                int endQuoteIndex = params.indexOf('"', 1);
                playerName = params.substring(1, endQuoteIndex);
                remainingParams = params.substring(endQuoteIndex + 1);
            } else {
                int spaceIndex = params.indexOf(' ', 1);
                playerName = params.substring(0, spaceIndex);
                remainingParams = params.substring(spaceIndex + 1);
            }
            handle(playerName, remainingParams);
        }
    }
    
    protected abstract void handle(String playerName, String params); 
}
