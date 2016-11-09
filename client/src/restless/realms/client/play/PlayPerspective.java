package restless.realms.client.play;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.achievement.AchievementNotifications;
import restless.realms.client.adventure.AdventureCallback;
import restless.realms.client.audio.AudioManager;
import restless.realms.client.belt.Quickslot;
import restless.realms.client.city.CityPanel;
import restless.realms.client.console.CombatLogPanel;
import restless.realms.client.console.ConsolePanel;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.dragdrop.DragDropManager;
import restless.realms.client.guide.GuidePanel;
import restless.realms.client.guide.IntroductionPanel;
import restless.realms.client.inventory.InventoryPanel;
import restless.realms.client.item.ItemShopPanel;
import restless.realms.client.layout.PanelLocation;
import restless.realms.client.leaderboard.LeaderboardPanel;
import restless.realms.client.party.PartyPanel;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.perspective.Perspective;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.playercharacter.PlayerCharacterPanel;
import restless.realms.client.quest.QuestBoardPanel;
import restless.realms.client.quest.QuestCompletionPanel;
import restless.realms.client.quest.QuestListPanel;
import restless.realms.client.report.FeedbackPanel;
import restless.realms.client.room.LevelUpPanel;
import restless.realms.client.room.LootPanel;
import restless.realms.client.skill.TrainerPanel;
import restless.realms.client.util.AuditManager;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.DialogPanel;
import restless.realms.client.widget.PurchasePanel;
import restless.realms.client.worldmap.AdventureIntroductionPanel;
import restless.realms.client.worldmap.MapLocation;
import restless.realms.client.worldmap.WorldMapPanel;

import com.google.gwt.user.client.Event.NativePreviewEvent;

public class PlayPerspective extends Perspective {
    private QuestBoardPanel questBoardPanel;
    private TrainerPanel trainerPanel;
    private ItemShopPanel itemShopPanel;

    public PlayPerspective() {
        super("play");
    }

