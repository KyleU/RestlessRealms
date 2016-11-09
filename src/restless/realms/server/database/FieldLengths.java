package restless.realms.server.database;

public interface FieldLengths {
    public static final int SUMMARY = 40;
    public static final int DESCRIPTION = 2500;
    public static final int MAIL_CONTENT = 4000;

    public static final int STRING_ID = 20;
    public static final int ENUM_LENGTH = 10;
    
    public static final int ITEM_NAME = 50;
    public static final int SKILL_NAME = 50;

    public static final int STATISTIC_KEY = 40;
    public static final int MOB_ARCHETYPE_NAME = 30;

    public static final int ADVENTURE_ARCHETYPE_NAME = 30;

    public static final int ACHIEVEMENT_NAME = 60;
    public static final int ACHIEVEMENT_DESCRIPTION = 100;

    public static final int QUEST_NAME = 60;
    public static final int QUEST_STATUS = 20;
    public static final int QUEST_PROGRESS = 80;
    public static final int QUEST_TEXT = 4000;
    public static final int SHOP_NAME = 60;

    public static final int PROFESSION_NAME = 30;

    public static final int ACCOUNT_EMAIL = 250;
    public static final int ACCOUNT_NAME = 100;
    public static final int ACCOUNT_ADDITIONAL_INFO = 2500;

    public static final int PLAYER_NAME = 15;
    public static final int QUICKSLOTS = 80;

    public static final int CHAT_CONTENT = 1000;
}