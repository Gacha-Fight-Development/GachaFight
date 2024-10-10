package me.diu.gachafight.playerstats.leaderboard;

public class LevelEntry {
    private final String playerName;
    private final int level;

    public LevelEntry(String playerName, int level) {
        this.playerName = playerName;
        this.level = level;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getLevel() {
        return level;
    }
}