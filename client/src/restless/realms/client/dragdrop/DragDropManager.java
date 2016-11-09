package restless.realms.client.dragdrop;

import restless.realms.client.belt.Quickslot;
import restless.realms.client.combat.MobPanel;
import restless.realms.client.playercharacter.PlayerCharacterPanel;

import com.google.gwt.user.client.ui.Image;

public class DragDropManager {
    private static ActionDragController drag;
    
    public static void init() {
        assert drag == null;
        drag = new ActionDragController();
    }

    public static void makeDraggable(Image image) {
        drag.makeDraggable(image);
    }
    
    public static void makeNotDraggable(Image image) {
        drag.makeNotDraggable(image);
    }
    
    public static void registerDroppable(Quickslot quickslot) {
        QuickslotDropController dropController = new QuickslotDropController(quickslot);
        drag.registerDropController(dropController);
    }

    public static void registerAttachmentDroppable(Quickslot quickslot) {
        AttachmentDropController dropController = new AttachmentDropController(quickslot);
        drag.registerDropController(dropController);
    }

    public static void registerPvpDefenseDroppable(Quickslot quickslot) {
        PvpDefenseDropController dropController = new PvpDefenseDropController(quickslot);
        drag.registerDropController(dropController);
    }

    public static void registerStashDroppable(Quickslot quickslot) {
        StashDropController dropController = new StashDropController(quickslot);
        drag.registerDropController(dropController);
    }

    public static void registerDroppable(MobPanel mobPanel) {
        ActivateDropController dropController = new ActivateDropController(mobPanel);
        drag.registerDropController(dropController);
    }

    public static void registerDroppable(PlayerCharacterPanel pcp) {
        EquipmentDropController dropController = new EquipmentDropController(pcp);
        drag.registerDropController(dropController);
    }
}
