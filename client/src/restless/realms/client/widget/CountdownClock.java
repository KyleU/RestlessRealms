package restless.realms.client.widget;

import java.util.Date;

import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.playercharacter.PlayerCharacterHandler;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

public class CountdownClock extends Composite implements PlayerCharacterHandler {
    private static CountdownClock instance;

    private HTML body;
    private boolean triggered;
    private Date date;
    
    private Timer timer;
    
    public CountdownClock() {
        body = new HTML();
        body.setTitle("Time until AP regen (up to 5) and bodyguard upkeep payments.");
        //body.setVisible(false);
        initWidget(body);
        setStylePrimaryName("countdown");
        timer = new Timer() {
            @Override
            public void run() {
                update();
                timer.schedule(100);
            }
        };
    }
    
    private void update() {
        if(date != null) {
            Date now = new Date();
            int diff = (int)(date.getTime() - now.getTime());
            if(diff < 0) {
                if(!triggered) {
                    triggered = true;
                    ServiceManager.call("account", "aps", new ServiceCallback() {
                        @Override
                        public void onSuccess(ScriptObject result) {
                            int adventurePoints = result.getInt("aps");
                            ClientState.setAdventurePoints(adventurePoints);
                            int nextAdventurePointSeconds = result.getInt("nextadventurepointseconds");
                            ClientState.setNextAdventurePointSeconds(nextAdventurePointSeconds, adventurePoints);
                            int oldCurrency = PlayerCharacterCache.getInstance().getCurrency();
                            int newCurrency = result.getInt("currency");
                            ClientState.setCurrency(newCurrency);
                            if(oldCurrency > newCurrency) {
                                ConsoleUtils.help("You have paid a " + (oldCurrency - newCurrency) + " gold upkeep cost for your bodyguards.");
                            }
                            triggered = false;
                        }
                    });
                }
            } else {
                int seconds = (diff / 1000) % 60;
                int minutes = (diff / 1000 / 60) % 60;
                int hours = (diff / 1000 / 60 / 60);
                body.setHTML("Countdown: " + (hours < 10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds));
            }
        }
    }
    
    @Override
    public void applyEffects(JsArray<ScriptObject> actions, JsArrayString actionNames) {
        //no op
    }
    
    @Override
    public void onAdventurePoints(int adventurePoints) {
        //body.setVisible(adventurePoints < 5);
    }
    
    @Override
    public void onPlayerCharacter(ScriptObject playerCharacter) {
        //no op
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public static CountdownClock getInstance() {
        if(instance == null) {
            instance = new CountdownClock();
            ClientState.addPlayerCharacterHandler(instance);
            instance.timer.schedule(100);
            RootPanel.get("pageheader").add(instance);
        }
        return instance;
    }
}
