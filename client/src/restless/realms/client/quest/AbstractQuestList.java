package restless.realms.client.quest;

import restless.realms.client.ServiceManager;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.playercharacter.PlayerCharacterCache;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.util.ServiceCallback;
import restless.realms.client.widget.ButtonPanel;
import restless.realms.client.widget.Link;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class AbstractQuestList extends WindowPanel<AbsolutePanel> {
    protected final ScrollPanel activeQuestsBody = new ScrollPanel();
    protected final VerticalPanel activeQuestsList = new VerticalPanel();

    protected QuestDetailPanel questDetailPanel = new QuestDetailPanel();
    protected Link activeLink;
    
    protected String unavailableText;

    protected ServiceCallback detailsCallback;

    protected ServiceCallback loadCallback = new ServiceCallback() {
        @Override
        public void onSuccess(ScriptObject ret) {
            activeQuestsList.clear();
            if(body.getElement().getId().equals("quests")) {
                ButtonPanel buttonPanel = new ButtonPanel("Kale's Journal", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        JournalPanel.show();
                    }
                }, 2);
                FlowPanel fp = new FlowPanel();
                fp.getElement().getStyle().setPadding(5, Unit.PX);
                fp.getElement().getStyle().setProperty("margin", "auto");
                fp.getElement().getStyle().setProperty("textAlign", "center");
                buttonPanel.getElement().getStyle().setProperty("margin", "auto");
                fp.add(buttonPanel);
                activeQuestsList.add(fp);
            }
            
            JsArray<ScriptObject> quests = ret.getArray("quests");
            if(quests.length() == 0) {
                questDetailPanel.clear(unavailableText);
            } else {
                if(body.getElement().getId().equals("questboard")) {
                    ButtonPanel buttonPanel = new ButtonPanel("Accept All Quests", new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            ServiceManager.call("quest", "acceptall", new ServiceCallback() {
                                @Override
                                public void onSuccess(ScriptObject result) {
                                    JsArray<ScriptObject> accepted = result.getArray("accepted");
                                    for(int i = 0; i < accepted.length(); i++) {
                                        ScriptObject acceptedQuest = accepted.get(i);
                                        ConsoleUtils.help("You have accepted the quest " + acceptedQuest.get("name") + ".");
                                    }
                                    ConsoleUtils.help("You have accepted " + accepted.length() + " new " + (accepted.length() == 1 ? "quest" : "quests") + ".");
                                    loadCallback.onSuccess(result);
                                }
                            });
                        }
                    }, 2);
                    FlowPanel fp = new FlowPanel();
                    fp.getElement().getStyle().setPadding(5, Unit.PX);
                    fp.getElement().getStyle().setProperty("margin", "auto");
                    fp.getElement().getStyle().setProperty("textAlign", "center");
                    buttonPanel.getElement().getStyle().setProperty("margin", "auto");
                    fp.add(buttonPanel);
                    activeQuestsList.add(fp);
                }

                for(int i = 0; i < quests.length(); i++) {
                    ScriptObject quest = quests.get(i);
                    
                    final String id = quest.get("id");
                    ClickHandler clickHandler = new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            if(activeLink != null) {
                                activeLink.deactivate();
                            }
                            Link l = ((Link)event.getSource());
                            l.activate();
                            activeLink = l;
                            ServiceManager.call("quest", "details", detailsCallback, "id", id);
                        }
                    };
    
                    String text = "";
                    if(quest.hasKey("currentProgress")) {
                        int currentProgress = quest.getInt("currentProgress");
                        int completionQuantity = quest.getInt("completionQuantity");
                        if(currentProgress == completionQuantity) {
                            text += "<div style='float:right;padding-right:5px;'><img src='img/icon/checkmark.png' /></div>";
                        } else {
                            text += "<div style='float:right;padding-right:5px;color:#b9b9b9;'>" + currentProgress + "/" + completionQuantity + "</div>";
                        }
                    }
                    text += quest.get("name");
                    Link questName = new Link(text, true, null, clickHandler);
                    questName.getElement().getStyle().setColor(getColor(quest.getInt("suggestedLevel")));
                    activeQuestsList.add(questName);
                }
            }
        }
    };

    public AbstractQuestList(String id, String title) {
        super(id, new AbsolutePanel(), title, null);

        activeQuestsBody.setStylePrimaryName("questlistpanel");

        body.add(activeQuestsBody, 0, 0);
        activeQuestsBody.add(activeQuestsList);
        
        questDetailPanel.clear("Select a quest to view details.");
        body.add(questDetailPanel, 190, 0);

        clear();
    }
    
    public abstract void load();
    public abstract void show();

    public void clear() {
        activeLink = null;
        activeQuestsList.clear();
        activeQuestsList.add(new Label("Loading Quests..."));
        questDetailPanel.clear("Select a quest to view details.");
    }

    private String getColor(int suggestedLevel) {
        String ret;
        int delta = suggestedLevel - PlayerCharacterCache.getInstance().getLevel();
        if(delta < -2) {
            ret = "#33ca42";
        } else if(delta < 0) {
            ret = "#00c6ff";
        } else if(delta == 0) {
            ret = "#ffffff";
        } else if(delta < 3) {
            ret = "#fff600";
        } else {
            ret = "#fa0000";
        }
        return ret;
    }
}