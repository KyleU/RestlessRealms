package restless.realms.client.dragdrop;

import restless.realms.client.ClientManager;
import restless.realms.client.action.ActionIcon;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.playercharacter.PlayerCharacterPanel;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;

public class EquipmentDropController extends SimpleDropController {
    private PlayerCharacterPanel pcp;
    
    public EquipmentDropController(PlayerCharacterPanel pcp) {
        super(pcp);
        this.pcp = pcp;
    }

    @Override
    public void onDrop(DragContext context) {
        ActionIcon source = (ActionIcon)context.draggable;
        
        int left = context.desiredDraggableX - pcp.getAbsoluteLeft();
        int slot = left > 120 ? 1 : 0;
        ClientManager.send(MessageType.EQUIP, source, slot);
        super.onDrop(context);
    }
    
    @Override
    public void onEnter(DragContext context) {
        super.onEnter(context);
    }
    
    @Override
    public void onLeave(DragContext context) {
        super.onLeave(context);
    }
}
