package restless.realms.client.item;

import restless.realms.client.ServiceManager;
import restless.realms.client.action.ActionDetailPanel;
import restless.realms.client.action.ActionIcon;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

public class ItemInfoPanel extends Composite {
    private static ServiceCallback callback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            ItemInfoPanel panel = new ItemInfoPanel(result);
            ConsoleUtils.log(panel);
        }
    };
        
    public ItemInfoPanel(ScriptObject item) {
        FlowPanel body = new FlowPanel();
        initWidget(body);
        setStylePrimaryName("iteminfo");
        
        ActionIcon actionIcon = new ActionIcon();
        actionIcon.addStyleName("left");
        actionIcon.show("item", item, false);
        body.add(actionIcon);
        
        String detailHtml = ActionDetailPanel.getDetailHtml(actionIcon);
        body.add(new HTML(detailHtml));
    }

    public static void show(String itemName) {
        ServiceManager.call("shop", "iteminfo", callback, "name", itemName);
    }
}
