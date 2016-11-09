package restless.realms.client.console.command;

import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.pvp.PvpIntroPanel;

public class DuelCommand extends Command {
    public DuelCommand() {
        this.name = "duel";
        this.description = "Begin a PvP duel against player. Usage: /duel Player Name.";
    }
    
    @Override
    public void handle(String params) {
        String playerName = params.trim();
        if(playerName.length() == 0) {
            ConsoleUtils.error("Usage: \"/duel player-name\"");
        } else {
            PvpIntroPanel.show(playerName);
        }
    }
}
