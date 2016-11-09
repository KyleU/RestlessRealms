package restless.realms.server.leaderboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Leaderboard implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LeaderboardType type;

    private final List<LeaderboardEntry> entries;
    
    public Leaderboard(LeaderboardType type) {
        this.type = type;
        this.entries = new ArrayList<LeaderboardEntry>();
    }
    
    public void add(LeaderboardEntry value) {
        this.entries.add(value);
    }
    
    public LeaderboardType getType() {
        return type;
    }
    
    public List<LeaderboardEntry> getEntries() {
        return entries;
    }
}
