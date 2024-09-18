package me.diu.gachafight.playerstats.leaderboard;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerDataManager;
import me.diu.gachafight.playerstats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MoneyLeaderboard {

    private final GachaFight plugin;
    private final PlayerDataManager playerDataManager;
    private Map<UUID, Double> moneyData;
    private List<MoneyEntry> leaderboard;
    private File leaderboardFile;
    private FileConfiguration leaderboardConfig;

    public MoneyLeaderboard(GachaFight plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        this.moneyData = new HashMap<>();
        this.leaderboard = new ArrayList<>();

        // Create and load the leaderboard config file
        leaderboardFile = new File(plugin.getDataFolder(), "leaderboard.yml");
        if (!leaderboardFile.exists()) {
            try {
                leaderboardFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        leaderboardConfig = YamlConfiguration.loadConfiguration(leaderboardFile);

        // Load the leaderboard data from the config file
        loadLeaderboard();

        // Schedule leaderboard updates every 20 minutes
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateLeaderboard, 0L, 24000L); // 24000 ticks = 20 minutes

        // Schedule save tasks 10 minutes before the leaderboard updates
        Bukkit.getScheduler().runTaskTimer(plugin, this::saveAllOnlinePlayers, 0L, 12000L); // 12000 ticks = 10 minutes
    }

    // Save all online players' money data 10 minutes before leaderboard updates
    private void saveAllOnlinePlayers() {
        playerDataManager.saveAll();

        // Update the moneyData map with current online players' data
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerStats stats = PlayerStats.getPlayerStats(player);
            if (stats.getMoney() > 1) {
                moneyData.put(player.getUniqueId(), stats.getMoney());
            }
        }

        saveAllMoneyData();  // Save the updated money data to the config file
    }

    // Update the leaderboard by sorting the players by money
    private void updateLeaderboard() {
        leaderboard = moneyData.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new MoneyEntry(Bukkit.getOfflinePlayer(entry.getKey()).getName(), entry.getValue()))
                .collect(Collectors.toList());

        saveAllMoneyData();  // Save the updated leaderboard to the config file
        plugin.getLogger().info("Leaderboard updated!");
    }


    // Save all money data to the config file
    private void saveAllMoneyData() {
        leaderboardConfig.set("players", null); // Clear the current data

        for (UUID uuid : moneyData.keySet()) {
            leaderboardConfig.set("players." + uuid + ".money", moneyData.get(uuid));
            leaderboardConfig.set("players." + uuid + ".name", Bukkit.getOfflinePlayer(uuid).getName());
        }

        try {
            leaderboardConfig.save(leaderboardFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load all money data from the config file
    private void loadLeaderboard() {
        if (leaderboardConfig.contains("players")) {
            for (String uuidString : leaderboardConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                double money = leaderboardConfig.getDouble("players." + uuidString + ".money");

                moneyData.put(uuid, money);
            }
        }
    }

    // Get the top 5 leaderboard entries
    public List<MoneyEntry> getTop5() {
        return leaderboard;
    }

    // Get a player's placement on the leaderboard
    public int getPlayerPlacement(UUID playerUUID) {
        List<Map.Entry<UUID, Double>> sortedMoneyData = moneyData.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .collect(Collectors.toList());

        for (int i = 0; i < sortedMoneyData.size(); i++) {
            if (sortedMoneyData.get(i).getKey().equals(playerUUID)) {
                return i + 1; // Rank starts at 1
            }
        }
        return -1; // Not on the leaderboard
    }

    // Display the leaderboard in chat
    public void displayLeaderboard(Player player) {
        player.sendMessage("§a§lTop 5 Money Leaderboard:");
        int rank = 1;
        for (MoneyEntry entry : leaderboard) {
            player.sendMessage("§6" + rank + ". " + entry.getPlayerName() + " - §e" + String.format("%.2f", entry.getMoney()));
            rank++;
        }

        // Show the player's rank if they are on the leaderboard
        int playerRank = getPlayerPlacement(player.getUniqueId());
        if (playerRank > 5) {
            player.sendMessage("§7Your Rank: §6" + playerRank);
        }
    }

}
