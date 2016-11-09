package restless.realms.client.quest;

import restless.realms.client.ClientManager;
import restless.realms.client.ClientState;
import restless.realms.client.ServiceManager;
import restless.realms.client.action.ActionIcon;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.perspective.MessageType;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.DialogBox;
import restless.realms.client.widget.DialogPanel;
import restless.realms.client.worldmap.MapLocation;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class QuestDetailPanel extends Composite {
    private final AbsolutePanel body;

    private final ScrollPanel questTextContainer;
    private final VerticalPanel questDetail;
    private final Label questName;
    private final Label questProgress;

    private static final ServiceCallback acceptCallback = new ServiceCallback() {        
        @Override
        public void onSuccess(ScriptObject result) {
            ConsoleUtils.help("You have accepted the quest " + result.get("name") + ".");
            ClientManager.send(MessageType.ENTER_LOCATION, "questboard");
        }
        public void onFailure(String code, String message) {
            super.onFailure(code, message);
            ClientManager.send(MessageType.ENTER_LOCATION, "questboard");
        };
    };

    private static final ServiceCallback completeCallback = new QuestCompletionCallback();
    private static final ServiceCallback abandonCallback = new QuestAbandonCallback();

    public QuestDetailPanel() {
        body = new AbsolutePanel();
        body.setStylePrimaryName("questdetail");
        initWidget(body);
        
        questName = new Label();
        questName.setStylePrimaryName("questname");
        body.add(questName, 0, 0);

        questProgress = new Label();
        questProgress.setStylePrimaryName("questprogress");
        body.add(questProgress, 245, 12);

        questTextContainer = new ScrollPanel();
        questTextContainer.setStylePrimaryName("questtext");
        body.add(questTextContainer, 0, 45);
        
        ScrollPanel questDetailList = new ScrollPanel();
        questDetailList.setStylePrimaryName("questdetaillist");
        questDetail = new VerticalPanel();
        questDetailList.add(questDetail);
        body.add(questDetailList, 285, 45);
    }
    
    public void setQuestDetails(ScriptObject details, String mode) {
        clear(null);
        ScriptObject quest = details.getObject("quest");
        
        final String questId = quest.get("id");
        int completionQuantity = quest.getInt("completionQuantity");
        boolean showButton = true;
        
        questName.setText(quest.get("name"));
        
        Label textLabel = new Label("\"" + quest.get("introText") + "\"");
        questTextContainer.add(textLabel);
        
        if(details.hasKey("progress")) {
            ScriptObject progress = details.getObject("progress");
            if(progress.get("currentStatus").equals("ACTIVE")) {
                int currentProgress = progress.getInt("currentProgress");
                String progressMessage = quest.get("progressText") + " - " + currentProgress + " / " + completionQuantity;
                questProgress.setText(progressMessage );
                showButton = (currentProgress == completionQuantity);
            }
        }

        String adventure = quest.get("completionAdventure");
        String locationName = null;
        for(MapLocation location : ClientState.getMapLocations()) {
            if(location.getId().equals(adventure)) {
                locationName = location.getName();
                break;
            }
        }
        questDetail.add(new Label("Location: " + locationName));
        int level = quest.getInt("suggestedLevel");
        if(level > 1) {
            questDetail.add(new Label("Level " + level + " or higher suggested."));
        }
        questDetail.add(new HTML("<div class=\"spacer\">&nbsp;</div>"));
        
        if(quest.hasKey(("completionItem"))) {
            questDetail.add(new Label("Required Item:"));
            ScriptObject completionItem = details.getObject("completionItem");
            HorizontalPanel horizontalPanel = new HorizontalPanel();
            ActionIcon actionIcon = new ActionIcon();
            actionIcon.show("item", completionItem, true);
            horizontalPanel.add(actionIcon);
            Label label = new Label(" x" + completionQuantity);
            label.getElement().getStyle().setPaddingTop(30, Unit.PX);
            label.getElement().getStyle().setPaddingLeft(5, Unit.PX);
            horizontalPanel.add(label);
            questDetail.add(horizontalPanel);
        } else if(quest.hasKey(("completionMobArchetype"))) {
            questDetail.add(new Label("Required Kill:"));
            String mobName = details.get("completionMobName");
            questDetail.add(new Label(mobName + " x" + completionQuantity + "."));
        } else {
            assert false;
        }
        questDetail.add(new HTML("<div class=\"spacer\">&nbsp;</div>"));

        ScriptObject treasure = details.getObject("treasure");
        JsArray<ScriptObject> items = treasure.getArray("items");
        questDetail.add(new Label("Reward:"));
        questDetail.add(new Label(treasure.getInt("currency") + " gold and " + quest.get("rewardXp") + " experience."));
        
        if(items.length()  > 0) {
            HorizontalPanel itemsPanel = new HorizontalPanel();
            for(int i = 0; i < items.length(); i++) {
                ScriptObject item = items.get(i);
                ActionIcon actionIcon = new ActionIcon();
                actionIcon.getElement().getStyle().setMarginRight(5, Unit.PX);
                actionIcon.show("item", item, true);
                itemsPanel.add(actionIcon);
                itemsPanel.setCellWidth(actionIcon, "50px");
            }
            questDetail.add(itemsPanel);
        }
        
        if(showButton) {
            questDetail.add(new HTML("<div class=\"spacer\">&nbsp;</div>"));
            if("accept".equals(mode)) {
                questDetail.add(new ButtonPanel("Accept Quest", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        DialogBox dialogBox = new DialogBox("", "Accepting Quest...", (String[])null);
                        DialogPanel.show(dialogBox);
                        ServiceManager.call("quest", "accept", acceptCallback , "id", questId);
                    }
                }, 2));
            } else if("complete".equals(mode)) {
                questDetail.add(new ButtonPanel("Complete Quest", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        DialogBox dialogBox = new DialogBox("", "Completing Quest...", (String[])null);
                        DialogPanel.show(dialogBox);
                        ServiceManager.call("quest", "complete", completeCallback, "id", questId);
                    }
                }, 2));
            } else if("active".equals(mode)) {
                questDetail.add(new Label("Finish your combat before completing this quest."));
            } else {
                assert false;
            }
        } else {
            if("complete".equals(mode)) {
                questDetail.add(new ButtonPanel("Abandon Quest", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        DialogBox dialogBox = new DialogBox("", "Abandoning Quest...", (String[])null);
                        DialogPanel.show(dialogBox);
                        ServiceManager.call("quest", "abandon", abandonCallback, "id", questId);
                    }
                }, 2));
            }
        }
    }

    public void clear(String message) {
        questName.setText("");
        questTextContainer.clear();
        questProgress.setText("");
        questDetail.clear();
        if(message != null) {
            questTextContainer.add(new Label(message));
        }
    }
}
