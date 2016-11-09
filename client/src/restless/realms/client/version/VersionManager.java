package restless.realms.client.version;

import restless.realms.client.console.ConsoleUtils;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

public class VersionManager {
    private static int clientVersion = 0;
    
    public static void check(int serverVersion) {
        if(clientVersion == 0) {
            clientVersion = serverVersion;
        }
        if(clientVersion < serverVersion) {
            ConsoleUtils.help("A new version of Restless Realms is available!");
            ConsoleUtils.help("The application will reload in ten seconds.");
            Timer timer = new Timer() {
                @Override
                public void run() {
                    Window.Location.reload();
                }
            };
            timer.schedule(10000);
            return;
        }
    }

}
