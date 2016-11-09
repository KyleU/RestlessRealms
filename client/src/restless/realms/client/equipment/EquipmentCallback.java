package restless.realms.client.equipment;

import restless.realms.client.ClientState;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.item.ItemShopPanel;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

public class EquipmentCallback extends ServiceCallback {
    private String successMessage;
    
    public EquipmentCallback(String successMessage) {
        this.successMessage = successMessage;
    }
    
    @Override
    public void onSuccess(ScriptObject result) {
        ScriptObject equipment = result.getObject("equipment");
        ScriptObject pc = result.getObject("pc");
        
        PlayerCharacterCache.getInstance().setEquipment(equipment);
        ClientState.setPlayerCharacter(pc);
        ClientState.setItems(PlayerCharacterCache.getInstance().getItems());
        ItemShopPanel isp = (ItemShopPanel)ClientState.getLayout().getPanel("itemshop");
        if(isp != null) {
            isp.refreshSellPanel();
        }
        ConsoleUtils.help(successMessage);
    }
}
