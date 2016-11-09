package restless.realms.client.achievement;

import restless.realms.client.action.ActionIcon;
import restless.realms.client.animation.NotificationAnimation;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

public class AchievementNotifications extends Composite {
    private static AchievementNotifications instance;
    
    public static void init() {
        instance = new AchievementNotifications();
        RootPanel.get("notifications").add(instance);
    }
    
    public static AchievementNotifications getInstance() {
        return instance;
    }
    
    private FlowPanel body;
    
    public AchievementNotifications() {
        body = new FlowPanel();
        initWidget(body);
    }
    
    public void display(ScriptObject achievement) {
        AbsolutePanel panel = new AbsolutePanel();
        panel.setStylePrimaryName("notification");

        ActionIcon icon = new ActionIcon();
        icon.show("achievement", achievement, false);
        panel.add(icon, 5, 5);
        
        String html = "<span class=\"earned\">You've earned an Achievement!</span> ";
        html += "<strong>" + achievement.get("name") + ":</strong> ";
        html += achievement.get("description");
        HTML text = new HTML(html);
        panel.add(text, 55, 20);
        
        body.add(panel);
        new NotificationAnimation(panel).run(4000);
    }
}
