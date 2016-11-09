package restless.realms.client.widget;

import restless.realms.client.ClientState;
import restless.realms.client.achievement.AchievementListPanel;
import restless.realms.client.facebook.FacebookPanel;
import restless.realms.client.layout.PanelLocation;
import restless.realms.client.layout.SizeConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class WindowPanel<T extends Widget> extends Composite {
    private final VerticalPanel content = new VerticalPanel();
	
    protected final AbsolutePanel titleRow = new AbsolutePanel();
	protected final Label titleText = new Label();
	protected ButtonPanel exitButton;
	protected final T body;
	protected final Widget footer;

	public WindowPanel(String id, T body, String windowTitle, Widget footer) {
		initWidget(content);
		
		titleText.setText(windowTitle);
		titleText.setWordWrap(false);
		
        titleRow.setStyleName("title");
		titleRow.add(this.titleText, 5, 5);
		content.add(titleRow);
        content.setCellHeight(titleText, "30px");

        this.body = body;
        body.getElement().setId(id);
		content.add(body);
        body.getElement().getParentElement().setClassName("windowbody");
        body.getElement().getParentElement().setId(id + "-container");
		
        this.footer = footer;
		if(footer != null) {
			content.add(footer);
			content.setCellHeight(footer, "25px");
			footer.setHeight("25px");
            footer.getElement().getParentElement().setClassName("windowfooter");
		}
	}
	
	public void setWindowTitle(String windowTitle) {
	    titleText.setText(windowTitle);
	}
	
	@Override
	public void setHeight(String height) {
	    if(height.endsWith("px")) {
			int bodyHeight = Integer.parseInt(height.substring(0, height.indexOf("px")));
			bodyHeight = bodyHeight - 30;
			if(footer != null) {
				bodyHeight = bodyHeight - 25;
			}
			body.setHeight(bodyHeight + "px");
		}
		super.setHeight(height);
	}
	
	@Override
	public void setWidth(String width) {
        if(width.endsWith("px")) {
            body.setWidth(width);
        }
	    super.setWidth(width);
	}
	
    protected void addExitButton(ClickHandler clickHandler) {
        addExitButton("Back", clickHandler);
    }

    protected void addExitButton(String title, ClickHandler clickHandler) {
        assert exitButton == null;
        exitButton = new ButtonPanel(title, clickHandler, 0);
        titleRow.add(exitButton, 674, 4);
    }
    
    protected void addTitleIcon(int left, String imageUrl, int imageX, String title, final String panelKey) {
        Image icon = new Image(imageUrl, imageX, panelKey == null ? 16 : 0, SizeConstants.SMALL_ICON_DIMENSION, SizeConstants.SMALL_ICON_DIMENSION);
        if(panelKey != null) {
            icon.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if(!ClientState.getLayout().containsPanel(panelKey)) {
                        if(panelKey.equals("facebook")) {
                            ClientState.getLayout().addPanel(PanelLocation.BOTTOMRIGHT, "facebook", new FacebookPanel());
                        } else if(panelKey.equals("achievement")) {
                            AchievementListPanel panel = new AchievementListPanel();
                            ClientState.getLayout().addPanel(PanelLocation.BOTTOMLEFT, "achievement", panel);
                            panel.refresh();
                        }
                    }
                    ClientState.getLayout().showPanel(panelKey);
                }
            });
            icon.setStylePrimaryName("clickable");
        }
        icon.setTitle(title);
        titleRow.add(icon, left, 5);
    }
}
