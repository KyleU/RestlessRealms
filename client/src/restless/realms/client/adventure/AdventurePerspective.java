package restless.realms.client.adventure;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.audio.AudioManager;
import restless.realms.client.belt.Quickslot;
import restless.realms.client.console.ConsoleChannel;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.layout.MainLayout;
import restless.realms.client.layout.PanelLocation;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.perspective.Perspective;
import restless.realms.client.puzzle.PuzzlePanel;
import restless.realms.client.room.ExitPanel;
import restless.realms.client.room.LootPanel;
import restless.realms.client.room.ShrinePanel;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event.NativePreviewEvent;

public class AdventurePerspective extends Perspective {
    private AdventurePanel adventurePanel;
    
    private LootPanel lootPanel;
    private PuzzlePanel puzzlePanel;
    private ShrinePanel shrinePanel;
    private ExitPanel exitPanel;
    
    private ScriptObject adventure;
    private boolean pendingMovement = false;
    private boolean adventureHasFocus;
    
    public AdventurePerspective() {
        super("adventure");
    }
    
    @Override
    public void init() {
        super.init();
        assert(ClientState.getLayout() != null);
        adventurePanel = new AdventurePanel();
        ClientState.setShowHelp(false);
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "adventure", adventurePanel);

        lootPanel = (LootPanel)ClientState.getLayout().getPanel("loot");
        
        puzzlePanel = new PuzzlePanel();
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "puzzle", puzzlePanel);
        shrinePanel = new ShrinePanel();
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "shrine", shrinePanel);
        exitPanel = new ExitPanel();
        ClientState.getLayout().addPanel(PanelLocation.MAIN, "exit", exitPanel);
    }
    
    @Override
    public void onEnter() {
        super.onEnter();
        MainLayout layout = ClientState.getLayout();
        layout.showPanel("adventure");
        layout.getMainNavigation().activate(null);
        layout.getBeltPanel().clearOverlays();
        adventureHasFocus = true;
        
        AudioManager.playMusic("adventure");
    }
    
    @Override
    public void onLeave() {
        adventureHasFocus = false;
    }
    
    @Override
    public void onMessage(MessageType type, final Object... params) {
        switch(type) {
        case WINDOW_CLOSE:
            ClientState.getLayout().showPanel("adventure");
            ClientState.getLayout().getMainNavigation().activate(null);
            adventureHasFocus = true;
            break;
        case ADVENTURE:
            adventure = (ScriptObject)params[0];
            ScriptObject activeRoom = (ScriptObject)params[1];
            ClientState.setAdventureId(adventure.getInt("id"));
            ClientState.setAdventureType(adventure.get("type"));
            ClientState.setAdventureStatus(adventure.get("status"));
            adventurePanel.setAdventure(adventure);
            activateRoom(activeRoom);
            break;
        case MOVE:
            move((Integer)params[0]);
            break;
        case KEY_PRESS:
            NativePreviewEvent event = (NativePreviewEvent)params[0];
            onKeyPress(event);
            break;
        case ENTER_LOCATION:
            adventureHasFocus = false;
            ServiceManager.call("adventure", "abandon", new ServiceCallback() {
                @Override
                public void onSuccess(ScriptObject result) {
                    ClientState.setPlayerCharacter(result);
                    ClientState.setAdventureStatus(null);
                    ClientState.setAdventureId(null);
                    ClientManager.setPerspective("play");
                    ClientManager.send(MessageType.ENTER_LOCATION, params);
                }
            });
            break;
        case WORLD_MAP_OPEN:
        case GUIDE_OPEN:
        case QUESTS_OPEN:
        case LEADERBOARD_OPEN:
            adventureHasFocus = false;
            super.onMessage(type, params);
            break;
        default:
            super.onMessage(type, params);
            break;
        }
    }

    private void move(final Integer direction) {
        if(pendingMovement) {
            ConsoleUtils.log(ConsoleChannel.Error, "You still have a request pending. If you're seeing this message repeatedly, refresh the page.");
        } else {
            int[] neighbors = adventurePanel.getMapPanel().getNeighbors(adventure.getInt("activeRoomIndex"));
            if(neighbors[direction] == -1) {
                ConsoleUtils.error("Invalid direction");
                return;
            }
            ServiceCallback callback = new ServiceCallback(){
                @Override
                public void onFailure(String code, String message) {
                    adventurePanel.setActiveRoomIndex(null, adventure.getInt("activeRoomIndex"), null, null);
                    super.onFailure(code, message);
                    pendingMovement = false;
                }

                @Override
                public void onSuccess(ScriptObject ret) {
                    activateRoom(ret);
                    pendingMovement = false;
                }
            };

            pendingMovement = true;
            adventurePanel.disableCompass();
            
            ServiceManager.call("adventure", "move", callback, "roomIndex", neighbors[direction]);
        }
    }

    private void activateRoom(ScriptObject activeRoom) {
        String previousRoomState = activeRoom.get("previousRoomState");
        
        int index = activeRoom.getInt("index");
        String type = activeRoom.get("type");
        String state = activeRoom.get("state");

        adventure.set("activeRoomIndex", index);
        adventurePanel.setActiveRoomIndex(previousRoomState, index, type, state);
        
        if("ACTIVE".equals(state)) {
            adventureHasFocus = false;
            if("INTRO".equals(type)) {
                adventureHasFocus = true;
            } else if("EMPTY".equals(type)) {
                adventureHasFocus = true;
            } else if("COMBAT".equals(type)) {
                ClientManager.setPerspective("combat");
            } else if("PUZZLE".equals(type)) {
                puzzlePanel.show(activeRoom);
            } else if("LOOT".equals(type)) {
                lootPanel.show("LOOT");
            } else if("SHRINE".equals(type)) {
                shrinePanel.show();
            } else if("EXIT".equals(type)) {
                exitPanel.show(adventure.get("type"));
            } else {
                assert false : type;
            }
        } else if("COMPLETED".equals(state)) {
            //no op
        } else {
            assert false;
        }
    }

    private void onKeyPress(NativePreviewEvent event) {
        if(!ConsoleUtils.onKeyPress(event)) {
            int keyCode = event.getNativeEvent().getKeyCode();
            if(keyCode == 87) {
                ClientManager.send(MessageType.WORLD_MAP_OPEN);
            } else if(keyCode == 71) {
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
            } else if(adventureHasFocus) {
                if(keyCode == KeyCodes.KEY_UP) {
                    ClientManager.send(MessageType.MOVE, 0);
                    event.cancel();
                    event.getNativeEvent().preventDefault();
                    event.getNativeEvent().stopPropagation();
                } else if(keyCode == KeyCodes.KEY_RIGHT) {
                    ClientManager.send(MessageType.MOVE, 1);
                    event.cancel();
                    event.getNativeEvent().preventDefault();
                    event.getNativeEvent().stopPropagation();
                } else if(keyCode == KeyCodes.KEY_DOWN) {
                    ClientManager.send(MessageType.MOVE, 2);
                    event.cancel();
                    event.getNativeEvent().preventDefault();
                    event.getNativeEvent().stopPropagation();
                } else if(keyCode == KeyCodes.KEY_LEFT) {
                    ClientManager.send(MessageType.MOVE, 3);
                    event.cancel();
                    event.getNativeEvent().preventDefault();
                    event.getNativeEvent().stopPropagation();
                }
            } else {
                if(keyCode == 'L') {
                    if(lootPanel.isActive()) {
                        lootPanel.lootAll();
                    }
                }
            }
        }
    }
}
