package restless.realms.client.facebook;

import restless.realms.client.widget.WindowPanel;

import com.google.gwt.user.client.ui.HTML;

public class FacebookPanel extends WindowPanel<HTML> {
    public FacebookPanel() {
        super("facebook", new HTML(), "Facebook", null);
        
        addTitleIcon(150, "img/icon/filters.png", 0, "Chat", "console");
        addTitleIcon(170, "img/icon/filters.png", 16, "Combat Log", "combatlog");
        addTitleIcon(190, "img/icon/filters.png", 32, "Feedback", "feedback");
        addTitleIcon(210, "img/icon/filters.png", 48, "Facebook", null);
        
        body.setHTML("<iframe src=\"http://www.facebook.com/plugins/likebox.php?id=145550285128&amp;width=230&amp;connections=3&amp;stream=false&amp;header=false&amp;height=220\" scrolling=\"no\" frameborder=\"0\" style=\"border:none; overflow:hidden; width:230px; height:220px; background-color:#ffffff;\"></iframe>");
    }
}
