package restless.realms.client.animation;

import restless.realms.client.util.GwtUtils;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;

public class EffectResultAnimation extends Animation implements Runnable {
    private Element element;
    private final int startY;
    private final int stopY;
    private final Element parent;
    private final ScriptObject effectResult;

    public EffectResultAnimation(Element parent, int startY, int stopY, ScriptObject effectResult) {
        this.element = DOM.createDiv();
        this.parent = parent;
        this.startY = startY;
        this.stopY = stopY;
        this.effectResult = effectResult;

        this.element.setClassName("effect");
    }
    
    @Override
    protected void onStart() {
        String text;
        int quantity = effectResult.getInt("quantity");
        String type = effectResult.get("type");
        if("HEALING".equals(type) || "REPLENISH".equals(type)) {
            text = "+" + quantity;
            //text = String.valueOf(quantity);
        } else if("DEATH".equals(type) || "STUN".equals(type)) {
            text = type;
        } else {
            //text = "-" + quantity;
            text = String.valueOf(quantity);
        }
        this.element.setInnerText(text);
        this.element.getStyle().setProperty("color", getColor(type));

        parent.appendChild(this.element);

        GwtUtils.setOpacity(element, 1.0);
        element.getStyle().setProperty("top", startY + "px");
    }
    
    @Override
    protected void onUpdate(double progress) {
        int delta = stopY - startY;

        int newY = startY + (int)(delta * progress);
        element.getStyle().setProperty("top", newY + "px");
        
        double opacity = (-progress) + 1;
        GwtUtils.setOpacity(element, opacity);
    }

    @Override
    protected void onComplete() {
        parent.removeChild(this.element);
    }

    private String getColor(String type) {
        String ret = "#f00";
        if("PHYSICAL".equals(type)) {
            //ret = "#731010";
            ret = "#fff";
        } else if("FIRE".equals(type)) {
            ret = "#f57400";
        } else if("ICE".equals(type)) {
            ret = "#00c6ff";
        } else if("ELECTRIC".equals(type)) {
            ret = "#ffc81f";
        } else if("HEALING".equals(type)) {
            ret = "#00c6ff";
        } else if("DRAIN".equals(type)) {
            ret = "#71ac00";
        } else if("REPLENISH".equals(type)) {
            ret = "#4778d2";
        } else if("DEATH".equals(type)) {
            ret = "#fff";
        } else if("STUN".equals(type)) {
            ret = "#00ac5d";
        } else {
            assert false;
        }
        return ret;
    }

    @Override
    public void run() {
        run(2000);
    }
}
