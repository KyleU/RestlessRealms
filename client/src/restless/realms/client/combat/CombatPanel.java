package restless.realms.client.combat;

import java.util.ArrayList;
import java.util.List;

import restless.realms.client.ClientState;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.dragdrop.DragDropManager;
import restless.realms.client.playercharacter.PlayerCharacterHandler;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.widget.WindowPanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;

public class CombatPanel extends WindowPanel<AbsolutePanel> implements PlayerCharacterHandler {
    private int selectionIndex;
    private MobPanel[] mobPanels;
    private final Image selectionImage;

    
    public CombatPanel() {
        super("combat", new AbsolutePanel(), "Combat", null);
        ClientState.addPlayerCharacterHandler(this);
        mobPanels = new MobPanel[4];
        for(int i = 0; i < mobPanels.length; i++) {
            mobPanels[i] = new MobPanel(i);
            body.add(mobPanels[i], i * 191, 0);
            DragDropManager.registerDroppable(mobPanels[i]);
        }
        selectionImage = new Image("img/icon/target.png", 0, 0, 21, 21);
        body.add(selectionImage, 10, 45);
        selectionImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ConsoleUtils.help("You've already selected this creature. Now use a skill or item from your quickslots below.");
            }
        });
    }

    public void setMobs(JsArray<ScriptObject> mobs) {
        for(int i = 0; i < mobs.length(); i++) {
            setMob(i, mobs.get(i));
        }
        setTargetSelectionIndex(getFirstAvailableTargetIndex(), false);
    }

    public void setMob(int index, ScriptObject mob) {
        mobPanels[index].setMob(mob);
        if(mob != null && mob.getInt("hitpoints") == 0) {
            if(index == this.selectionIndex) {
                int firstTargetIndex = getFirstAvailableTargetIndex();
                if(firstTargetIndex != (this.selectionIndex)) {
                    setTargetSelectionIndex(firstTargetIndex, true);
                }
            }
        }
    }
    
    public void clear() {
        for(int i = 0; i < 4; i++) {
            MobPanel mobPanel = mobPanels[i];
            mobPanel.setMob(null);
        }
        setTargetSelectionIndex(-1, false);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void applyEffects(JsArray<ScriptObject> actions, JsArrayString actionNames) {
        List<ScriptObject>[] effectResultsByEnemyIndex = new List[4];
        for(int i = 0; i < actions.length(); i++) {
            ScriptObject action = actions.get(i);
            JsArray<ScriptObject> effectResults = action.getArray("results");
            for(int j = 0; j < effectResults.length(); j++) {
                final ScriptObject effectResult = effectResults.get(j);
                int enemyIndex = effectResult.getInt("target") - CombatPerspective.getNumAllies();
                if(enemyIndex > -1) {
                    if(effectResultsByEnemyIndex[enemyIndex] == null) {
                        effectResultsByEnemyIndex[enemyIndex] = new ArrayList<ScriptObject>();
                    }
                    effectResultsByEnemyIndex[enemyIndex].add(effectResult);
                }
            }
        }
        
        for(int i = 0; i < effectResultsByEnemyIndex.length; i++) {
            if(effectResultsByEnemyIndex[i] != null) {
                MobPanel targetPanel = mobPanels[i];
                targetPanel.apply(effectResultsByEnemyIndex[i]);
            }
        }
    }

    @Override
    public void onPlayerCharacter(ScriptObject playerCharacter) {
        //no op
    }

    @Override
    public void onAdventurePoints(int adventurePoints) {
        // no op
    }

    public void setBackground(String adventureType) {
        body.getElement().getStyle().setProperty("backgroundImage", "url(img/combat/" + adventureType + "-combat.png)");
    }
    
    public MobPanel[] getMobPanels() {
        return mobPanels;
    }
    
    public int getSelectionIndex() {
        return selectionIndex;
    }
    
    public void setTargetSelectionIndex(int selectionIndex, boolean showHelp) {
        if(showHelp && this.selectionIndex == selectionIndex) {
            ConsoleUtils.help("You've already selected this creature. Now use a skill or item from your quickslots below.");
        }
        this.selectionIndex = selectionIndex;
        body.setWidgetPosition(selectionImage, (selectionIndex * 191) + 10, 45);
    }

    public void moveSelectionIndex(int i) {
        int newIndex = selectionIndex + i;
        if(newIndex >= 0 && newIndex < mobPanels.length) {
            MobPanel mobPanel = mobPanels[newIndex];
            if(mobPanel.isVisible() && mobPanel.getHitpoints().getValue() > 0) {
                setTargetSelectionIndex(newIndex, true);
            } else {
                if(i > 0) {
                    moveSelectionIndex(i + 1);
                } else {
                    moveSelectionIndex(i - 1);
                }
            }
        }
    }

    private int getFirstAvailableTargetIndex() {
        for(int i = 0; i < 4; i++) {
            MobPanel mobPanel = mobPanels[i];
            if(mobPanel.getHitpoints().getValue() > 0) {
                return i;
            }
        }
        return -1;
    }
}
