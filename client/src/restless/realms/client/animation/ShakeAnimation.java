package restless.realms.client.animation;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Element;

public class ShakeAnimation extends Animation {
    private final Element element;
    private final int radius;
    private final int originalX;
    
    public ShakeAnimation(Element element, int radius, int originalX) {
        this.element = element;
        this.radius = radius;
        this.originalX = originalX;
    }
    
    @Override
    protected void onStart() {
    }

    @Override
    protected void onUpdate(double progress) {
//        double offset = ((progress - 0.5) * 2) * radius;
//        int newX = originalX - (int)offset; 
//        element.getStyle().setLeft(newX, Unit.PX);
        int newX;
        if(progress < 0.25) {
            newX = originalX - radius;
        } else if(progress < 0.5) {
            newX = originalX + radius;
        } else if(progress < 0.75) {
            newX = originalX - radius;
        } else {
            newX = originalX + radius;
        }
        element.getStyle().setLeft(newX, Unit.PX);
    }
    
    @Override
    protected void onComplete() {
        element.getStyle().setLeft(originalX, Unit.PX);
    }
}
