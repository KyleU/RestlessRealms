package restless.realms.client.dragdrop;

import restless.realms.client.ServiceManager;
import restless.realms.client.action.ActionIcon;
import restless.realms.client.belt.Quickslot;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;

public class StashDropController extends SimpleDropController {
    private final Quickslot quickslot;

    public StashDropController(Quickslot quickslot) {
        super(quickslot);
        this.quickslot = quickslot;
    }

    @Override
    public void onDrop(DragContext context) {
        final ActionIcon source = (ActionIcon)context.draggable;
        if(source.getType().equals("item")) {
            String itemType = source.getAction().get("type");
            if(itemType.equals("QUEST")) {
                throw new IllegalArgumentException("You may not put quest items in your stash.");
            }
            //quickslot.setActionIcon(source);
            ServiceManager.call("stash", "deposit", new ServiceCallback() {
                    @Override
                    public void onSuccess(ScriptObject result) {
                        PlayerCharacterCache.getInstance().removeItem(source.getAction().getInt("id")); 
                    }
                }, 
                "item", source.getAction().get("id"), 
                "index", -(quickslot.getIndex() - 100)
            );
        } else {
            ConsoleUtils.error("Only items may be put into the stash.");
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
