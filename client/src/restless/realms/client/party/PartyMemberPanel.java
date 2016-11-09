package restless.realms.client.party;

import java.util.List;

import restless.realms.client.animation.EffectResultAnimation;
import restless.realms.client.console.ConsoleUtils;
import restless.realms.client.util.ScriptObject;
import restless.realms.client.widget.CompositeImage;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;

public class PartyMemberPanel extends Composite {
    private AbsolutePanel body;
    private CompositeImage portrait;
    
    public PartyMemberPanel() {
        body = new AbsolutePanel();
        body.setStylePrimaryName("partymember");
        initWidget(body);
        portrait = new CompositeImage(6, "img/paperdoll/male/background/empty.png", 185, 215);
    }
    
    public void apply(List<ScriptObject> effectResults) {
        //body.clearRecurringEffects();

        for(int i = 0; i < effectResults.size(); i++) {
            ScriptObject effectResult = effectResults.get(i);

            int additionalRounds = effectResult.getInt("additionalRounds");
            if(additionalRounds > 0) {
                // body.addRecurringEffectsIcon(effectResult.get("type"), additionalRounds);
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

    public void setPartyMember(ScriptObject partyMember) {
        if(partyMember == null) {
            this.setVisible(false);
            portrait.clear();
        } else {
            this.setVisible(true);
            ConsoleUtils.error("Party Member: " + partyMember.toDebugString());
            setPortraitImages(partyMember.get("images"));
        }
    }

    public void setPortraitImages(String images) {
        String[] imageArray = images.split(",");
        assert imageArray.length == 6 : imageArray.length; 
        for(int i = 0; i < imageArray.length; i++) {
            String url = "img/paperdoll/" + imageArray[i] + ".png";
            portrait.setUrl(i, url);
        }
    }
}
