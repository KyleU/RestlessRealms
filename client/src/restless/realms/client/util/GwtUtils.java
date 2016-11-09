package restless.realms.client.util;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;

public class GwtUtils {
    public static void setOpacity(Element element, double opacity) {
        element.getStyle().setProperty("opacity", String.valueOf(opacity));
        element.getStyle().setProperty("filter", "alpha(opacity=" + String.valueOf((int)(opacity * 100)) + ")");
    }

    public static String escapeHtml(String maybeHtml) {
        final com.google.gwt.user.client.Element div = DOM.createDiv();
        DOM.setInnerText(div, maybeHtml);
        return DOM.getInnerHTML(div);
    }
    
    public static String getTimeDescription(long timeDelta) {
        String ret;
        if(timeDelta > 172800000) {
            ret = (timeDelta / 86400000) + " days ago";
        } else if(timeDelta > 7200000) {
            ret = (timeDelta / 3600000) + " hours ago";
        } else if(timeDelta > 60000) {
            ret = (timeDelta / 60000) + " minutes ago";
        } else if(timeDelta >= 0) {
            ret = (timeDelta / 1000) + " seconds ago";
        } else {
            ret = "from the future";
        }
        return ret;
    }

    public static native String getUserAgent() /*-{
        return navigator.userAgent.toLowerCase();
    }-*/;

    public static native void exportStaticMethods() /*-{
        $wnd.showPlayer = $entry(@restless.realms.client.playercharacter.PlayerCharacterInfoPanel::show(Ljava/lang/String;));
        $wnd.showItem = $entry(@restless.realms.client.item.ItemInfoPanel::show(Ljava/lang/String;));
    }-*/;
}
