package restless.realms.client.dragdrop;

import restless.realms.client.action.ActionDetailPanel;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class ActionDragController extends PickupDragController {
    public ActionDragController() {
        super(RootPanel.get(), false);
        this.setBehaviorDragProxy(true);
        this.setBehaviorDragStartSensitivity(3);
    }
    
    @Override
    protected Widget newDragProxy(DragContext context) {
        ActionDetailPanel.getInstance().clear();
        Image i = (Image)context.draggable;
        Image clone = new Image(i.getUrl(), i.getOriginLeft(), i.getOriginTop(), i.getWidth(), i.getHeight());
        return clone;
    }
}
