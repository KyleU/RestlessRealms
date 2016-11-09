package restless.realms.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import restless.realms.client.console.ConsoleChannel;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.inventory.InventoryHandler;
import restless.realms.client.layout.MainLayout;
import restless.realms.client.playercharacter.PlayerCharacterHandler;
import restless.realms.client.skill.SkillsHandler;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.widget.CountdownClock;
import restless.realms.client.worldmap.MapLocation;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

public class ClientState {
    private static MainLayout layout;

    private static String sessionId;

    private static List<PlayerCharacterHandler> playerCharacterHandlers = new ArrayList<PlayerCharacterHandler>();
    private static List<InventoryHandler> inventoryHandlers = new ArrayList<InventoryHandler>();
    private static List<SkillsHandler> skillsHandlers = new ArrayList<SkillsHandler>();
    
    private static boolean showHelp;
    private static Integer adventureId;
    private static String adventureStatus;
    private static String adventureType;
    
    private static int accountId;
    private static String provider;
    private static List<MapLocation> mapLocations;

    private ClientState() {
    }
    
    public static MainLayout getLayout() {
        return layout;
    }
    public static void setLayout(MainLayout layout) {
        ClientState.layout = layout;
    }
    
    public static String getSessionId() {
        return sessionId;
    }
    public static void setSessionId(String sessionId) {
        ClientState.sessionId = sessionId;
    }
    
    // PlayerCharacter
    public static void addPlayerCharacterHandler(PlayerCharacterHandler handler) {
        assert !playerCharacterHandlers.contains(handler);
        playerCharacterHandlers.add(handler);
    }
    
    public static void setPlayerCharacter(ScriptObject playerCharacter) {
        for(PlayerCharacterHandler handler : playerCharacterHandlers) {
            handler.onPlayerCharacter(playerCharacter);
        }
    }
    
    public static void setAdventurePoints(int adventurePoints) {
        for(PlayerCharacterHandler handler : playerCharacterHandlers) {
            handler.onAdventurePoints(adventurePoints);
        }
    }

    public static void applyEffects(JsArray<ScriptObject> actions, JsArrayString actionNames) {
        for(PlayerCharacterHandler handler : playerCharacterHandlers) {
            handler.applyEffects(actions, actionNames);
        }
    }
    
    // Inventory
    public static void addInventoryHandler(InventoryHandler handler) {
        assert !inventoryHandlers.contains(handler);
        inventoryHandlers.add(handler);
    }
    public static void setCurrency(int currency) {
        for(InventoryHandler handler : inventoryHandlers) {
            handler.onCurrency(currency);
        }
    }
    public static void setTokens(int tokens) {
        for(InventoryHandler handler : inventoryHandlers) {
            handler.onTokens(tokens);
        }
    }
    public static void setItems(JsArray<ScriptObject> items) {
        for(InventoryHandler handler : inventoryHandlers) {
            handler.onItems(items);
        }
    }
    public static void setPerks(JsArray<ScriptObject> perks) {
        for(InventoryHandler handler : inventoryHandlers) {
            handler.onPerks(perks);
        }
    }
    
    // Skills
    public static void addSkillsHandler(SkillsHandler handler) {
        assert !skillsHandlers.contains(handler);
        skillsHandlers.add(handler);
    }
    public static void setSkills(JsArray<ScriptObject> skills) {
        for(SkillsHandler handler : skillsHandlers) {
            handler.onSkills(skills);
        }
    }
    
    public static boolean isShowHelp() {
        return showHelp;
    }
    public static void setShowHelp(boolean showHelp) {
        ClientState.showHelp = showHelp;
    }
    
    public static Integer getAdventureId() {
        return adventureId;
    }
    public static void setAdventureId(Integer adventureId) {
        ClientState.adventureId = adventureId;
    }

    public static String getAdventureStatus() {
        return adventureStatus;
    }
    public static void setAdventureStatus(String adventureStatus) {
        ClientState.adventureStatus = adventureStatus;
    }

    public static String getAdventureType() {
        return adventureType;
    }
    public static void setAdventureType(String adventureType) {
        ClientState.adventureType = adventureType;
    }

    public static List<MapLocation> getMapLocations() {
        return mapLocations;
    }
    public static void setMapLocations(JsArray<ScriptObject> locations) {
        mapLocations = new ArrayList<MapLocation>();
        for(int i = 0; i < locations.length(); i++) {
            ScriptObject location = locations.get(i);
            MapLocation l = new MapLocation(location);
            mapLocations.add(l);
        }
    }

    public static void setAccountId(int accountId) {
        assert accountId > 0;
        ClientState.accountId = accountId;
    }
    public static int getAccountId() {
        return accountId;
    }

    public static void setProvider(String provider) {
        assert provider.length() > 0;
        ClientState.provider = provider;
    }
    public static String getProvider() {
        return provider;
    }

    public static void setNextAdventurePointSeconds(int nextAdventurePointSeconds, int currentAps) {
        Date date = new Date();
        date.setTime(date.getTime() + (nextAdventurePointSeconds * 1000));
        CountdownClock.getInstance().setDate(date);
        
        //int seconds = nextAdventurePointSeconds % 60;
        int minutes = (nextAdventurePointSeconds / 60) % 60;
        int hours = (nextAdventurePointSeconds / 60 / 60);

        if(currentAps < 5) {
            ConsoleUtils.log(ConsoleChannel.Help, "You have " + currentAps + " adventure points, and will gain another in " + hours + " hours and " + minutes + " minutes.");
        }
    }
}