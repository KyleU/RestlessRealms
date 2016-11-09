package restless.realms.client.leaderboard;

import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.util.CommonEventHandlers;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.NavigationPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;

public class LeaderboardPanel extends NavigationPanel {
    private boolean initialized = false;
    private ServiceCallback callback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            StringBuilder table = new StringBuilder();
            table.append("<table class=\"leaderboard\" style=\"width:100%;\">\n");
            table.append("<tr><th style=\"text-align:left;width:100px;\">#</th><th style=\"text-align:left;width:200px;\">");
            table.append("Name");
            table.append("</th><th style=\"text-align:left;width:100px;\">");
            table.append("Class");
            table.append("</th><th style=\"text-align:left;\">");
            table.append(result.get("valueTitle"));
            table.append("</th></tr>");

            ScriptObject leaderboard = result.getObject("leaderboard");
            JsArray<ScriptObject> entries = leaderboard.getArray("entries");
            for(int i = 0; i < entries.length(); i++) {
                ScriptObject entry = entries.get(i);
                table.append("<tr class=\"" + (i % 2 == 0 ? "even" : "odd") + "\"><td>");
                table.append(i + 1);
                table.append("</td><td>");
                table.append("<span class=\"playerlink\" onclick=\"showPlayer('" + entry.get("playerName") + "');\">");
                table.append(entry.get("playerName"));
                table.append("</span>");
                table.append("</td><td>");
                table.append(entry.get("profession"));
                table.append("</td><td>");
                String value = NumberFormat.getDecimalFormat().format(Integer.parseInt(entry.get("value")));
                table.append(value);
                table.append("</td></tr>\n");
            }
            table.append("</table>");
            
            html.setHTML(table.toString());
        }
    };
    private HTML html;

    public LeaderboardPanel() {
        super("leaderboards", "Leaderboards");
        addExitButton(CommonEventHandlers.CLICK_WINDOW_CLOSE);
        setRightPanel(new ScrollPanel());
        html = new HTML();
        ((ScrollPanel)rightPanel).add(html);
    }

    public void show() {
        ClientState.getLayout().showPanel("leaderboard");
        select(null);
        html.setHTML("");
        
        if(!initialized ) {
            ServiceManager.call("leaderboard", "list", new ServiceCallback() {
                @Override
                public void onSuccess(ScriptObject result) {
                    initialized = true;
                    JsArrayString keys = result.keys();
                    for(int i = 0; i < keys.length(); i++) {
                        String key = keys.get(i);
                        if(!key.startsWith("_")) {
                            addLink(key, result.get(key), null);
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onNavigation(String key) {
        select(key);
        ServiceManager.call("leaderboard", "get", callback, "id", key);
    }
}
