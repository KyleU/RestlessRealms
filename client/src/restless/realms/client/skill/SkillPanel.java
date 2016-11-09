package restless.realms.client.skill;

import restless.realms.client.ClientManager;
import restless.realms.client.action.ActionPanel;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class SkillPanel extends ActionPanel {
    public SkillPanel(final ScriptObject skill, ClickHandler iconClickHandler, boolean linkName) {
        this.setTitle(skill.get("name"));
        this.setSummary(skill.get("summary"));

        this.setAction("skill", skill, iconClickHandler == null);
        if(iconClickHandler != null) {
            this.getIcon().addStyleName("buy");
            this.getIcon().addClickHandler(iconClickHandler);
        }
        if(linkName) {
            ClickHandler linkClickHandler = new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    ClientManager.send(MessageType.ACTIVATE, "skill", skill.getInt("id"));
                }
            };
            this.setTitleLink(linkClickHandler);
        }
    }
}
