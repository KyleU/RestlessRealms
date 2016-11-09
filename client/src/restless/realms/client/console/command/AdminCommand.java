package restless.realms.client.console.command;

import restless.realms.client.ClientManager;
import restless.realms.client.ServiceManager;
import restless.realms.client.admin.AdminPanel;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

public class AdminCommand extends Command {
    private static boolean isAdmin;

    public AdminCommand() {
        this.name = "admin";
    }
    
    public static void setAdmin(boolean isAdmin) {
        AdminCommand.isAdmin = isAdmin;
    }
    
    public static boolean isAdmin() {
        return isAdmin;
    }
    
    @Override
    public void handle(String paramString) {
        if(!isAdmin) {
            throw new IllegalStateException("You are not an administrator.");
        }
        
        if(paramString == null) {
            AdminPanel.show();
        } else {
            String[] params = paramString.split(" ");
            
            if(params.length == 0) {
                throw new IllegalArgumentException("Invalid parameters.");
            } else if("sandbox".equals(params[0])) {
                    if(ClientManager.getActivePerspective().equals("play")) {
                        ClientManager.send(MessageType.ENTER_LOCATION, "sandbox");
                    }
            } else if("tutorial".equals(params[0])) {
                if(ClientManager.getActivePerspective().equals("play")) {
                    ClientManager.send(MessageType.ENTER_LOCATION, "tutorial");
                }
            } else if("chat".equals(params[0])) {
                String message = "";
                int i = 1;
                while(i < params.length) {
                    message += params[i++] + " ";
                }
                message = message.trim();
                ServiceManager.call("admin", "chat", new ServiceCallback() {
                    @Override
                    public void onSuccess(ScriptObject result) {
                    }
                }, "message", message );
            } else if("ping".equals(params[0])) {
                String message = "";
                int i = 1;
                while(i < params.length) {
                    message += params[i++] + " ";
                }
                message = message.trim();
                ServiceManager.call("admin", "ping", new ServiceCallback() {
                    @Override
                    public void onSuccess(ScriptObject result) {
                    }
                }, "message", message );
            } else {
                final Object[] serviceParams = new Object[params.length - 2];
                for(int i = 2; i < params.length; i++) {
                    String param = params[i].trim();
                    serviceParams[i - 2] = param;
                }
                
                ServiceManager.call(params[0].trim(), params[1].trim(), new ServiceCallback() {
                    @Override
                    public void onSuccess(ScriptObject result) {
                        AdminPanel.show(result);
                    }
                }, serviceParams);
            }
        }
    }
}
