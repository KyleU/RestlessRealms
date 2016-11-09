package restless.realms.client.widget;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

public class LoadingPanel extends Composite {
	public LoadingPanel() {
		initWidget(new HTML("Loading..."));
	}
}
