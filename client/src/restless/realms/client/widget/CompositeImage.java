package restless.realms.client.widget;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;

public class CompositeImage extends Composite {
    private String emptyImage;
    private final AbsolutePanel body;
    private final Image[] images;
    
    public CompositeImage(int numImages, String initialImageUrl, int width, int height) {
        body = new AbsolutePanel();
        body.setPixelSize(width, height);
        initWidget(body);
        this.emptyImage = initialImageUrl;
        images = new Image[numImages];
        for(int i = 0; i < images.length; i++) {
            Image img = new Image(emptyImage);
            img.setPixelSize(width, height);
            body.add(img, 0, 0);
            images[i] = img;
        }
    }
    
    public void setUrl(int index, String url) {
        if(url == null) {
            url = emptyImage;
        }
        images[index].setUrl(url);
    }

    public void addClickHandler(ClickHandler clickHandler) {
        images[images.length - 1].addClickHandler(clickHandler);
    }
    
    @Override
    public void setStylePrimaryName(String name) {
        for(Image i : images) {
            i.setStylePrimaryName(name);
        }
    }

    public void clear() {
        for(int i = 0; i < images.length; i++) {
            setUrl(i, null);
        }
    }
}
