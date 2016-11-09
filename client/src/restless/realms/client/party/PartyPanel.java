package restless.realms.client.party;

import java.util.ArrayList;
import java.util.List;

import restless.realms.client.ClientState;
import restless.realms.client.console.command.AdminCommand;
import restless.realms.client.playercharacter.PlayerCharacterHandler;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;

public class PartyPanel extends WindowPanel<AbsolutePanel> implements PlayerCharacterHandler {
    private PartyMemberPanel[] partyMembers;
    private int numPartyMembers;
    
    public PartyPanel() {
	    super("party", new AbsolutePanel(), "Adventuring Party", null);
        ClientState.addPlayerCharacterHandler(this);

        if(AdminCommand.isAdmin()) {
            partyMembers = new PartyMemberPanel[4];
            partyMembers[0] = new PartyMemberPanel();
            body.add(partyMembers[0], 0, 0);
            partyMembers[1] = new PartyMemberPanel();
            body.add(partyMembers[1], 0, 51);
            partyMembers[2] = new PartyMemberPanel();
            body.add(partyMembers[2], 0, 102);
            partyMembers[3] = new PartyMemberPanel();
            body.add(partyMembers[3], 0, 153);
        } else {
            Label comingSoon = new Label("Pets or multiplayer parties might be coming soon. I'm going as fast as I can, you animals!");
            body.add(comingSoon, 5, 0);
        }
        
        addTitleIcon(170, "img/icon/filters.png", 14 * 16, "Portrait", "player");
        addTitleIcon(190, "img/icon/filters.png", 16 * 16, "Party", null);
        addTitleIcon(210, "img/icon/filters.png", 15 * 16, "Achievements", "achievement");

	}
    
    public void setPartyMembers(JsArray<ScriptObject> effectTargets) {
        for(int i = 0; i < 4; i++) {
            partyMembers[i].setPartyMember(effectTargets.length() > i ? effectTargets.get(i) : null);
        }
    }

    @Override
    public void onPlayerCharacter(ScriptObject playerCharacter) {

    }
	
	@Override
	public void onAdventurePoints(int adventurePoints) {
	    // no op
	}

	@Override
	@SuppressWarnings("unchecked")
	public void applyEffects(JsArray<ScriptObject> actions, JsArrayString actionNames) {
        List<ScriptObject>[] effectResultsByAllyIndex = new List[4];

	    //body.clearRecurringEffects();
        for(int i = 0; i < actions.length(); i++) {
            ScriptObject action = actions.get(i);
            JsArray<ScriptObject> effectResults = action.getArray("results");
            
    	    for(int j = 0; j < effectResults.length(); j++) {
                final ScriptObject effectResult = effectResults.get(j);
                int effectTargetIndex = effectResult.getInt("target");
                if(effectTargetIndex >= 0 && effectTargetIndex < numPartyMembers) {
                    if(effectResultsByAllyIndex[effectTargetIndex] == null) {
                        effectResultsByAllyIndex[effectTargetIndex] = new ArrayList<ScriptObject>();
                    }
                    effectResultsByAllyIndex[effectTargetIndex].add(effectResult);
                }                    
            }
        }

        for(int i = 0; i < effectResultsByAllyIndex.length; i++) {
            if(effectResultsByAllyIndex[i] != null) {
                PartyMemberPanel targetPanel = partyMembers[i];
                targetPanel.apply(effectResultsByAllyIndex[i]);
            }
        }
	}
}
