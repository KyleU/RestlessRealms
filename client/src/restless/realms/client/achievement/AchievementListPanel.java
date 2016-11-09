package restless.realms.client.achievement;

import restless.realms.client.ServiceManager;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AchievementListPanel extends WindowPanel<ScrollPanel> {
    private VerticalPanel verticalPanel;
    
    private ServiceCallback callback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            int totalPossibleScore = result.getInt("totalPossibleScore");
            JsArray<ScriptObject> achievements = result.getArray("achievements");
            int totalScore = 0;
            for(int i = 0; i < achievements.length(); i++) {
                ScriptObject achievement = achievements.get(i);
                totalScore += achievement.getInt("pointValue");
                verticalPanel.add(new AchievementPanel(achievement));
            }
            ((HTML)footer).setHTML("<div style='padding:6px;'>Score: " + totalScore + " of " + totalPossibleScore + "</div>");
        }
    };

    public AchievementListPanel() {
        super("achievement", new ScrollPanel(), "Achievements", new HTML());
        
        footer.setStylePrimaryName("achievementPointTotal");
        
        verticalPanel = new VerticalPanel();
        body.add(verticalPanel);
        body.setStylePrimaryName("actionlist");
        
        addTitleIcon(190, "img/icon/filters.png", 14 * 16, "Portrait", "player");
        addTitleIcon(210, "img/icon/filters.png", 15 * 16, "Achievements", null);
    }

    public void refresh() {
        ServiceManager.call("achievement", "list", callback);
    }
}
