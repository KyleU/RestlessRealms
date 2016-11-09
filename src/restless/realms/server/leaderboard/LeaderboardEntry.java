package restless.realms.server.leaderboard;

import restless.realms.server.playercharacter.PlayerCharacter;

public class LeaderboardEntry {
    private final String playerName;
    private final String profession;
    private final Object value;
    
    public LeaderboardEntry(PlayerCharacter player, Object value) {
        this(player.getName(), player.getProfession(), value);
    }

    public LeaderboardEntry(String playerName, String profession, Object value) {
        super();
        this.playerName = playerName;
        this.profession = profession;
        this.value = value;
    }

    public String getPlayerName() {
        return playerName;
    }
    public String getProfession() {
        return profession;
    }
    public Object getValue() {
        return value;
    }
}
