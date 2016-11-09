package restless.realms.client.quest;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.action.ActionIcon;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.room.LevelUpPanel;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class QuestCompletionPanel extends WindowPanel<ScrollPanel> {
    private VerticalPanel content = new VerticalPanel();
    
    private ClickHandler questsClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            ClientManager.send(MessageType.QUESTS_OPEN);
        }
    };
    
    public QuestCompletionPanel() {
        super("questcompletion", new ScrollPanel(), "Quest Complete!", null);
        body.add(content);
        addExitButton(questsClickHandler);
    }
    
    public void onResult(ScriptObject result) {
        ScriptObject pc = result.getObject("pc");
        int oldLevel = PlayerCharacterCache.getInstance().getLevel();
        int newLevel = pc.getInt("level");
        
        ScriptObject inventory = result.getObject("inventory");
        int currency = inventory.getInt("currency");
        int tokens = inventory.getInt("tokens");
        JsArray<ScriptObject> items = inventory.getArray("items");

        ClientState.setPlayerCharacter(pc);
        ClientState.setCurrency(currency);
        ClientState.setTokens(tokens);
        ClientState.setItems(items);
        
        content.clear();
        ScriptObject questDetails = result.getObject("questDetails");
        
        ScriptObject quest = questDetails.getObject("quest");
        ScriptObject treasure = questDetails.getObject("treasure");
        
        Label nameLabel = new Label(quest.get("name"));
        nameLabel.setStylePrimaryName("largetext");
        content.add(nameLabel);
        content.add(new Label("\"" + quest.get("completionText") + "\""));
        content.add(new HTML("&nbsp;"));
        content.add(new Label("You've Received:"));
        content.add(new HTML("&nbsp;"));
        content.add(new Label(treasure.getInt("currency") + " Gold."));
        content.add(new Label(quest.getInt("rewardXp") + " Experience."));
        
        JsArray<ScriptObject> rewardItems = treasure.getArray("items");
        if(rewardItems.length()  > 0) {
            HorizontalPanel rewardItemsPanel = new HorizontalPanel();
            for(int i = 0; i < rewardItems.length(); i++) {
                ScriptObject rewardItem = rewardItems.get(i);
                ActionIcon actionIcon = new ActionIcon();
                actionIcon.getElement().getStyle().setMarginRight(5, Unit.PX);
                actionIcon.show("item", rewardItem, true);
                rewardItemsPanel.add(actionIcon);
                rewardItemsPanel.setCellWidth(actionIcon, "50px");
            }
            content.add(rewardItemsPanel);
        }

        content.add(new HTML("&nbsp;"));
        content.add(new ButtonPanel("Thanks", questsClickHandler, 1));
        
        if(newLevel != oldLevel) {
            LevelUpPanel.show(new Runnable() {
                @Override
                public void run() {
                    ClientState.getLayout().showPanel("questcompletion");
                }
            });
        } else {
            ClientState.getLayout().showPanel("questcompletion");
        }
    }
}
