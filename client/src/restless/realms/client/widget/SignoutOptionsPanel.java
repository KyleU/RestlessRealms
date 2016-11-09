package restless.realms.client.widget;

import restless.realms.client.ClientManager;
import restless.realms.client.RestlessRealms;
import restless.realms.client.perspective.MessageType;

public class SignoutOptionsPanel extends DialogBox {
    public SignoutOptionsPanel() {
        super(
                "Leaving Us?", 
                "It was nice having you.  We hope you'll come back soon and play again.  If you're just going to change your character, we're sorry we jumped to conclusions.", 
                "Sign Out", 
                "Characters", 
                "Cancel");
    }
    
    @Override
    public void onAction(String action) {
        if("Sign Out".equals(action)) {
            RestlessRealms.signout(true);
        } else if("Characters".equals(action)) {
            RestlessRealms.signout(false);
        } else if("Cancel".equals(action)) {
            ClientManager.send(MessageType.WINDOW_CLOSE);
        } else {
            super.onAction(action);
        }
    }
}
