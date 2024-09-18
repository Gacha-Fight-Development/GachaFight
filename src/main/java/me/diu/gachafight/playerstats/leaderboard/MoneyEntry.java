package me.diu.gachafight.playerstats.leaderboard;

public class MoneyEntry {
    private final String playerName;
    private final double money;

    public MoneyEntry(String playerName, double money) {
        this.playerName = playerName;
        this.money = money;
    }

    public String getPlayerName() {
        return playerName;
    }

    public double getMoney() {
        return money;
    }
}
