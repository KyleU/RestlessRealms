package restless.realms.client.achievement;

import restless.realms.client.action.ActionIcon;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class AchievementPanel extends Composite {
    private AbsolutePanel body;

    private ActionIcon icon;
    
    private final Label titleLabel;
    private final Label descriptionLabel;
    
    public AchievementPanel(ScriptObject achievement) {
        body = new AbsolutePanel();
        body.setStylePrimaryName("achievementpanel");
        initWidget(body);
        
        titleLabel = new Label(achievement.get("name"));
        body.add(titleLabel, 55, 3);
        descriptionLabel = new Label(achievement.get("description"), true);
        descriptionLabel.setStylePrimaryName("actionsummary");
        body.add(descriptionLabel, 55, 17);
        this.icon = new ActionIcon();
        this.icon.show("achievement", achievement, false);
        body.add(this.icon, 3, 3);
    }
    
    protected ActionIcon getIcon() {
        return this.icon;
    }
    
    protected AbsolutePanel getBody() {
        return body;
    }
    
    @Override
    public void setTitle(String title) {
        this.titleLabel.setText(title);
    }
    
    @Override
    public String getTitle() {
        return this.titleLabel.getText();
    };
    
    public void setDescription(String description) {
        this.descriptionLabel.setText(description);
    }
}