    @Override
    public void init() {
        super.init();
        DragDropManager.init();
        
        AudioManager.initImages();
        
        AchievementNotifications.init();
        
        PlayerCharacterCache.getInstance().init();

        assert(ClientState.getLayout() != null);

        ClientState.getLayout().addPanel(PanelLocation.BOTTOMLEFT, "player", new PlayerCharacterPanel());
        ClientState.getLayout().addPanel(PanelLocation.BOTTOMLEFT, "party", new PartyPanel());
        
        ClientState.getLayout().addPanel(PanelLocation.BOTTOMCENTER, "inventory", new InventoryPanel());

        ClientState.getLayout().addPanel(PanelLocation.BOTTOMRIGHT, "console", new ConsolePanel());
        ClientState.getLayout().addPanel(PanelLocation.BOTTOMRIGHT, "combatlog", new CombatLogPanel());
        ClientState.getLayout().addPanel(PanelLocation.BOTTOMRIGHT, "feedback", new FeedbackPanel());

        ClientState.getLayout().addPanel(PanelLocation.MAIN, "introduction", new IntroductionPanel());
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "dialog", new DialogPanel());
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "worldmap", new WorldMapPanel());
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "leaderboard", new LeaderboardPanel());
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "adventureintro", new AdventureIntroductionPanel());
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "guide", new GuidePanel());
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "quests", new QuestListPanel());
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "questcompletion", new QuestCompletionPanel());
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "loot", new LootPanel());
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "levelup", new LevelUpPanel());
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "noadventures", new NoAdventuresPanel());
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "city", new CityPanel());

        questBoardPanel = new QuestBoardPanel();
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "questboard", questBoardPanel);
        
        trainerPanel = new TrainerPanel();
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "trainer", trainerPanel);
        
        itemShopPanel = new ItemShopPanel();
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "itemshop", itemShopPanel);
                
        PurchasePanel.addLink();
    }

    @Override
    public void onEnter() {
        super.onEnter();
        ClientState.getLayout().showPanel("player");
        ClientState.getLayout().showPanel("inventory");
        ClientState.getLayout().showPanel("console");

        ClientState.getLayout().showNavAndBelt();
        ClientState.getLayout().getBeltPanel().clearOverlays();

        if(ClientState.isShowHelp()) {
            ClientState.getLayout().showPanel("introduction");
        } else if(ClientState.getAdventureId() == null) {
            WorldMapPanel worldMapPanel = (WorldMapPanel)ClientState.getLayout().getPanel("worldmap");
            worldMapPanel.show();
        } else {
            if(ClientState.getAdventureId() > 0) {
                if(ClientState.getAdventureStatus().equals("ACTIVE")) {
                    ServiceManager.call("adventure", "resume", new AdventureCallback());
                } else {
                    ConsoleUtils.error("Unhandled status " + ClientState.getAdventureStatus() + " for adventure " + ClientState.getAdventureId() + ".");
                }
            } else if(ClientState.getAdventureId() < 0) {
                ClientManager.setPerspective("combat");
            } else {
                assert false;
            }
        }
        
        AudioManager.playMusic("theme");
    }

    @Override
    public void onLeave() {

    }

    @Override
    public void onMessage(MessageType type, Object... params) {
        switch(type) {
        case WINDOW_CLOSE:
            if(ClientState.isShowHelp()) {
                for(MapLocation loc : ClientState.getMapLocations()) {
                    if("tutorial".equals(loc.getId())) {
                        AdventureIntroductionPanel.show(loc);
                    }
                }
            } else {
                WorldMapPanel worldMapPanel = (WorldMapPanel)ClientState.getLayout().getPanel("worldmap");
                worldMapPanel.show();
            }
            break;
        case ENTER_LOCATION:
            String key = (String)params[0];
            if("city".equals(key) || "outpost".equals(key)) {
                ClientState.getLayout().showPanel("city");
                AuditManager.audit("city", "index");
                ClientState.getLayout().getMainNavigation().activate(null);
            } else if("questboard".equals(key)) {
                questBoardPanel.show();
            } else if("guildhall".equals(key)) {
                ConsoleUtils.error("The guild hall isn't quite ready yet.");
            } else if("home".equals(key)) {
                HomePanel.show();
            } else if("trainer".equals(key)) {
                trainerPanel.show();
            } else if("auction".equals(key)) {
                ConsoleUtils.error("The auction house isn't quite ready yet.");
            } else if("wares".equals(key)) {
                ConsoleUtils.error("The special wares store isn't quite ready yet.");
            } else if("news".equals(key)) {
                ConsoleUtils.error("The town crier isn't quite ready yet.");
            } else if("itemshop".equals(key)) {
                itemShopPanel.show((String)params[1]);
            } else if("blackmarket".equals(key)) {
                ClientState.getLayout().showPanel("blackmarket");
                AuditManager.audit("city", "blackmarket");
            } else {
                startAdventure(key);
            }
            break;
        case KEY_PRESS:
            NativePreviewEvent event = (NativePreviewEvent)params[0];
            if(!ConsoleUtils.onKeyPress(event)) {
                int keyCode = event.getNativeEvent().getKeyCode();
                if(keyCode == 87) {
                    ClientManager.send(MessageType.WORLD_MAP_OPEN);
                } else if(keyCode == 72 /* H */) {
                    ClientManager.send(MessageType.GUIDE_OPEN);
                } else if(keyCode == 81) {
                    ClientManager.send(MessageType.QUESTS_OPEN);
                } else if(keyCode == 27) {
                    ClientManager.send(MessageType.WINDOW_CLOSE);
                } else if(48 < keyCode && keyCode < 57){
                    int index = keyCode - 49;
                    Quickslot quickslot = ClientState.getLayout().getBeltPanel().getQuickslots()[index];
                    if(quickslot.getId() > 0) {
                        ClientManager.send(MessageType.ACTIVATE, quickslot.getType(), quickslot.getId());
                    }
                }
            }
            break;
        default:
            super.onMessage(type, params);
            break;
        }
    }

    private void startAdventure(final String key) {
        if(ClientState.getAdventureId() == null) {
            ServiceManager.call("adventure", "start", new AdventureCallback(), "type", key);
        } else {
            ServiceManager.call("adventure", "abandon", new ServiceCallback() {
                @Override
                public void onSuccess(ScriptObject result) {
                    ServiceManager.call("adventure", "start", new AdventureCallback(), "type", key);                    
                }
            });
        }
    }
}