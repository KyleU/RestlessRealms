package restless.realms.client.playercharacter;

import restless.realms.client.util.ScriptObject;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

public interface PlayerCharacterHandler {
    void onPlayerCharacter(ScriptObject playerCharacter);
    void onAdventurePoints(int adventurePoints);
    void applyEffects(JsArray<ScriptObject> actions, JsArrayString actionNames);
}