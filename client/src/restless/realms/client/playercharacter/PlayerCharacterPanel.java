package restless.realms.client.playercharacter;

import java.util.List;

import restless.realms.client.ClientState;
import restless.realms.client.animation.EffectResultAnimation;
import restless.realms.client.animation.FadeInAnimation;
import restless.realms.client.animation.FadeOutAnimation;
import restless.realms.client.dragdrop.DragDropManager;
import restless.realms.client.inventory.InventoryHandler;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;

public class PlayerCharacterPanel extends WindowPanel<PlayerCharacterDisplay> implements PlayerCharacterHandler, InventoryHandler {
    
	private FadeInAnimation detailsFadeInAnimation;
    private FadeOutAnimation detailsFadeOutAnimation;

    public PlayerCharacterPanel() {
	    super("player", new PlayerCharacterDisplay(true), "", null);
        ClientState.addPlayerCharacterHandler(this);
        ClientState.addInventoryHandler(this);

        initMouseHandlers();

        addTitleIcon(190, "img/icon/filters.png", 14 * 16, "Portrait", null);
        addTitleIcon(210, "img/icon/filters.png", 15 * 16, "Achievements", "achievement");

        DragDropManager.registerDroppable(this);
        
        this.detailsFadeInAnimation = new FadeInAnimation(body.getDetailsLink());
        this.detailsFadeOutAnimation = new FadeOutAnimation(body.getDetailsLink());
	}

    @Override
    public void onPlayerCharacter(ScriptObject playerCharacter) {
        if(playerCharacter.hasKey("name")) {
            setWindowTitle(playerCharacter.get("name"));
        }
        body.setPlayerCharacter(playerCharacter);
    }
	
	@Override
	public void onAdventurePoints(int adventurePoints) {
	    // no op
	}
	
	@Override
	public void applyEffects(JsArray<ScriptObject> actions, JsArrayString actionNames) {
        body.clearRecurringEffects();
        for(int i = 0; i < actions.length(); i++) {
            ScriptObject action = actions.get(i);
            JsArray<ScriptObject> effectResults = action.getArray("results");
            
    	    for(int j = 0; j < effectResults.length(); j++) {
                final ScriptObject effectResult = effectResults.get(j);
                if(effectResult.getInt("target") == 0) {
                    int additionalRounds = effectResult.getInt("additionalRounds");
                    if(additionalRounds > 0) {
                        body.addRecurringEffectsIcon(effectResult.get("type"), additionalRounds);
                    }
    
                    final EffectResultAnimation animation = new EffectResultAnimation(body.getElement(), 100, 0, effectResult);
                    if(i == 0) {
                        animation.run();
                    } else {
                        Timer t = new Timer() {
                            @Override
                            public void run() {
                                animation.run();
                            }
                        };
                        t.schedule(500 * i);
                    }
                }
            }
        }
	}
	
    @Override
    public void onCurrency(int currency) {
        //no op
    }
    
    @Override
    public void onTokens(int currency) {
        //no op
    }
    
	@Override
	public void onItems(JsArray<ScriptObject> items) {
	    List<Integer> ids = PlayerCharacterCache.getInstance().getEquipmentIds();
	    for(int i = 0; i < items.length(); i++) {
	        if(ids.size() > 0) {
	            ScriptObject item = items.get(i);
	            int id = item.getInt("id");
                if(ids.contains(id)) {
	                ids.remove((Integer)id);
	                String type = item.get("type");
                    if("HEAD".equals(type)) {
                        body.setEquipment(0, item);
                    } else if("CHEST".equals(type)) {
                        body.setEquipment(1, item);
                    } else if("LEGS".equals(type)) {
                        body.setEquipment(2, item);
                    } else if("ACCESSORY".equals(type)) {
                        body.setEquipment(3, item);
                    } else if("WEAPON".equals(type)) {
                        body.setEquipment(4, item);
                    } else {
                        assert false : type;
                    }
	            }
	        }
        }

        List<Integer> perkIds = PlayerCharacterCache.getInstance().getPerkIds();
        assert perkIds.size() == 2 : perkIds.size();
        
        Integer perkId = perkIds.get(0);
        if(perkId != null) {
            ScriptObject perk = PlayerCharacterCache.getInstance().getPerk(perkId);
            body.setEquipment(5, perk);
        }

        perkId = perkIds.get(1);
        if(perkId != null) {
            ScriptObject perk = PlayerCharacterCache.getInstance().getPerk(perkId);
            body.setEquipment(6, perk);
        }
	}
	
    @Override
    public void onPerks(JsArray<ScriptObject> perks) {
        //no op
    }

    private void initMouseHandlers() {
        MouseOverHandler mouseOverHandler = new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                body.getXpProgressBar().fadeIn();
                ClientState.getLayout().getBeltPanel().fadeIn();
                detailsFadeInAnimation.run(1000);
            }
        };
        this.addDomHandler(mouseOverHandler, MouseOverEvent.getType());
        
        MouseOutHandler mouseOutHandler = new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                body.getXpProgressBar().fadeOut();
                ClientState.getLayout().getBeltPanel().fadeOut();
                detailsFadeInAnimation.cancel();
                detailsFadeOutAnimation.run(1000);
            }
        };
        this.addDomHandler(mouseOutHandler, MouseOutEvent.getType());
    }

    public void clearRecurringEffects() {
        body.clearRecurringEffects();
    }
}
