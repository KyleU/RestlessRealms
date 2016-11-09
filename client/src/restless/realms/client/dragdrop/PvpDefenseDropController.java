package restless.realms.client.dragdrop;

import restless.realms.client.action.ActionIcon;
import restless.realms.client.belt.Quickslot;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.pvp.PvpDefensesPanel;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;

public class PvpDefenseDropController extends SimpleDropController {
    private final Quickslot quickslot;

    public PvpDefenseDropController(Quickslot quickslot) {
        super(quickslot);
        this.quickslot = quickslot;
    }

    @Override
    public void onDrop(DragContext context) {
        ActionIcon source = (ActionIcon)context.draggable;
        if(source.getType().equals("skill")) {
            quickslot.setActionIcon(source);
            PvpDefensesPanel.update();
        } else {
            ConsoleUtils.error("Only skills may be added to your duel defenses.");
        }
        super.onDrop(context);
    }
    
    @Override
    public void onEnter(DragContext context) {
        quickslot.onHover();
        super.onEnter(context);
    }
    
    @Override
    public void onLeave(DragContext context) {
        quickslot.onLeave();
        super.onLeave(context);
    }
}
