package restless.realms.client.dragdrop;

import restless.realms.client.ClientManager;
import restless.realms.client.action.ActionIcon;
import restless.realms.client.belt.Quickslot;
import restless.realms.client.perspective.MessageType;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;

public class QuickslotDropController extends SimpleDropController {
    private final Quickslot quickslot;

    public QuickslotDropController(Quickslot quickslot) {
        super(quickslot);
        this.quickslot = quickslot;
    }

    @Override
    public void onDrop(DragContext context) {
        ActionIcon source = (ActionIcon)context.draggable;
        ClientManager.send(MessageType.QUICKSLOTS, quickslot, source);
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
