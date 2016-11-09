package restless.realms.client.widget;

import restless.realms.client.ClientState;
import restless.realms.client.layout.PanelLocation;
import restless.realms.client.playercharacter.PlayerCharacterCache;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

public class PurchasePanel extends WindowPanel<AbsolutePanel> {
    private static final String OFFERPAL_URL = "http://pub.myofferpal.com/68fbf4707fd2cb410e567af9b1637518/showoffers.action";
    private static PurchasePanel instance;
    private static String lastObservedPanel;

    public static ClickHandler openClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            show();
        }
    };
    
    private ClickHandler offersClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            Integer accountId = ClientState.getAccountId();
            assert accountId > 0;
            String name = PlayerCharacterCache.getInstance().getName();
            name = name.replaceAll(" ", "_");
            String params = "?snuid=" + accountId + "&affl=" + name;
            Window.Location.assign(OFFERPAL_URL + params);
        }
    };
    
    public PurchasePanel() {
        super("purchase", new AbsolutePanel(), "Purchase Adventure Points", null);
        
        HTML offerpal = new HTML();
        int accountId = ClientState.getAccountId();

        String name = PlayerCharacterCache.getInstance().getName();
        name = name.replaceAll(" ", "_");

        String iframe = "<iframe src=\"http://ad110.myofferpal.com/68fbf4707fd2cb410e567af9b1637518/cbanner.action?snuid=" + accountId  + "&affl=" + name + "&scrollBarType=1\" style=\"width:666px;height:100px;background-color:#000;\"></iframe>";
        offerpal.setHTML(iframe);
        body.add(offerpal, 42, 0);
        
        ButtonPanel offers = new ButtonPanel("View Offers", offersClickHandler , 2);
        body.add(offers, 310, 120);
        
        addExitButton(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                assert lastObservedPanel != null;
                ClientState.getLayout().showPanel(lastObservedPanel);
            }
        });
    }
    
    public static void show() {
        if(instance == null) {
            instance = new PurchasePanel();
            ClientState.getLayout().addPanel(PanelLocation.MAIN, "purchase", instance);
        }

        String mainWindowPanelKey = ClientState.getLayout().getMainWindowPanelKey();
        if(!"purchase".equals(mainWindowPanelKey)) {
            lastObservedPanel = mainWindowPanelKey;
        }
        ClientState.getLayout().showPanel("purchase");
    }

    public static void addLink() {
        Anchor purchaseLink = new Anchor("Purchase APs");
        purchaseLink.setStylePrimaryName("link");
        purchaseLink.addStyleName("purchaselink");
        purchaseLink.addClickHandler(PurchasePanel.openClickHandler);
        RootPanel.get("pageheader").add(purchaseLink);
    }

}
