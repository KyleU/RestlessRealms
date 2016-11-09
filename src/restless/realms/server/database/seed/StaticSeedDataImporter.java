package restless.realms.server.database.seed;

import java.util.Date;

import restless.realms.server.chat.ChatMessage;
import restless.realms.server.session.Session;

public class StaticSeedDataImporter {
    public static final String TEST_SESSION_ID = "00000000-0000-0000-0000-000000000000";

    private PersistanceProvider persistanceProvider;
    
    public StaticSeedDataImporter(PersistanceProvider persistanceProvider) {
        this.persistanceProvider = persistanceProvider;
    }
    
    public void apply() {
        applySessions();
        applyChatMessages();
    }
    
    private void applySessions() {
        persistanceProvider.save(TEST_SESSION_ID, new Session(TEST_SESSION_ID, 1, "Test User", new Date(), true));
    }

    private void applyChatMessages() {
        persistanceProvider.save("chat1", new ChatMessage(null, "Global", "Kyle", "Hey, thanks for helping us test. I had to reset the database, so you'll need to retrain your skills and buy some new gear.", new Date()));
    }
}