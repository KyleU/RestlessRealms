package restless.realms.client;

import java.util.HashMap;
import java.util.Map;

import restless.realms.client.adventure.AdventurePerspective;
import restless.realms.client.combat.CombatPerspective;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.perspective.Perspective;
import restless.realms.client.play.PlayPerspective;

public class ClientManager {
    private static Map<String, Perspective> perspectives;
    private static Perspective activePerspective;
    
    public static void init() {
        assert(perspectives == null);
        Perspective perspective;
        perspectives = new HashMap<String, Perspective>();
        
        perspective = new PlayPerspective();
        perspectives.put(perspective.getCode(), perspective);
        perspective = new AdventurePerspective();
        perspectives.put(perspective.getCode(), perspective);
        perspective = new CombatPerspective();
        perspectives.put(perspective.getCode(), perspective);
    }

    public static void setPerspective(final String code) {
        Perspective p = perspectives.get(code);
        assert p != null;

        if(activePerspective != null) {
            activePerspective.onLeave();
        }
        activePerspective = p;
        p.onEnter();
    }

    public static String getActivePerspective() {
        return activePerspective == null ? null : activePerspective.getCode();
    }
    
    public static void send(MessageType type, Object... params) {
        assert activePerspective != null : type;
        if(activePerspective != null) {
            activePerspective.onMessage(type, params);
        }
    }
}
