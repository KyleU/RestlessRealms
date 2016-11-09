package restless.realms.client.dragdrop;

import restless.realms.client.action.ActionIcon;
import restless.realms.client.belt.Quickslot;
import restless.realms.client.console.ConsoleUtils;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;

public class AttachmentDropController extends SimpleDropController {
    private final Quickslot quickslot;

    public AttachmentDropController(Quickslot quickslot) {
        super(quickslot);
        this.quickslot = quickslot;
    }

    @Override
    public void onDrop(DragContext context) {
        ActionIcon source = (ActionIcon)context.draggable;
        if(source.getType().equals("item")) {
            String itemType = source.getAction().get("type");
            if("CONSUMABLE".equals(itemType)) {
                ConsoleUtils.error("You may not attach consumable items to mail.");
            } else {
                quickslot.setActionIcon(source);
            }
        } else {
            ConsoleUtils.error("Only items may be attached to mail.");
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
