package restless.realms.client.layout;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Widget;

public class PanelInfo {
    final DeckPanel deckPanel;
    final Map<String, Integer> panelIndexes;
    
    public PanelInfo() {
        this.deckPanel = new DeckPanel() {
            /**
             * Horrible hack to allow scroll panels to shrink/grow. 
             */
            @Override
            public void setHeight(String height) {
                int index = this.getVisibleWidget();
                if(index > -1) {
                    this.getWidget(index).setHeight(height);
                }
                super.setHeight(height);
            }
        };
//        if(!GwtUtils.getUserAgent().contains("msie")) {
//            this.deckPanel.setAnimationEnabled(true);
//        }
        this.panelIndexes = new HashMap<String, Integer>();
    }
    
    public void add(String key, Widget w) {
        this.deckPanel.add(w);
        int index = this.deckPanel.getWidgetCount() - 1;
        panelIndexes.put(key, index);
    }
}
