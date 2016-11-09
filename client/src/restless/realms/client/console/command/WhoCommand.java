package restless.realms.client.console.command;

import restless.realms.client.ServiceManager;
import restless.realms.client.console.ConsoleChannel;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.playercharacter.PlayerCharacterInfoPanel;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

import com.google.gwt.core.client.JsArrayString;

public class WhoCommand extends Command {
    private static final ServiceCallback callback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            JsArrayString names = result.getStringArray("players");
            ConsoleUtils.log(ConsoleChannel.Region, result.get("status"));
            String html = "";
            for(int i = 0; i < names.length(); i++) {
                String name = names.get(i);
                String link = "<span class=\"playerlink\" onclick=\"showPlayer('" + name + "');return false;\">" + name + "</span>";
                if(i != 0) {
                    html += ", ";
                }
                html += link;
            }
            if(names.length() >= 50) {
                html += "...";
            }
            ConsoleUtils.log(ConsoleChannel.Region, html, true);
        }
    };
    
    public WhoCommand() {
        this.name = "who";
        this.description = "Find information about a player. Usage: /who Player Name.";
    }
    
    @Override
    public void handle(String params) {
        String playerName = params == null ? "" : params.trim();
        if(playerName.length() == 0) {
            ServiceManager.call("chat", "who", callback);
        } else {
            try {
                int level = Integer.parseInt(params);
                ServiceManager.call("chat", "who", callback, "level", level);
            } catch(Exception e) {
                PlayerCharacterInfoPanel.show(playerName);
            }
        }
    }
}
