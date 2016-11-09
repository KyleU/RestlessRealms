package restless.realms.client.action;

import restless.realms.client.dragdrop.DragDropManager;
import restless.realms.client.util.ScriptObject;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

public class ActionPanel extends Composite {
    private AbsolutePanel body;

    private ActionIcon icon;
    
    private final Label titleLabel;
    private final Label summaryLabel;
    private final Label additionalInfoLabel;
    
    public ActionPanel() {
        body = new AbsolutePanel();
        body.setStylePrimaryName("actionpanel");
        initWidget(body);
        
        titleLabel = new Label();
        body.add(titleLabel, 55, 3);
        summaryLabel = new Label();
        summaryLabel.setStylePrimaryName("actionsummary");
        body.add(summaryLabel, 55, 17);
        additionalInfoLabel = new Label();
        body.add(additionalInfoLabel, 55, 31);
    }
    
    protected void setAction(String type, ScriptObject action, boolean draggable) {
        this.icon = new ActionIcon();
        this.icon.show(type, action, true);
        body.add(this.icon, 3, 3);
        if(draggable) {
            DragDropManager.makeDraggable(this.icon);
        }
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
    
    public void setTitleLink(ClickHandler iconClickHandler) {
        this.titleLabel.addClickHandler(iconClickHandler);
        this.titleLabel.getElement().getStyle().setProperty("textDecoration", "underline");
        this.titleLabel.getElement().getStyle().setProperty("cursor", "pointer");
    }

    
    public void setRarity(int rarity) {
        String rarityName = "normal";
        if(rarity == 0) {
            //normal
        } else if(rarity == 1) {
            rarityName = "magic";
        } else if(rarity == 2) {
            rarityName = "rare";
        } else if(rarity == 3) {
            rarityName = "unique";
        } else if(rarity == 4) {
            rarityName = "legendary";
        } else {
            assert false : rarity;
        }

        this.titleLabel.setStylePrimaryName(rarityName);
    }
    
    public void setSummary(String summary) {
        this.summaryLabel.setText(summary);
    }
    
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfoLabel.setText(additionalInfo);
    }
}
