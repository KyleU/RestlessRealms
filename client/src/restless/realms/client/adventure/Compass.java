package restless.realms.client.adventure;

import restless.realms.client.ClientManager;
import restless.realms.client.perspective.MessageType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;

public class Compass extends Composite {
    private static final String IMAGE_URL = "/img/adventure/compass.png";

    private AbsolutePanel body;
    private CompassDirection north, east, south, west; 
    
    public Compass() {
        body = new AbsolutePanel();
        body.setStylePrimaryName("compass");
        initWidget(body);
        Image ball = new Image(IMAGE_URL, 0, 40, 80, 80);
        body.add(ball, 10, 10);
        
        north = new CompassDirection(0);
        body.add(north, 42, 2);

        east = new CompassDirection(1);
        body.add(east, 81, 42);

        south = new CompassDirection(2);
        body.add(south, 42, 81);

        west = new CompassDirection(3);
        body.add(west, 3, 42);
    }
    
    public void setAvailableDirections(boolean showNorth, boolean showEast, boolean showSouth, boolean showWest) {
        north.setEnabled(showNorth);
        east.setEnabled(showEast);
        south.setEnabled(showSouth);
        west.setEnabled(showWest);
    }
    

    private class CompassDirection extends Image {
        private boolean enabled;

        public CompassDirection(final int directionIndex) {
            super(IMAGE_URL, directionIndex * 20, 0, 16, 16);
            this.setStylePrimaryName("direction");
            
            ClickHandler clickHandler = new ClickHandler(){
                @Override
                public void onClick(ClickEvent event) {
                    if(CompassDirection.this.enabled) {
                        ClientManager.send(MessageType.MOVE, directionIndex);
                    }
                }
            };
            this.addClickHandler(clickHandler);

            MouseOutHandler mouseOutHandler = new MouseOutHandler(){
                public void onMouseOut(MouseOutEvent event) {
                    if(CompassDirection.this.enabled) {
                        CompassDirection.this.setVisibleRect(CompassDirection.this.getOriginLeft(), 0, 20, 20);
                    }
                }
            };
            this.addMouseOutHandler(mouseOutHandler);
        }
        
        private void setEnabled(boolean enabled) {
            this.enabled = enabled;
            if(this.enabled) {
                CompassDirection.this.setVisibleRect(CompassDirection.this.getOriginLeft(), 0, 20, 20);
                CompassDirection.this.addStyleDependentName("avail");
            } else {
                CompassDirection.this.setVisibleRect(CompassDirection.this.getOriginLeft(), 20, 20, 20);
                CompassDirection.this.removeStyleDependentName("avail");
            }
        }
    }
}
