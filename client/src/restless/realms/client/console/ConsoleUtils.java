package restless.realms.client.console;

import restless.realms.client.mail.MailPanel;
import restless.realms.client.report.FeedbackPanel;

import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

public class ConsoleUtils {
    private static ConsolePanel consolePanel = null;
    private static FeedbackPanel feedbackPanel = null;

    public static void log(ConsoleChannel channel, String message) {
        log(channel, message, false);
    }
    
    public static void log(ConsoleChannel channel, String message, boolean asHtml) {
        if(ConsoleUtils.consolePanel == null) {
            Window.alert("[" + channel + "] " + message);
        } else {
            consolePanel.add(channel, message, asHtml);
        }
    }

    public static void log(Widget w) {
        if(ConsoleUtils.consolePanel == null) {
            Window.alert(w.toString());
        } else {
            consolePanel.add(w);
        }
    }

	public static void error(String message) {
		log(ConsoleChannel.Error, message);
	}

    public static void error(String message, Throwable t) {
        error(message + " - " + t.getClass().getName() + " - " + t.getMessage());
    }

    public static void help(String message) {
        log(ConsoleChannel.Help, message);
    }
    
    public static boolean onKeyPress(NativePreviewEvent event) {
        boolean ret = false;
        int key = event.getNativeEvent().getKeyCode();
        if(consolePanel.hasFocus()) {
            if(13 == key) {
                consolePanel.processCommand();
            }
            ret = true;
        } else if(feedbackPanel.hasFocus()) {
            ret = true;
        } else if(MailPanel.hasFocus()) {
            ret = true;
        } else {
            if(191 == key) {
                consolePanel.focus();
                consolePanel.setText("");
                ret = true;
            } else if(13 == key) {
                consolePanel.focus();
                ret = true;
            }
        }
        return ret;
    }

    public static void setConsolePanel(ConsolePanel panel) {
        ConsoleUtils.consolePanel  = panel;
    }
    
    public static void setFeedbackPanel(FeedbackPanel feedbackPanel) {
        ConsoleUtils.feedbackPanel = feedbackPanel;
    }
}