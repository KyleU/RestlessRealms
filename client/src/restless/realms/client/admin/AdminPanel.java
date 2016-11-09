package restless.realms.client.admin;

import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.layout.PanelLocation;
import restless.realms.client.util.CommonEventHandlers;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.NavigationPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AdminPanel extends NavigationPanel {
    private static AdminPanel instance;  
    
    private VerticalPanel contents;

    private ServiceCallback callback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            JsArrayString keys = result.keys();
            for(int i = 0; i < keys.length(); i++) {
                String key = keys.get(i);
                addLink(key, result.get(key), null);
            }
        }
    };

    public AdminPanel() {
        super("admin", "Administration");

        contents = new VerticalPanel();

        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.add(contents);
        setRightPanel(scrollPanel);
        
        addExitButton(CommonEventHandlers.CLICK_WINDOW_CLOSE);
    }
    
    @Override
    protected void onNavigation(String key) {
        contents.clear();
        select(key);
        ServiceManager.call("admin", key, new ServiceCallback() {
            @Override
            public void onSuccess(ScriptObject result) {
                showResult(result);
            }
        });
    }

    public static void show() {
        if(instance == null) {
            instance = new AdminPanel();
            ClientState.getLayout().addPanel(PanelLocation.MAIN, "admin", instance);
            ServiceManager.call("admin", "list", instance.callback);
        }
        ClientState.getLayout().showPanel("admin");
    }

    public static void show(ScriptObject result) {
        show();
        instance.select(null);
        instance.showResult(result);
    }
    
    private void showResult(ScriptObject result) {
        JsArrayString keys = result.keys();
        for(int i = 0; i < keys.length(); i++) {
            String key = keys.get(i);

            contents.add(new HTML("<strong>" + key + "</strong>"));
            if(result.isArray(key)) {
                contents.add(getTable(result.getArray(key)));
            } else if(result.isObject(key)) {
                ScriptObject object = result.getObject(key);
                contents.add(new Label(object.toDebugString()));
            } else {
                contents.add(new Label(result.get(key)));
            }       
        }
    }

    private FlexTable getTable(JsArray<ScriptObject> array) {
        FlexTable ret;
        if(array.length() == 0) {
            ret = new FlexTable();
            ret.setText(0, 0, "No Results");
        } else {
            ret = getTable(array.get(0));
            //odd loop to skip first element
            for(int i = 1; i < array.length(); i++) {
                ScriptObject row = array.get(i);
                JsArrayString keys = row.keys();
                for(int j = 0; j < keys.length(); j++) {
                    String key = keys.get(j);
                    ret.setText(i + 1, j, row.get(key));
                }
            }
        }
        return ret;
    }

    private FlexTable getTable(ScriptObject scriptObject) {
        FlexTable ret = new FlexTable();
        ret.setStylePrimaryName("data");
        JsArrayString keys = scriptObject.keys();
        for(int j = 0; j < keys.length(); j++) {
            String key = keys.get(j);
            ret.setHTML(0, j, "<strong>" + key + "</strong>");
            ret.setText(1, j, scriptObject.get(key));
        }

        return ret;
    }
}
