package restless.realms.client.belt;

import restless.realms.client.ClientManager;
import restless.realms.client.action.ActionDetailPanel;
import restless.realms.client.action.ActionIcon;
import restless.realms.client.console.ConsoleChannel;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.dragdrop.DragDropManager;
import restless.realms.client.layout.SizeConstants;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

public class Quickslot extends Composite {
    AbsolutePanel body;
    private final int index;
    private ActionIcon icon;
    private HTML overlay;
    private HTML hover;

    private ClickHandler clickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            if(index == -1) {
                ConsoleUtils.log(ConsoleChannel.Help, "Drag an item here to attach it to this message.");
            } else if(index == -2) {
                ConsoleUtils.log(ConsoleChannel.Help, "Drag a skill here to add it to your defenses.");
            } else if(index == -3) {
                ConsoleUtils.log(ConsoleChannel.Help, "The final slot of your defenses is locked to your attack skill.");
            } else if(index <= -500) {
                ConsoleUtils.log(ConsoleChannel.Help, "Drag an item here to add it to your guild vault.");
            } else if(index <= -100) {
                if(getId() == 0) {
                    ConsoleUtils.log(ConsoleChannel.Help, "Drag an item here to add it to your account stash.");
                } else {
                    ConsoleUtils.error("!!!");
                }
            } else {
                if(getId() == 0) {
                    ConsoleUtils.log(ConsoleChannel.Help, "Drag a skill or item here to equip it.");
                } else {
                    ClientManager.send(MessageType.ACTIVATE, getType(), getId());
                }
            }
        }
    };

    public Quickslot(int index) {
        body = new AbsolutePanel() {
            @Override
            public void onBrowserEvent(Event event) {
                handleBrowserEvent(event);
            }
        };
        initWidget(body);
        this.index = index;
        this.setStylePrimaryName("quickslot");

        body.sinkEvents(Event.MOUSEEVENTS);
        
        icon = new ActionIcon();
        icon.setStylePrimaryName("quicksloticon");
        icon.addClickHandler(clickHandler);
        if(index >= 0) {
            icon.getElement().setId("quickslot" + index);
        }
        body.add(icon, 0, 0);

        if(index >= 0) {
            overlay = new HTML();
            overlay.setStylePrimaryName("overlay");
            overlay.setVisible(false);
            overlay.addClickHandler(clickHandler);
            body.add(overlay, 2, 2);
        }

        hover = new HTML();
        hover.setStylePrimaryName("hover");
        hover.setVisible(false);
        body.add(hover, 0, 0);
    }

    public int getIndex() {
        return index;
    }

    public void setActionIcon(ActionIcon sourceIcon) {
        if(sourceIcon == null) {
            DragDropManager.makeNotDraggable(icon);
            clear();
        } else {
            icon.show(sourceIcon.getType(), sourceIcon.getAction(), false);
            DragDropManager.makeDraggable(icon);
        }
    }

    public void handleBrowserEvent(Event event) {
        if(DOM.eventGetType(event) == Event.ONMOUSEOVER) {
            if(icon.getAction() != null) {
                ActionDetailPanel.getInstance().setAction(icon, true);
            }
        } else if(DOM.eventGetType(event) == Event.ONMOUSEOUT) {
            if(icon.getAction() != null) {
                ActionDetailPanel.getInstance().clear();
            }
        }
    }
    
    public ScriptObject getAction() {
        return icon == null ? null : icon.getAction();
    }
    
    public ActionIcon getActionIcon() {
        return icon;
    }

    public void clear() {
        icon.clear();
        removeOverlays();
    }

    public int getId() {
        return icon.getAction() == null ? 0 : icon.getAction().getInt("id");
    }

    public String getType() {
        return icon.getType() == null ? null : icon.getType();
    }

    public char getTypeCode() {
        return getType().equals("skill") ? 's' : 'i';
    }

    public void showOverlay(char type, int quantity, int remaining) {
        assert type == 'w' || type == 'c' : type;
        assert remaining > 0 : remaining; 

        //String title = "This skill has a " + quantity + " round " + (type == 'w' ? "warmup" : "cooldown") + ". ";
        //title += "You must wait " + remaining + " more " + (remaining == 1 ? "round" : "rounds") + " before using it" + (type == 'w' ? "." : " again.");
        //overlay.setTitle(title);
        
        double newHeight = (double)remaining / (double)quantity;
        newHeight = newHeight * (SizeConstants.ACTION_ICON_SIZE - 4);
        String newVerticalOffset = (41 - (int)(newHeight)) + "px";
        overlay.getElement().getStyle().setProperty("backgroundPosition", "-92px " + newVerticalOffset);
        overlay.setHTML(String.valueOf(remaining));
        overlay.setVisible(true);
    }

    public void removeOverlays() {
        if(overlay != null) {
            overlay.setVisible(false);
        }
    }

    public void onHover() {
        hover.setVisible(true);
    }
    public void onLeave() {
        hover.setVisible(false);
    }
}