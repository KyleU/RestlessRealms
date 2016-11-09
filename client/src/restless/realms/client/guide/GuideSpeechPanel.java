package restless.realms.client.guide;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;

public class GuideSpeechPanel extends Composite {
    HorizontalPanel body;
    Label speechText;

    public GuideSpeechPanel() {
        
        body = new HorizontalPanel();
        
        ScrollPanel scrollPanel = new ScrollPanel(body);
        initWidget(scrollPanel);
        
        speechText = new Label();
        speechText.setStylePrimaryName("windowcontent");
        body.add(speechText);

        //Image guidePic = new Image("img/interface/guide.png");
        //body.add(guidePic);
    }
    
    public void setSpeechText(String text) {
        speechText.setText(text);
    }
}
