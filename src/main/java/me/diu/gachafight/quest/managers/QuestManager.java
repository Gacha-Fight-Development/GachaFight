package me.diu.gachafight.quest.managers;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.quest.DatabaseManager;
import me.diu.gachafight.quest.Quest;
import me.diu.gachafight.quest.objectives.KeyOpenObjective;
import me.diu.gachafight.quest.objectives.KillMobObjective;
import me.diu.gachafight.quest.objectives.OnlineTimeObjective;
import me.diu.gachafight.quest.utils.QuestObjective;
import me.diu.gachafight.quest.utils.QuestUtils;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class QuestManager {
    private static final Map<Integer, Quest> quests = new HashMap<>();
    private final GachaFight plugin;
    private final DatabaseManager databaseManager;


    public QuestManager(GachaFight plugin, File dataFolder, DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.plugin = plugin;
        loadQuestsFromConfig(dataFolder);
    }

    public static void loadQuestsFromConfig(File dataFolder) {
        File configFile = new File(dataFolder, "quests.yml");
        if (!configFile.exists()) {
            throw new IllegalStateException("quests.yml file not found!");
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        List<Map<?, ?>> questsList = config.getMapList("quests");

        for (Map<?, ?> questData : questsList) {
            int id = (int) questData.get("id");
            String name = (String) questData.get("name");
            String description = (String) questData.get("description");
            Map<?, ?> objectiveData = (Map<?, ?>) questData.get("objective");
            String type = (String) objectiveData.get("type");

            QuestObjective objective;
            switch (type) {
                case "killMob":
                    String target = (String) objectiveData.get("target");
                    int amount = (int) objectiveData.get("amount");
                    objective = new KillMobObjective(description, target, amount);
                    break;
                case "onlineTime":
                    int timeInSeconds = (int) objectiveData.get("amount");
                    objective = new OnlineTimeObjective(description, timeInSeconds);
                    break;
                case "keyOpen":
                    amount = (int) objectiveData.get("amount");
                    objective = new KeyOpenObjective(description, amount);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown quest objective type: " + type);
            }

            // Load rewards
            Map<String, Object> rewards = (Map<String, Object>) questData.get("rewards");

            // Check if the quest is repeatable (default to true if not specified)
            boolean repeatable = questData.containsKey("repeatable") ? (boolean) questData.get("repeatable") : true;

            // Create the quest object
            Quest quest = new Quest(id, name, description, objective, rewards, repeatable);

            // Add the quest to the map
            quests.put(id, quest);
        }
    }

    public void markQuestAsCompleted(Player player, int questId) {
        // Get the quest object by ID
        Quest quest = getQuestById(questId);

        // Check if the quest is not repeatable
        if (quest != null && !quest.isRepeatable()) {
            // Mark the quest as completed in the database
            try (Connection conn = databaseManager.getConnection()) {
                String sql = "INSERT INTO completed_quests (player_uuid, quest_id) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE quest_id = quest_id"; // No-op update for preventing duplicates

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, player.getUniqueId().toString());
                    stmt.setInt(2, questId);
                    stmt.executeUpdate();
                    player.sendMessage("§aQuest " + quest.getName() + " marked as completed!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Quest is repeatable, no need to mark as completed
            player.sendMessage("§eQuest " + (quest != null ? quest.getName() : "") + " is repeatable and won't be marked as completed.");
        }
    }

    public static Quest getQuestById(int id) {
        return quests.get(id);
    }
    public static Quest getDailyQuestForPlayer(Player player) {
        // Fetch all available daily quests (IDs 1-1000)
        List<Quest> dailyQuests = quests.values().stream()
                .filter(quest -> quest.getId() >= 1 && quest.getId() <= 1000) // Quest ID between 1 and 1000
                .collect(Collectors.toList());

        // Optionally, check if the player already has an active or completed daily quest
        Quest activeDailyQuest = getActiveDailyQuestForPlayer(player);
        if (activeDailyQuest != null) {
            return activeDailyQuest; // Return the active daily quest if the player is already on it
        }

        // Assign a new daily quest to the player
        Quest newDailyQuest = assignNewDailyQuest(player, dailyQuests);

        // Optionally, track the assignment in a database or cache
        saveAssignedDailyQuest(player, newDailyQuest);

        return newDailyQuest;
    }

    // Helper method to get the active daily quest for the player (check if they are already working on one)
    public static Quest getActiveDailyQuestForPlayer(Player player) {
        // Assuming you have a method that checks the player's progress on daily quests
        for (int questId = 1; questId <= 1000; questId++) {
            Integer progress = QuestUtils.loadQuestProgress(player, questId); // Fetch quest progress for daily quests
            if (progress != null && progress > 0) {
                return getQuestById(questId); // Return the quest if the player is actively working on it
            }
        }
        return null; // No active daily quest found
    }

    // Helper method to assign a new daily quest
    public static Quest assignNewDailyQuest(Player player, List<Quest> dailyQuests) {
        // Here you can implement logic to randomly assign a daily quest or pick the next available quest.
        // For this example, we'll just pick the first available daily quest that isn't completed.
        for (Quest quest : dailyQuests) {
            Integer progress = QuestUtils.loadQuestProgress(player, quest.getId());
            if (progress == null || progress == 0) { // Player hasn't started this quest
                return quest; // Assign this quest
            }
        }

        // If all quests have been completed, return null or recycle quests (optional logic)
        return null; // No available daily quest found
    }

    // Optionally, save the assigned daily quest for the player (can store in a database)
    private static void saveAssignedDailyQuest(Player player, Quest quest) {
        if (quest != null) {
            // Save the assigned daily quest in the database if necessary
            // This can track that the player has been assigned a specific daily quest for today.
        }
    }
    public static Quest assignRandomDailyQuest(Player player) {
        // Fetch all available daily quests (IDs 1-1000)
        List<Quest> availableDailyQuests = getQuestsInRange(1, 1000);

        // Check if there are no available quests
        if (availableDailyQuests.isEmpty()) {
            return null; // No daily quests available
        }

        // Randomly select a daily quest
        Collections.shuffle(availableDailyQuests); // Randomize the list
        Quest randomDailyQuest = availableDailyQuests.get(0); // Pick the first random quest

        // Save assigned daily quest (if needed)
        saveAssignedDailyQuest(player, randomDailyQuest);

        return randomDailyQuest;
    }
    public static List<Quest> getQuestsInRange(int minId, int maxId) {
        return quests.values().stream()
                .filter(quest -> quest.getId() >= minId && quest.getId() <= maxId) // Filter by quest ID range
                .collect(Collectors.toList());
    }
    public static Integer loadQuestProgress(Player player, int questId) {
        UUID playerUUID = player.getUniqueId(); // Get the player's UUID
        String sql = "SELECT progress FROM quest_progress WHERE player_uuid = ? AND quest_id = ?";

        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, questId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("progress"); // Return the progress value
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQL exceptions
        }

        // Return null if no progress is found (the player hasn't started the quest)
        return null;
    }
}
