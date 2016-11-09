package restless.realms.client.inventory;

import restless.realms.client.util.ScriptObject;

import com.google.gwt.core.client.JsArray;

public interface InventoryHandler {
    void onCurrency(int currency);
    void onTokens(int tokens);
    void onItems(JsArray<ScriptObject> items);
    void onPerks(JsArray<ScriptObject> perks);
}