package restless.realms.client.skill;

import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.action.ActionDetailPanel;
import restless.realms.client.perk.PerkPanel;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.util.AuditManager;
import restless.realms.client.util.ClientScaleOptions;
import restless.realms.client.util.CommonEventHandlers;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.DialogBox;
import restless.realms.client.widget.DialogPanel;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TrainerPanel extends WindowPanel<AbsolutePanel> {
    private final ScrollPanel skillPanel = new ScrollPanel();
    private VerticalPanel skillList = new VerticalPanel();
    
    private final ScrollPanel perkPanel = new ScrollPanel();
    private VerticalPanel perkList = new VerticalPanel();
    
    private final Image shopkeeperImage = new Image("img/shop/trainer.png");

    ServiceCallback loadCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject ret) {
            skillList.clear();
            ScriptObject skillTree = ret.getObject("skills");
            JsArrayString levels = skillTree.keys();
            for(int i = 0; i < levels.length(); i++) {
                String s = levels.get(i);
                if(!s.startsWith("__")) {
                    final Integer level = Integer.parseInt(s);
                    JsArray<ScriptObject> skills = skillTree.getArray(level.toString());
                    for(int j = 0; j < skills.length(); j++) {
                        ScriptObject skill = skills.get(j);
                        final int id = skill.getInt("id");
                        final String name = skill.get("name");
                        final int price = level * 600 * ClientScaleOptions.ECONOMY;
                        ClickHandler clickHandler = new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                ActionDetailPanel.getInstance().clear();
                                train(id, name, price);
                            }
                        };
                        SkillPanel skillPanel = new SkillPanel(skill, clickHandler, false);
                        if(PlayerCharacterCache.getInstance().getLevel() >= level) {
                            skillPanel.setAdditionalInfo("Train for " + price + " gold.");
                        } else {
                            skillPanel.setAdditionalInfo("Level " + level + " required.");
                        }
                        skillList.add(skillPanel);
                    }
                }
            }
            
            perkList.clear();
            JsArray<ScriptObject> availablePerks = ret.getArray("perks");
            for(int j = 0; j < availablePerks.length(); j++) {
                ScriptObject perk = availablePerks.get(j);
                final int id = perk.getInt("id");
                final int level = perk.getInt("minLevel");
                final int price = perk.getInt("msrp");
                final String name = perk.get("name");
                ClickHandler clickHandler = new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        ActionDetailPanel.getInstance().clear();
                        trainPerk(id, name, price);
                    }
                };
                PerkPanel perkPanel = new PerkPanel(perk, clickHandler, false);
                if(PlayerCharacterCache.getInstance().getLevel() >= level) {
                    perkPanel.setAdditionalInfo("Buy for " + price + " tokens.");
                } else {
                    perkPanel.setAdditionalInfo("Level " + level + " required.");
                }
                perkList.add(perkPanel);
            }
            

        }
    };

    private ServiceCallback trainCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            ClientState.setSkills(result.getArray("skills"));
            ClientState.setCurrency(result.getInt("currency"));
            show();
        }
        
        @Override
        public void onFailure(String code, String message) {
            ClientState.getLayout().showPanel("trainer");
            super.onFailure(code, message);
        };
    };
    
    private ServiceCallback trainPerkCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject result) {
            PlayerCharacterCache.getInstance().setEquipment(result.getObject("equipment"));
            ClientState.setPerks(result.getArray("perks"));
            ClientState.setTokens(result.getInt("tokens"));
            show();
        }
        
        @Override
        public void onFailure(String code, String message) {
            ClientState.getLayout().showPanel("trainer");
            super.onFailure(code, message);
        };
    };
    
	public TrainerPanel() {
	    super("trainer", new AbsolutePanel(), "Training Grounds", null);

        Image i = new Image("img/shop/trainer-banner.png");
        body.add(i, 0, 0);

        skillPanel.setStylePrimaryName("skillbuypanel");
        skillPanel.addStyleName("actionlist");

        body.add(skillPanel, 0, 51);
        skillPanel.add(skillList);
		
        perkPanel.setStylePrimaryName("perkbuypanel");
        perkPanel.addStyleName("actionlist");

        body.add(perkPanel, 263, 51);
        perkPanel.add(perkList);

        shopkeeperImage.setWidth("226px");
        shopkeeperImage.setHeight("300px");
        body.add(shopkeeperImage, 526, 0);

        addExitButton(CommonEventHandlers.CLICK_ENTER_CITY);
	}
	
	public void show() {
        skillList.clear();
        skillList.add(new Label("Loading Skills..."));

        ClientState.getLayout().showPanel("trainer");
        AuditManager.audit("city", "trainer", null, null);

        ServiceManager.call("shop", "skills", loadCallback);
	}
	
    private void trainPerk(final int id, String name, int price) {
        String message = "Are you sure you want to buy " + name + " for " + price + " tokens?";
        DialogBox trainPrompt = new DialogBox("Buy Perk", message, "Buy", "Cancel") {
            @Override
            public void onAction(String action) {
                if("Buy".equals(action)) {
                    ServiceManager.call("shop", "trainperk", trainPerkCallback, "perk", id);
                } else if("Cancel".equals(action)) {
                    ClientState.getLayout().showPanel("trainer");
                } 
            }
        };
        DialogPanel.show(trainPrompt);
    }

    private void train(final int id, String name, int price) {
        String message = "Are you sure you want to train " + name + " for " + price + " gold?";
        DialogBox trainPrompt = new DialogBox("Train Skill", message, "Train", "Cancel") {
            @Override
            public void onAction(String action) {
                if("Train".equals(action)) {
                    ServiceManager.call("shop", "train", trainCallback, "skill", id);
                } else if("Cancel".equals(action)) {
                    ClientState.getLayout().showPanel("trainer");
                } 
            }
        };
        DialogPanel.show(trainPrompt);
    }
}