package restless.realms.client.combat;

import restless.realms.client.ClientState;
import restless.realms.client.console.CombatLogPanel;
import restless.realms.client.playercharacter.PlayerCharacterHandler;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

public class CombatLogger implements PlayerCharacterHandler {
    @Override
    public void applyEffects(JsArray<ScriptObject> actions, JsArrayString actionNames) {
        for(int i = 0; i < actions.length(); i++) {
            ScriptObject action = actions.get(i);
            String actionName = actionNames.get(i);
            int source = action.getInt("source");
            int target = action.getInt("target");
            
            String message;
            if(source == 0) {
                String targetName = target == 0 ? "yourself" : CombatPerspective.getName(target);
                message = "You use " + actionName + " on " + targetName + ".";
            } else {
                String targetName = target == source ? "themself" : target == 0 ? "you" : CombatPerspective.getName(target);
                message = CombatPerspective.getName(source) + " uses " + actionName + " on " + targetName + ".";
            }
            CombatLogPanel combatLogPanel = (CombatLogPanel)ClientState.getLayout().getPanel("combatlog");
            combatLogPanel.add(message, true);
            
            JsArray<ScriptObject> effectResults = action.getArray("results");
            for(int j = 0; j < effectResults.length(); j++) {
                ScriptObject effectResult = effectResults.get(j);
                log(effectResult);
            }
        }
    }

    private void log(ScriptObject effect) {
        String message;
        String type = effect.get("type");
        int source = effect.getInt("source");
        int target = effect.getInt("target");
        int quantity = effect.getInt("quantity");
        //int additionalRounds = effect.getInt("additionalRounds");
        
        if(type.equals("HEALING")) {
            if(source == 0) {
                message = "You regain " + quantity + " hitpoints.";
            } else {
                message = CombatPerspective.getName(source) + " regains " + quantity + " hitpoints.";
            }
        } else if(type.equals("REPLENISH")) {
            if(source == 0) {
                message = "You regain " + quantity + " mana.";
            } else {
                message = CombatPerspective.getName(source) + " regains " + quantity + " mana.";
            }
        } else if(type.equals("DEATH")) {
            if(source == 0) {
                message = "You have killed ";
            } else {
                message = CombatPerspective.getName(source) + " has killed ";
            }
            if(target == 0) {
                message += "you";
                if(source == 0) {
                    message += "rself";
                }
            } else {
                message += CombatPerspective.getName(target);
            }
            message += ".";
        } else if(type.equals("STUN")) {
            if(source == 0) {
                message = "You have stunned ";
            } else {
                message = CombatPerspective.getName(source) + " has stunned ";
            }
            if(target == 0) {
                message += "you";
            } else {
                message += CombatPerspective.getName(target);
            }
            message += ".";
        } else {
            if(source == 0) {
                message = "You hit ";
            } else {
                message = CombatPerspective.getName(source) + " hits ";
            }

            if(target == 0) {
                message += "you";
            } else {
                message += CombatPerspective.getName(target);
            }
            
            message += " for " + quantity;
            if(!type.equals("PHYSICAL")) {
                message += " " + type.toLowerCase();
            }
            message += " damage.";
        }
        message = "<span class='" + type.toLowerCase() + "'>" + message + "</span>";
        CombatLogPanel combatLogPanel = (CombatLogPanel)ClientState.getLayout().getPanel("combatlog");
        combatLogPanel.add(message, true);
    }
    
    @Override
    public void onPlayerCharacter(ScriptObject playerCharacter) {
        //noop
    }

    @Override
    public void onAdventurePoints(int adventurePoints) {
        //noop
    }
}
