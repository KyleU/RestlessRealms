package restless.realms.client.widget;

import restless.realms.client.animation.FadeInAnimation;
import restless.realms.client.animation.FadeOutAnimation;
import restless.realms.client.animation.WidthChangeAnimation;
import restless.realms.client.util.GwtUtils;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ProgressBar extends AbsolutePanel {
    private Widget progressBar;
    private Label label;
    
    private final int width;
    private final String suffix;

    private int value;
    private int maxValue;
    private int currentPercentage;
    
    private FadeInAnimation fadeInAnimation;
    private FadeOutAnimation fadeOutAnimation;
    
    public ProgressBar(int width, int colorIndex, String suffix, boolean handleFades) {
        this.width = width;
        this.suffix = suffix;
        
        this.setStylePrimaryName("progress");
        this.setWidth(width + "px");
        
        if(handleFades) {
            MouseOverHandler mouseOverHandler = new MouseOverHandler() {
                @Override
                public void onMouseOver(MouseOverEvent event) {
                    fadeIn();
                }
            };
            this.addDomHandler(mouseOverHandler, MouseOverEvent.getType());
            
            MouseOutHandler mouseOutHandler = new MouseOutHandler() {
                @Override
                public void onMouseOut(MouseOutEvent event) {
                    fadeOut();
                }
            };
            this.addDomHandler(mouseOutHandler, MouseOutEvent.getType());
        }
        
        progressBar = new HTML(" ");
        progressBar.setStylePrimaryName("bar");
        progressBar.getElement().getStyle().setProperty("backgroundPosition", "0px -" + (14 * colorIndex) + "px");
        this.add(progressBar, 0, 0);

        label = new Label("0 / 0");
        GwtUtils.setOpacity(label.getElement(), 0.0);
        label.setWidth(width + "px");
        label.getElement().getStyle().setProperty("cursor", "default");
        label.getElement().getStyle().setProperty("textAlign", "center");
        this.add(label, 0, 0);
        this.fadeInAnimation = new FadeInAnimation(label.getElement());
        this.fadeOutAnimation = new FadeOutAnimation(label.getElement());

    }
    
    public void setMaxValue(int maxValue) {
        assert maxValue >= 0;
        if(this.maxValue != maxValue) {
            this.maxValue = maxValue;
            resize(false);
        }
    }
    
    public int getMaxValue() {
        return maxValue;
    }
    
    public void setValue(int value, boolean animate) {
        if(value < 0) {
            value = 0;
        }
        if(value > this.maxValue) {
            value = maxValue;
        }
        
        if(this.value != value) {
            this.value = value;
            resize(animate);
        }
    }
    
    public int getValue() {
        return value;
    }

    public void fadeIn() {
        fadeInAnimation.run(1000);   
    }
    
    public void fadeOut() {
        fadeOutAnimation.run(1000);
    }
    
    private void resize(boolean animate) {
        int percentage = maxValue == 0 ? 0 : ((value * 100) / maxValue);
        assert percentage >= 0 : percentage + "% (" + value + " of " + maxValue + ").";;
        //assert percentage <= 100 : percentage + "% (" + value + " of " + maxValue + ").";

        int oldWidth = (width * this.currentPercentage) / 100;
        this.currentPercentage = percentage;
        int progressWidth = (width * percentage) / 100;
        
        String text = value + " / " + maxValue;
        if(suffix != null) {
            text += suffix;
        }
        label.setText(text);
        fadeOutAnimation.run(2000);
        if(animate) {
            new WidthChangeAnimation(progressBar, oldWidth, progressWidth).run(1000);
        } else {
            progressBar.setWidth(progressWidth + "px");
        }
    }
}
