package restless.realms.client.animation;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.ui.Widget;

public class WidthChangeAnimation extends Animation {
    private final Widget widget;
    private final int startingWidth;
    private final int targetWidth;
    
    public WidthChangeAnimation(Widget widget, int startingWidth, int targetWidth) {
        this.widget = widget;
        this.startingWidth = startingWidth;
        this.targetWidth = targetWidth;
    }

    @Override
    protected void onUpdate(double progress) {
        int delta = targetWidth - startingWidth;
        int newWidth = startingWidth + (int)(delta * progress);
        widget.setWidth(newWidth  + "px");
    }
    
    @Override
    protected void onComplete() {
        widget.setWidth(targetWidth + "px");
        super.onComplete();
    }
}
