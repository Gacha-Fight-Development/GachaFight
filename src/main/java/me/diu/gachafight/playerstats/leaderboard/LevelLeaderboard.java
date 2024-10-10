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

public class LevelLeaderboard {

    private final GachaFight plugin;
    private final PlayerDataManager playerDataManager;
    public static Map<UUID, Integer> levelData;
    private List<LevelEntry> leaderboard;
    public static File leaderboardFile;
    public static FileConfiguration leaderboardConfig;
    private String cachedLeaderboardDisplay = "";

    public LevelLeaderboard(GachaFight plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        this.levelData = new HashMap<>();
        this.leaderboard = new ArrayList<>();

        // Create and load the leaderboard config file for levels
        leaderboardFile = new File(plugin.getDataFolder(), "level_leaderboard.yml");
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

    // Save all online players' level data 10 minutes before leaderboard updates
    private void saveAllOnlinePlayers() {
        playerDataManager.saveAll();

        // Update the levelData map with current online players' level data and mark dirty players
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerStats stats = PlayerStats.getPlayerStats(player);
            int playerLevel = stats.getLevel(); // Assuming PlayerStats has a getLevel() method
            if (playerLevel > 1) {
                UUID playerUUID = player.getUniqueId();
                if (!levelData.containsKey(playerUUID) || levelData.get(playerUUID) != playerLevel) {
                    levelData.put(playerUUID, playerLevel);
                    LeaderboardUtils.markPlayerDirty(playerUUID); // Mark the player as dirty if their level changes
                }
            }
        }

        LeaderboardUtils.saveDirtyLevelData();  // Save only the updated level data to the config file
    }

    // Update the leaderboard by sorting the players by level
    private void updateLeaderboard() {
        PriorityQueue<Map.Entry<UUID, Integer>> topPlayers = new PriorityQueue<>(Map.Entry.comparingByValue());

        // Iterate over levelData and keep track of the top 5 players
        for (Map.Entry<UUID, Integer> entry : levelData.entrySet()) {
            topPlayers.offer(entry);
            if (topPlayers.size() > 5) {
                topPlayers.poll(); // Remove the player with the least level
            }
        }

        // Update the leaderboard list from the priority queue
        leaderboard = new ArrayList<>();
        while (!topPlayers.isEmpty()) {
            Map.Entry<UUID, Integer> entry = topPlayers.poll();
            leaderboard.add(0, new LevelEntry(Bukkit.getOfflinePlayer(entry.getKey()).getName(), entry.getValue()));
        }

        // Save the leaderboard data asynchronously
        LeaderboardUtils.saveDirtyLevelData();

        plugin.getLogger().info("Level Leaderboard updated!");
    }

    // Load all level data from the config file
    private void loadLeaderboard() {
        if (leaderboardConfig.contains("players")) {
            for (String uuidString : leaderboardConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                int level = leaderboardConfig.getInt("players." + uuidString + ".level");

                levelData.put(uuid, level);
            }
        }
    }

    // Get the top 5 leaderboard entries for levels
    public List<LevelEntry> getTop5() {
        return leaderboard;
    }

    // Get a player's placement on the level leaderboard
    public int getPlayerPlacement(UUID playerUUID) {
        List<Map.Entry<UUID, Integer>> sortedLevelData = levelData.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        for (int i = 0; i < sortedLevelData.size(); i++) {
            if (sortedLevelData.get(i).getKey().equals(playerUUID)) {
                return i + 1; // Rank starts at 1
            }
        }
        return -1; // Not on the leaderboard
    }

    // Display the level leaderboard in chat
    public void displayLeaderboard(Player player) {
        if (cachedLeaderboardDisplay.isEmpty()) {
            StringBuilder display = new StringBuilder("§a§lTop 5 Level Leaderboard:\n");
            int rank = 1;
            for (LevelEntry entry : leaderboard) {
                display.append("§6").append(rank).append(". ").append(entry.getPlayerName())
                        .append(" - §eLevel ").append(entry.getLevel()).append("\n");
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
