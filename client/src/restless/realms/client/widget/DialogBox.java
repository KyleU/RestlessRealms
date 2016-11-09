package restless.realms.client.widget;

public class DialogBox {
    private final String title;
    private final String contentHtml;
    private final String[] buttons;

    public DialogBox(String title, String contentHtml, String... buttons) {
        this.title = title;
        this.contentHtml = contentHtml;
        this.buttons = buttons;
    }
    
    public void onAction(String action) {
        throw new IllegalArgumentException("Unhandled dialog box action \"" + action + "\"");
    }

    public String getTitle() {
        return title;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public String[] getButtons() {
        return buttons;
    }
}
