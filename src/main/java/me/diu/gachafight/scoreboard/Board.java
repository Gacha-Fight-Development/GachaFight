package me.diu.gachafight.scoreboard;

import me.clip.placeholderapi.PlaceholderAPI;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerDataManager;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.di.ServiceLocator;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class Board {
    private final GachaFight plugin;
    private final PlayerDataManager playerDataManager;
    public Board(ServiceLocator serviceLocator) {
        this.plugin = GachaFight.getInstance();
        this.playerDataManager = serviceLocator.getService(PlayerDataManager.class);
    }

    public void setScoreBoard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("Scoreboard", "dummy",
                ColorChat.chat("&x&F&F&0&0&D&9G&x&F&F&1&C&C&1a&x&F&F&3&7&A&9c&x&F&F&5&3&9&1h&x&F&F&6&E&7&9a&x&F&F&8&A&6&0F&x&F&F&A&5&4&8i&x&F&F&C&1&3&0g&x&F&F&D&C&1&8h&x&F&F&F&8&0&0t"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        PlayerStats stats = playerDataManager.getPlayerStats(player.getUniqueId());
        if (stats == null) {
            playerDataManager.loadPlayerData(player);
            stats = playerDataManager.getPlayerStats(player.getUniqueId());
        }
        String rawMoneyText = PlaceholderAPI.setPlaceholders(player, "%vault_eco_balance%");
        double balance;
        try {
            balance = Double.parseDouble(rawMoneyText);
        } catch (NumberFormatException e) {
            balance = 0.0; // Default to 0 if parsing fails
        }

        String formattedMoney = String.format("&a$ %.2f", Math.round(balance * 100.0) / 100.0);
        Score moneyScore = obj.getScore(ColorChat.chat(formattedMoney));
        moneyScore.setScore(5);

        Score gemScore = obj.getScore(ColorChat.chat("&a❖" + stats.getGem()));
        gemScore.setScore(4);

        Score discordScore = obj.getScore(ColorChat.chat("&3/Discord"));
        discordScore.setScore(3);

        Score serverScore = obj.getScore(ColorChat.chat("&bGachaFight.minehut.gg"));
        serverScore.setScore(2);
        player.setScoreboard(board);
    }
}
