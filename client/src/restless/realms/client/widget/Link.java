package restless.realms.client.widget;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;

public class Link extends HTML {
    public Link(final String text, boolean asHtml, final String hoverTitle, final ClickHandler clickHandler) {
        super();
        if(asHtml) {
            this.setHTML(text);
        } else {
            this.setText(text);
        }
        this.setWordWrap(false);
        this.setStylePrimaryName("link");
        if(hoverTitle != null && hoverTitle.length() > 0) {
            setTitle(hoverTitle);
        }
        addClickHandler(clickHandler);
    }

    public void activate() {
        this.addStyleDependentName("active");
    }
    public void deactivate() {
        this.removeStyleDependentName("active");
    }
}
