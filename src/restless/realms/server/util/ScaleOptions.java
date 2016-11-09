package restless.realms.server.util;


public interface ScaleOptions {
    public static final int ECONOMY = 10;
    public static final int COMBAT = 10;
    public static final int QUEST_XP_MULTIPLIER = 6;
    
    public static final int MOB_XP_PER_LEVEL = 100;
    public static final int MAX_PLAYER_LEVEL = 40;
    public static final int MAX_LEVEL = 50;
    public static final int STARTING_APS = 5;
    
    public static final int MAX_PARTY_SIZE = 4;
    
    public static final int DUEL_OFFENSIVE_WIN_MULTIPLIER = 10;
    public static final int DUEL_OFFENSIVE_LOSS_MULTIPLIER = -10;
    public static final int DUEL_DEFENSIVE_WIN_MULTIPLIER = 20;
    public static final int DUEL_DEFENSIVE_LOSS_MULTIPLIER = -3;

    // also change TrainerPanel
    public static final int SKILL_COST_PER_LEVEL = 600;
}