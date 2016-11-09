package restless.realms.client.messaging;

import java.io.Serializable;
import java.util.List;

import restless.realms.client.RestlessRealms;
import restless.realms.client.achievement.AchievementNotifications;
import restless.realms.client.audio.AudioManager;
import restless.realms.client.chat.ChatService;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.mail.MessagesLink;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.version.VersionManager;
import restless.realms.client.widget.TipPanel;

import com.google.gwt.user.client.Timer;
import com.greencat.gwt.comet.client.CometClient;
import com.greencat.gwt.comet.client.CometListener;

public class MessagingListener implements CometListener {
    private CometClient client;

    @Override
    public void onConnected(int heartbeat, int connectionID) {
//        ConsoleUtils.log(ConsoleChannel.Help, "comet.connected [" +heartbeat+", "+connectionID+"]");
    }

    @Override
    public void onDisconnected() {
//        ConsoleUtils.log(ConsoleChannel.Help, "comet.disconnected");
    }

    @Override
    public void onError(Throwable exception, boolean connected) {
//        int statuscode =-1;
//        if (exception instanceof StatusCodeException) {
//            statuscode = ((StatusCodeException)exception).getStatusCode();
//        }
//        ConsoleUtils.log(ConsoleChannel.Help, "comet.error [connected=" + connected + "] ("+statuscode+")" + exception.getMessage());
    }

    @Override
    public void onHeartbeat() {
//        ConsoleUtils.log(ConsoleChannel.Help, "comet.heartbeat ["+client.getConnectionID()+"]");
    }

    @Override
    public void onRefresh() {
//        ConsoleUtils.log(ConsoleChannel.Help, "comet.refresh ["+client.getConnectionID()+"]");
    }

    @Override
    public void onMessage(List<? extends Serializable> messages) {
        for(Serializable obj : messages) {
            ScriptObject event = ScriptObject.fromJson(obj.toString());
            
            String type = event.get("type");
            ScriptObject message = event.getObject("message");
            if("CHAT".equals(type)) {
                ChatService.logMessage(message);
            } else if("SIGNOUT".equals(type)) {
                ConsoleUtils.help("You have signed in elsewhere.");
                ConsoleUtils.help("The game will refresh in ten seconds.");
                new Timer() {
                    @Override
                    public void run() {
                        RestlessRealms.signout(false);
                    }
                }.schedule(10000);
            } else if("MESSAGES".equals(type)) {
                MessagesLink.getInstance().setNumMessages(message.getInt("numMessages"));
            } else if("UPDATE".equals(type)) {
                String tipText = message.get("tip");
                TipPanel.setTipText(tipText);
                int serverVersion = message.getInt("version");
                VersionManager.check(serverVersion);
            } else if("ACHIEVEMENT".equals(type)) {
                AchievementNotifications.getInstance().display(message);
                AudioManager.play("bloop");
            } else {
                ConsoleUtils.error("Unhandled event of type " + type + ": " + event.toDebugString());
            }
//            ConsoleUtils.log(ConsoleChannel.Help, "comet.message [" + client.getConnectionID() + "] " + obj.toString());
        }
//        ConsoleUtils.log(ConsoleChannel.Help, "[" + client.getConnectionID() + "] Received " + messages.size() + " messages: " + messages.toString());
    }

    public CometClient getClient() {
        return client;
    }
    public void setClient(CometClient client) {
        this.client = client;
    }
};

