package restless.realms.client.dragdrop;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.action.ActionIcon;
import restless.realms.client.combat.CombatPanel;
import restless.realms.client.combat.MobPanel;
import restless.realms.client.perspective.MessageType;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;

public class ActivateDropController extends SimpleDropController {
    private final MobPanel mobPanel;

    public ActivateDropController(MobPanel mobPanel) {
        super(mobPanel);
        this.mobPanel = mobPanel;
    }

    @Override
    public void onDrop(DragContext context) {
        ActionIcon source = (ActionIcon)context.draggable;
        ClientManager.send(MessageType.ACTIVATE, source.getType(), source.getAction().getInt("id"));
        super.onDrop(context);
    }
    
    @Override
    public void onEnter(DragContext context) {
        CombatPanel combatPanel = (CombatPanel)ClientState.getLayout().getPanel("combat");
        if(mobPanel.isVisible() && mobPanel.getHitpoints().getValue() > 0) {
            combatPanel.setTargetSelectionIndex(mobPanel.getIndex(), false);
        }
        super.onEnter(context);
    }
}
