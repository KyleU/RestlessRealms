package restless.realms.client.chat;

import restless.realms.client.ServiceManager;
import restless.realms.client.audio.AudioManager;
import restless.realms.client.console.ConsoleChannel;
import restless.realms.client.console.ConsolePanel;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.util.GwtUtils;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

import com.google.gwt.core.client.JsArray;

public class ChatService {
    protected static final int MAX_INTERVAL = 60000;

    private static int latestKnownChatIndex;

    private static final ServiceCallback chatCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject ret) {
            if(ret == null || ret.getArray("messages").length() == 0) {
            } else {
                JsArray<ScriptObject> messages = ret.getArray("messages");
                logMessages(messages);
            }
            ConsolePanel.setAnimated(true);
        }
    };

    public static void refreshNow() {
        ServiceManager.call("chat", "list", chatCallback, "latestKnownIndex", Integer.toString(latestKnownChatIndex));
    }

    private static void logMessages(JsArray<ScriptObject> ret) {
        if(ret != null) {
            for(int i = 0; i < ret.length(); i++) {
                ScriptObject message = ret.get(i);
                logMessage(message);
            }
        }
    }

    public static void logMessage(ScriptObject message) {
        String channel = message.get("channel");
        ConsoleChannel consoleChannel = ConsoleChannel.valueOf(channel);
        String playerName = message.get("playerName");
        String content = getPlayerLink(playerName);
        if(consoleChannel == ConsoleChannel.Region) {
            //skip colon
        } else if(consoleChannel == ConsoleChannel.Pvp) {
            //skip colon
        } else if(consoleChannel == ConsoleChannel.Tell) {
            content += " whispers: ";
            AudioManager.play("beep");
        } else {
            content += ": ";
        }
        String messageContent = GwtUtils.escapeHtml(message.get("content"));
        messageContent = replacePlayerNames(messageContent);
        messageContent = replaceItemNames(messageContent);
        content += messageContent;
        ConsoleUtils.log(consoleChannel, content, true);
        latestKnownChatIndex = message.getInt("id");
    }

    private static String replacePlayerNames(String messageContent) {
        int currentIndex = messageContent.indexOf('#', 0);

        String ret;
        if(currentIndex == -1) {
            ret = messageContent;
        } else {
            ret = messageContent.substring(0, currentIndex);
            while(currentIndex > -1) {
                int startIndex = currentIndex;
                currentIndex = messageContent.indexOf('#', currentIndex+1);
                int endIndex = currentIndex;
                if(endIndex == -1) {
                    ret += messageContent.substring(startIndex);
                } else {
                    String playerName = messageContent.substring(startIndex + 1, endIndex);
                    if(playerName.trim().length() == 0) {
                        ret += "#" + playerName + "#";
                    } else {
                        ret += getPlayerLink(playerName);
                    }
                    currentIndex = messageContent.indexOf('#', currentIndex + 1);
                    if(currentIndex == -1) {
                        ret += messageContent.substring(endIndex + 1);
                    } else {
                        ret += messageContent.substring(endIndex + 1, currentIndex);
                    }
                }
            }
        }
        return ret;
    }

    private static String getPlayerLink(String playerName) {
        return "<span class=\"playerlink\" onclick=\"showPlayer('" + playerName + "');return false;\">" + playerName + "</span>";
    }

    private static String replaceItemNames(String messageContent) {
        int currentIndex = messageContent.indexOf('@', 0);

        String ret;
        if(currentIndex == -1) {
            ret = messageContent;
        } else {
            ret = messageContent.substring(0, currentIndex);
            while(currentIndex > -1) {
                int startIndex = currentIndex;
                currentIndex = messageContent.indexOf('@', currentIndex+1);
                int endIndex = currentIndex;
                if(endIndex == -1) {
                    ret += messageContent.substring(startIndex);
                } else {
                    String itemName = messageContent.substring(startIndex + 1, endIndex);
                    if(itemName.trim().length() == 0) {
                        ret += "@" + itemName + "@";
                    } else {
                        ret += getItemLink(itemName);
                    }
                    currentIndex = messageContent.indexOf('@', currentIndex + 1);
                    if(currentIndex == -1) {
                        ret += messageContent.substring(endIndex + 1);
                    } else {
                        ret += messageContent.substring(endIndex + 1, currentIndex);
                    }
                }
            }
        }
        return ret;
    }

    private static String getItemLink(String itemName) {
        return "<span class=\"itemlink\" onclick=\"showItem('" + itemName + "');return false;\">" + itemName + "</span>";
    }
}