package restless.realms.client.animation;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;

public class ScrollToBottomAnimation extends Animation {
    private final Element element;
    
    private int scrollStart;
    private int scrollEnd;
    
    public ScrollToBottomAnimation(Element element) {
        this.element = element;
    }
    
    @Override
    protected void onStart() {
        scrollStart = element.getScrollTop();
        scrollEnd = element.getScrollHeight();
    }
    
    @Override
    protected void onUpdate(double progress) {
        int newScroll = scrollStart + (int)((scrollEnd - scrollStart) * progress);
        element.setScrollTop(newScroll);
    }
    
    @Override
    protected void onComplete() {
        element.setScrollTop(scrollEnd);
    }
}