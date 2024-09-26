package me.diu.gachafight.quest;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.quest.objectives.DailyCooldownObjective;
import me.diu.gachafight.quest.objectives.KeyOpenObjective;
import me.diu.gachafight.quest.objectives.KillObjective;
import me.diu.gachafight.quest.objectives.OnlineTimeObjective;
import me.diu.gachafight.quest.utils.QuestFactory;
import me.diu.gachafight.quest.utils.QuestObjective;
import me.diu.gachafight.utils.FeedbackUtils;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QuestManager {
    private final Map<Integer, Quest> configuredQuests = new HashMap<>();
    private final GachaFight plugin;
    private final DatabaseManager databaseManager;
    private final QuestFactory questFactory;


    public QuestManager(GachaFight plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.questFactory = new QuestFactory(this);
        loadQuestsFromConfig();
    }

    private void loadQuestsFromConfig() {
        File configFile = new File(plugin.getDataFolder(), "quests.yml");
        if (!configFile.exists()) {
            plugin.saveResource("quests.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        List<Map<?, ?>> quests = config.getMapList("quests");

        for (Map<?, ?> questData : quests) {
            try {
                int id = (int) questData.get("id");
                String name = (String) questData.get("name");
                String description = (String) questData.get("description");
                Map<?, ?> objectiveData = (Map<?, ?>) questData.get("objective");
                String type = (String) objectiveData.get("type");

                QuestObjective objective;
                switch (type) {
                    case "kill":
                        String target = (String) objectiveData.get("target");
                        int amount = (int) objectiveData.get("amount");
                        objective = new KillObjective(description, target, amount, 0, this);
                        break;
                    case "onlineTime":
                        amount = (int) objectiveData.get("amount");
                        objective = new OnlineTimeObjective(description, amount);
                        break;
                    case "keyOpen":
                        amount = (int) objectiveData.get("amount");
                        objective = new KeyOpenObjective(description, amount, 0, this);
                        break;
                    case "dailyCooldown":
                        amount = (int) objectiveData.get("amount");
                        objective = new DailyCooldownObjective(description, amount);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown quest type: " + type);
                }

                int slot = (int) questData.get("slot");
                Quest quest = new Quest(name, description, objective, id, slot);
                Map<String, Object> rewards = (Map<String, Object>) questData.get("rewards");
                if (rewards != null) {
                    quest.setRewards(rewards);
                }
                configuredQuests.put(id, quest);
                List<Integer> dependencies = (List<Integer>) questData.get("dependencies");
                quest.setDependencies(dependencies != null ? dependencies : new ArrayList<>());
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error loading quest from config: " + e.getMessage());
            }
        }
    }

    public Quest createQuestFromConfig(int questId) {
        return configuredQuests.get(questId);
    }

    public void assignQuest(Player player, @NotNull Quest quest, Integer slot) {
        // Save quest progress in the database
        Timestamp startTime = Timestamp.from(Instant.now());
        saveQuestProgress(player, quest.getId(), 0, startTime, slot != null ? slot : -1);
    }



    public void saveQuestProgress(@NotNull Player player, int questId, int progress, Timestamp startTime, int slot) {
        try (Connection conn = databaseManager.getConnection()) {
            if (startTime != null) {
                // Insert or update with startTime
                String insertSql = "INSERT INTO quest_progress (player_uuid, quest_id, progress, start_time, slot) " +
                        "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE progress = ?, start_time = ?, slot = ?";
                Bukkit.getLogger().info("Attempting to insert progress");
                try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                    stmt.setString(1, player.getUniqueId().toString());
                    stmt.setInt(2, questId);
                    stmt.setInt(3, progress);
                    stmt.setTimestamp(4, startTime);
                    stmt.setInt(5, slot);
                    stmt.setInt(6, progress);
                    stmt.setTimestamp(7, startTime);
                    stmt.setInt(8, slot);
                    stmt.executeUpdate();
                }
            } else {
                // Update without modifying startTime
                String updateSql = "UPDATE quest_progress SET progress = ?, slot = ? WHERE player_uuid = ? AND quest_id = ?";
                Bukkit.getLogger().info("Attempting to update progress");
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setInt(1, progress);
                    stmt.setInt(2, slot);
                    stmt.setString(3, player.getUniqueId().toString());
                    stmt.setInt(4, questId);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Helper method to load existing start time
    // QuestManager.java

    // Helper method to load existing start time
    public Timestamp loadQuestStartTime(@NotNull Player player, int questId) {
        String sql = "SELECT start_time FROM quest_progress WHERE player_uuid = ? AND quest_id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setInt(2, questId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getTimestamp("start_time");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Timestamp.from(Instant.now()); // fallback
    }

    public Integer loadQuestProgress(@NotNull Player player, int questId) {
        String sql = "SELECT progress, start_time FROM quest_progress WHERE player_uuid = ? AND quest_id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setInt(2, questId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Timestamp startTime = rs.getTimestamp("start_time");
                System.out.println("Quest found, start time: " + startTime);
                if (isQuestExpired(startTime, 24)) { // 24 hours for daily quests
                    System.out.println("Quest expired, deleting progress.");
                    deleteQuestProgress(player, questId);
                    return null;
                }
                int progress = rs.getInt("progress");
                System.out.println("Quest progress: " + progress);
                return progress;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    private boolean isQuestExpired(@NotNull Timestamp startTime, int hours) {
        long elapsedTime = Instant.now().toEpochMilli() - startTime.getTime();
        return elapsedTime > (hours * 60 * 60 * 1000);
    }

    public void incrementQuestProgress(Player player, int questId) {
        if (hasCompletedQuest(player, questId)) {
            return; // The quest is already completed, no further progress is allowed.
        }
        Quest quest = getQuestById(questId, player); // Get the quest to check objective
        Integer currentProgress = 0;

        if (quest.getObjective() instanceof KillObjective) {
            KillObjective killObjective = (KillObjective) quest.getObjective();
            if (killObjective.getCurrentMobKills() == null) {
                Bukkit.getLogger().info("incrementQuestProgress: Player " + player.getName() + " does not have quest ID " + questId);
                return; // Player does not have this quest
            }
            // Increment the kill count
            killObjective.incrementMobKills(player, questId, null, quest.getSlot());
            currentProgress = killObjective.getCurrentMobKills();
            Bukkit.getLogger().info("Progress after increment: " + currentProgress);
        } else if (quest.getObjective() instanceof OnlineTimeObjective) {
            OnlineTimeObjective objective = (OnlineTimeObjective) quest.getObjective();
            objective.incrementTime(60); // Update objective's progress
            currentProgress = objective.getCurrentTime();
            saveQuestProgress(player, questId, currentProgress, null, quest.getSlot());
        } else if (quest.getObjective() instanceof KeyOpenObjective) {
            KeyOpenObjective keyOpenObjective = (KeyOpenObjective) quest.getObjective();
            if (keyOpenObjective.getCurrentKeyOpen() == null) {
                Bukkit.getLogger().info("incrementQuestProgress: Player " + player.getName() + " does not have quest ID " + questId);
                return; // Player does not have this quest
            }
            keyOpenObjective.incrementKeyOpen(player, 3, null, quest.getSlot()); // Update objective's progress
            currentProgress = keyOpenObjective.getCurrentKeyOpen();
            saveQuestProgress(player, questId, currentProgress, null, quest.getSlot());
        }

        if (currentProgress >= quest.getObjective().getRequiredAmount()) {
            completeQuest(player, questId);
        }
    }





    public void completeQuest(Player player, int questId) {
        Quest quest = getQuestById(questId, player);
        if (quest == null) return;

        Map<String, Object> rewards = quest.getRewards();
        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());

        // Ensure PlayerStats is correctly updated
        PlayerStats stats = PlayerStats.getPlayerStats(player);

        // Reward player with money if applicable
        if (rewards.containsKey("money")) {
            int money = (int) rewards.get("money");
            stats.setMoney(stats.getMoney() + money);
            player.sendMessage("§aYou received " + money + " gold!");
        }

        // Reward player with gems if applicable
        if (rewards.containsKey("gems")) {
            int gems = (int) rewards.get("gems");
            stats.setGem(stats.getGem() + gems);
            player.sendMessage("§aYou received " + gems + " gems!");
        }

        // Reward player with suffix tags if applicable
        if (rewards.containsKey("suffix_tag")) {
            String tagName = (String) rewards.get("suffix_tag");
            givePlayerTagPermission(player, tagName);
            player.sendMessage("§aYou received the suffix tag: " + tagName + "!");
        }

        // Enhanced feedback
        FeedbackUtils.playCompletionSound(player);
        FeedbackUtils.displayCompletionTitle(player);
        FeedbackUtils.showCompletionParticles(player);

        // Handle daily quest or permanent quest completion
        if (!isDailyQuest(questId)) {
            // Only mark non-daily quests as completed
            markQuestAsCompleted(player, questId);
            // Remove quest progress from the database
            deleteQuestProgress(player, questId);
        } else {
            // For daily quests, just reset the progress and set a cooldown
            deleteQuestProgress(player, questId);
            Timestamp newCooldownStartTime = Timestamp.from(Instant.now());
            saveQuestProgress(player, -1, 0, newCooldownStartTime, -1); // Use quest ID -1 to track cooldown
        }
    }


    // Method to give player permission for a suffix tag
    private void givePlayerTagPermission(Player player, String tagName) {
        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            String permission = "gacha.tags." + tagName.toLowerCase();
            PermissionNode permissionNode = PermissionNode.builder(permission).build();
            user.data().add(permissionNode);
            plugin.getLuckPerms().getUserManager().saveUser(user);
        }
    }


    public void deleteQuestProgress(@NotNull Player player, int questId) {
        String sql = "DELETE FROM quest_progress WHERE player_uuid = ? AND quest_id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setInt(2, questId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void checkAllPlayersForExpiredQuests() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String sql = "SELECT quest_id, start_time FROM quest_progress WHERE player_uuid = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, player.getUniqueId().toString());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int questId = rs.getInt("quest_id");
                    Timestamp startTime = rs.getTimestamp("start_time");
                    if (isQuestExpired(startTime, 24)) { // Check if expired
                        deleteQuestProgress(player, questId);
                        player.sendMessage("§cYour daily quest has expired.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    // QuestManager.java
    public Quest getQuestById(int questId, Player player) {
        try {
            return questFactory.createQuest(questId, player);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }




    public List<Quest> getPlayerQuests(@NotNull Player player) {
        List<Quest> quests = new ArrayList<>();
        String sql = "SELECT quest_id, progress, slot FROM quest_progress WHERE player_uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int questId = rs.getInt("quest_id");
                int progress = rs.getInt("progress");
                int slot = rs.getInt("slot");
                Quest quest = getQuestById(questId, player);
                if (quest != null) {
                    quest.setProgress(progress);
                    quest.setSlot(slot); // Set the slot
                    quests.add(quest);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quests;
    }
    public boolean canStartQuest(Player player, int questId) {
        Quest quest = createQuestFromConfig(questId);
        if (quest == null) {
            return false;
        }

        for (int depId : quest.getDependencies()) {
            if (!hasCompletedQuest(player, depId)) {
                return false;
            }
        }
        return true;
    }


    public void startOnlineTimeTracking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    List<Quest> quests = getPlayerQuests(player);
                    for (Quest quest : quests) {
                        if (quest.getObjective() instanceof OnlineTimeObjective) {
                            OnlineTimeObjective objective = (OnlineTimeObjective) quest.getObjective();
                            objective.incrementTime(6000); // Increment by 60 seconds (1 minute)
                            saveQuestProgress(player, quest.getId(), objective.getCurrentTime(), Timestamp.from(Instant.now()), quest.getSlot());

                            if (objective.isCompleted()) {
                                completeQuest(player, quest.getId());
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 1200, 1200); // Run every 1200 ticks (1 minute)
    }

    public boolean hasCompletedQuest(Player player, int questId) {
        String sql = "SELECT 1 FROM completed_quests WHERE player_uuid = ? AND quest_id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setInt(2, questId);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // If a row is found, the quest is completed
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Mark a quest as completed for a player
    public void markQuestAsCompleted(Player player, int questId) {
        if (questId < 10) {
            return;
        }
        String sql = "INSERT INTO completed_quests (player_uuid, quest_id) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE player_uuid = player_uuid"; // No-op update

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setInt(2, questId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getQuestIdFromName(String questName) {
        for (Quest quest : configuredQuests.values()) {
            if (quest.getName().equalsIgnoreCase(questName)) {
                return quest.getId();
            }
        }
        throw new IllegalArgumentException("Quest not found for name: " + questName);
    }

    public boolean isDailyQuest(int questId) {
        // Check if the quest ID is within the daily quest range
        return questId >= 1 && questId <= 10;
    }

}
