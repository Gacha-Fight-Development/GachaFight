package me.diu.gachafight.playerstats.leaderboard;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.hooks.VaultHook;
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
    public static Map<UUID, Double> moneyData;
    private List<MoneyEntry> leaderboard;
    public static File leaderboardFile;
    public static FileConfiguration leaderboardConfig;
    private String cachedLeaderboardDisplay = "";

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
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveAllOnlinePlayers, 0L, 12000L); // 12000 ticks = 10 minutes
    }

    // Save all online players' money data 10 minutes before leaderboard updates
    private void saveAllOnlinePlayers() {
        playerDataManager.saveAll();

        // Update the moneyData map with current online players' data and mark dirty players
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerStats stats = PlayerStats.getPlayerStats(player);
            if (VaultHook.getBalance(player) > 1) {
                UUID playerUUID = player.getUniqueId();
                if (!moneyData.containsKey(playerUUID) || moneyData.get(playerUUID) != VaultHook.getBalance(player)) {
                    moneyData.put(playerUUID, VaultHook.getBalance(player));
                    LeaderboardUtils.markPlayerDirty(playerUUID); // Mark the player as dirty if their money changes
                }
            }
        }

        LeaderboardUtils.saveDirtyMoneyData();  // Save only the updated money data to the config file
    }
    // Update the leaderboard by sorting the players by money
    private void updateLeaderboard() {
        PriorityQueue<Map.Entry<UUID, Double>> topPlayers = new PriorityQueue<>(Map.Entry.comparingByValue());

        // Iterate over moneyData and keep track of the top 5 players
        for (Map.Entry<UUID, Double> entry : moneyData.entrySet()) {
            topPlayers.offer(entry);
            if (topPlayers.size() > 5) {
                topPlayers.poll(); // Remove the player with the least money
            }
        }

        // Update the leaderboard list from the priority queue
        leaderboard = new ArrayList<>();
        while (!topPlayers.isEmpty()) {
            Map.Entry<UUID, Double> entry = topPlayers.poll();
            leaderboard.add(0, new MoneyEntry(Bukkit.getOfflinePlayer(entry.getKey()).getName(), entry.getValue()));
        }

        // Save the leaderboard data asynchronously
        LeaderboardUtils.saveDirtyMoneyData();

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
        if (cachedLeaderboardDisplay.isEmpty()) {
            StringBuilder display = new StringBuilder("§a§lTop 5 Money Leaderboard:\n");
            int rank = 1;
            for (MoneyEntry entry : leaderboard) {
                display.append("§6").append(rank).append(". ").append(entry.getPlayerName())
                        .append(" - §e").append(String.format("%.2f", entry.getMoney())).append("\n");
                rank++;
            }
            cachedLeaderboardDisplay = display.toString();
        }

        player.sendMessage(cachedLeaderboardDisplay);

        // Show the player's rank if they are on the leaderboard
        int playerRank = getPlayerPlacement(player.getUniqueId());
        if (playerRank > 5) {
            player.sendMessage("§7Your Rank: §6" + playerRank);
        }
    }

}
