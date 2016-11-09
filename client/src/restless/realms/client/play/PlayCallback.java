package restless.realms.client.play;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.chat.ChatService;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.console.command.AdminCommand;
import restless.realms.client.mail.MessagesLink;
import restless.realms.client.messaging.MessagingManager;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.util.AuditManager;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.TipPanel;

public class PlayCallback extends ServiceCallback {
    @Override
    public void onSuccess(ScriptObject result) {
        //happens before perspectiveChange, so onEnter can use it.
        ClientState.setAdventureId(result.hasKey("adventure") ? result.getInt("adventure") : null);
        ClientState.setAdventureStatus(result.hasKey("adventureStatus") ? result.get("adventureStatus") : null);
        ClientState.setMapLocations(result.getArray("adventures"));
        ClientState.setShowHelp(result.getBoolean("showHelp"));
        
        ScriptObject accountDetails = result.getObject("accountDetails");
        AdminCommand.setAdmin(accountDetails.getBoolean("administrator"));

        ClientManager.setPerspective("play");
        
        ClientState.setCurrency(result.getInt("currency"));
        ClientState.setTokens(result.getInt("tokens"));

        PlayerCharacterCache.getInstance().setProfession(result.getObject("profession"));
        PlayerCharacterCache.getInstance().setEquipment(result.getObject("equipment"));

        ScriptObject pc = result.getObject("player");
        
        // seed gender for portrait
        ClientState.setPlayerCharacter(ScriptObject.fromJson("{\"gender\":\"" + pc.get("gender").toLowerCase() + "\"}"));
        
        ClientState.setPerks(result.getArray("perks"));
        
        ClientState.setItems(result.getArray("inventory"));
        ClientState.setSkills(result.getArray("skills"));
        ClientState.setPlayerCharacter(pc);

        ConsoleUtils.help("Welcome to Restless Realms.");

        ChatService.refreshNow();
        MessagingManager.init();
        AuditManager.audit("play", "start");

        int nextAdventurePointSeconds = result.getInt("nextAdventurePointSeconds");
        int adventurePoints = accountDetails.getInt("adventurePoints");
        ClientState.setAccountId(accountDetails.getInt("accountId"));
        ClientState.setProvider(accountDetails.get("provider"));
        ClientState.setNextAdventurePointSeconds(nextAdventurePointSeconds, adventurePoints);
        ClientState.setAdventurePoints(adventurePoints);
        
        int numMessages = result.getInt("numMessages");
        MessagesLink.getInstance().setNumMessages(numMessages);
        
        String tipText = result.get("tip");
        TipPanel.setTipText(tipText);
    }
}
