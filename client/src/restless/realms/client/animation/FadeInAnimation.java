package restless.realms.client.animation;

import restless.realms.client.util.GwtUtils;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;

public class FadeInAnimation extends Animation {
    private final Element element;
    
    public FadeInAnimation(Element element) {
        this.element = element;
    }
    
    @Override
    protected void onStart() {
        GwtUtils.setOpacity(element, 0.0);
    }
    
    @Override
    protected void onUpdate(double progress) {
        GwtUtils.setOpacity(element, progress);
    }
    
    @Override
    protected void onComplete() {
        GwtUtils.setOpacity(element, 1.0);
    }
}