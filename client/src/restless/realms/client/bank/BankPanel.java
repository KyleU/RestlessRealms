package restless.realms.client.bank;

import restless.realms.client.belt.Quickslot;
import restless.realms.client.dragdrop.DragDropManager;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public abstract class BankPanel extends Composite {
    protected int numItems;
    
    protected FlowPanel body;
    protected Quickslot[] contents;

    public BankPanel(int numItems) {
        body = new FlowPanel();
        initWidget(body);
        
        this.numItems = numItems;
        contents = new Quickslot[numItems];
        
        for(int i = 0; i < contents.length; i++) {
            contents[i] = new Quickslot(-(i + 100));
            contents[i].clear();
            DragDropManager.registerStashDroppable(contents[i]);
            contents[i].addStyleName("bankitem");
            body.add(contents[i]);
        }
    }
    
    public Quickslot[] getContents() {
        return contents;
    }
}