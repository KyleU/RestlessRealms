package restless.realms.server.leaderboard;

public enum LeaderboardType {
    ACHIEVEMENT_SCORE("getByAchievementScore", "Achievement Score", "Score"),

    PVP_DUEL_SCORE("getByDuelScore", "Duel Score", "Score"),
    PVP_OFFENSIVE_WINS("getByPvpOffensiveWins", "Duel Offensive Wins", "Wins"),
    PVP_OFFENSIVE_LOSSES("getByPvpOffensiveLosses", "Duel Offensive Losses", "Losses"),
    PVP_DEFENSIVE_WINS("getByPvpDefensiveWins", "Duel Defensive Wins", "Wins"),
    PVP_DEFENSIVE_LOSSES("getByPvpDefensiveLosses", "Duel Defensive Losses", "Losses"),
    
    LEVEL("getByLevel", "Highest Level", "Level"),
    GOLD("getByGold", "Richest Players", "Gold"),
    KILLS("getByKills", "Total Kills", "Kills"),
    ADVENTURES_COMPLETED("getByAdventuresCompleted", "Completed Adventures", "Adventures"),
    QUESTS_COMPLETED("getByQuestsCompleted", "Completed Quests", "Quests"),
    ITEMS_BOUGHT("getByItemsBought", "Items Bought", "Items Bought"),
    ITEMS_SOLD("getByItemsSold", "Items Sold", "Items Sold"),
    JURAKS("getByJuraks", "Jurak Attacks", "Jurak Kills"),
    BATTLEMASTERY("getByBattlemasters", "Battlemaster Kills", "Kills"),
    
    PHYSICAL_DAMAGE("getByPhysicalDamage", "Physical Mastery", "Physical Dmg"),
    FIRESTARTER("getByFireDamage", "Firestarters", "Fire Damage"),
    ELECTRIFYING("getByElectricDamage", "Electrifying", "Electric Dmg"),
    ICE_DAMAGE("getByIceDamage", "Ice Damage", "Ice Damage"),
    HEALBOT("getByHealingDamage", "Healbots", "Damage Healed"),
    GAUNTLET("getByGauntlet", "Gauntlet Runs", "Completed"),

    ADVENTURES_ABANDONED("getByAdventuresAbandoned", "Abandoned Adventures", "Adventures"),
    DEATHS("getByDeaths", "Deaths", "Deaths"),
    ;
    
    private final String queryName;
    private final String title;
    private final String valueTitle;

    private LeaderboardType(String queryName, String title, String valueTitle) {
        this.queryName = queryName;
        this.title = title;
        this.valueTitle = valueTitle;
    }

    public String getQueryName() {
        return queryName;
    }

    public String getTitle() {
        return title;
    }

    public String getValueTitle() {
        return valueTitle;
    }
}
