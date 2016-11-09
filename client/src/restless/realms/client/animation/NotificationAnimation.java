package restless.realms.client.animation;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.ui.Widget;

public class NotificationAnimation extends Animation {
    private final Widget widget;
    
    public NotificationAnimation(Widget widget) {
        this.widget = widget;
    }
    
    @Override
    protected void onStart() {
        widget.setHeight("0px");
    }
    
    @Override
    protected void onUpdate(double progress) {
        if(progress < 0.05) {
            widget.setHeight((55 * (progress * 20)) + "px");
        } else if(progress < 0.95) {
            widget.setHeight("55px");
        } else {
            widget.setHeight((55 - (55 * ((progress - 0.95) * 20))) + "px");
        }
    }
    
    @Override
    protected void onComplete() {
        widget.setHeight("0px");
        widget.removeFromParent();
    }
}