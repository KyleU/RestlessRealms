package restless.realms.client.widget;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

public class TipPanel extends Composite {
    private static TipPanel instance;
    
    private HTML tipText;
    
    private TipPanel() {
        tipText = new HTML();
        initWidget(tipText);
        tipText.setStylePrimaryName("tiptext");
    }
    
    public static void init() {
        instance = new TipPanel();
        RootPanel.get("tipcontainer").add(instance);
    }
    
    public static TipPanel getInstance() {
        return instance;
    }
    
    public static void setTipText(String text) {
        instance.tipText.setHTML(text);
    }
}
