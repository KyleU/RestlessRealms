package restless.realms.client.console;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;

public class ConsoleMessageHandler implements KeyPressHandler, ClickHandler {
	private ConsolePanel logPanel;

	public ConsoleMessageHandler(ConsolePanel logPanel) {
		this.logPanel = logPanel;
	}
	
	@Override
	public void onKeyPress(KeyPressEvent event) {
        int key = event.getCharCode();
        if(key  == 10 || key == 13) {
			logPanel.processCommand();
		}
	}
	
    @Override
    public void onClick(ClickEvent event) {
		logPanel.processCommand();
    }
}
