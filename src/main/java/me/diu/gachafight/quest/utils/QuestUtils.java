package me.diu.gachafight.quest.utils;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.quest.Quest;
import me.diu.gachafight.quest.managers.DailyQuestManager;
import me.diu.gachafight.quest.managers.QuestManager;
import me.diu.gachafight.quest.objectives.KeyOpenObjective;
import me.diu.gachafight.quest.objectives.KillMobObjective;
import me.diu.gachafight.quest.objectives.OnlineTimeObjective;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class QuestUtils {

    private static QuestManager questManager; // Reference to the QuestManager
    public static void initialize(QuestManager manager) {
        questManager = manager;
    }

    // Method to increment quest progress for the player
    public static void incrementQuestProgress(Player player, Quest quest, String objectiveType, int incrementAmount) {
        if (quest == null) {
            return; // Quest does not exist
        }

        // Load the current progress
        Integer currentProgress = loadQuestProgress(player, quest.getId());
        if (currentProgress == null) {
            currentProgress = 0; // Initialize progress if none found
        }

        switch (objectiveType) {
            case "killMob":
                // Specific logic for kill mob quests
                if (quest.getObjective() instanceof KillMobObjective) {
                    currentProgress += incrementAmount;
                }
                break;
            case "keyOpen":
                // Specific logic for key open quests
                if (quest.getObjective() instanceof KeyOpenObjective) {
                    currentProgress += incrementAmount;
                }
                break;
            case "onlineTime":
                // Specific logic for time spent online quests
                if (quest.getObjective() instanceof OnlineTimeObjective) {
                    currentProgress += incrementAmount;
                }
                break;
            default:
                return; // Unknown objective type
        }

        // Save the new progress
        saveQuestProgress(player, quest.getId(), currentProgress);

        // Notify the player about the progress
        player.sendMessage("§aProgress: " + currentProgress + "/" + quest.getObjective().getAmount());

        // If the quest is completed, handle completion
        if (currentProgress >= quest.getObjective().getAmount()) {
            completeQuest(player, quest);
        }
    }

    // Helper method to complete a quest
    public static void completeQuest(Player player, Quest quest) {
        player.sendMessage("§aYou have completed the quest: " + quest.getName());

        // Apply quest rewards (this method can be expanded to handle more complex rewards)
        applyRewards(player, quest);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.sendTitle("Quest Completed!", quest.getName(), 10, 70, 20);

        // Check if it's a daily quest (ID between 1 and 1000)
        if (quest.getId() >= 1 && quest.getId() <= 1000) {
            // Use DailyQuestManager to mark the daily quest as completed
            DailyQuestManager.markDailyQuestCompleted(player);
        } else {
            // For non-daily quests, mark them as completed if they are not repeatable
            if (!quest.isRepeatable()) {
                questManager.markQuestAsCompleted(player, quest.getId());
            }
        }
        deleteQuestProgress(player, quest.getId());
    }


    // Helper method to apply quest rewards to the player
    public static void applyRewards(Player player, Quest quest) {
        // Logic to apply rewards (money, items, etc.) from the quest's rewards map
        if (quest.getRewards().containsKey("money")) {
            int money = (int) quest.getRewards().get("money");
            player.sendMessage("§aYou received " + money + " coins!");
            // Update player's money balance (this would depend on your economy system)
        }

        if (quest.getRewards().containsKey("gems")) {
            int gems = (int) quest.getRewards().get("gems");
            player.sendMessage("§aYou received " + gems + " gems!");
            // Update player's gem balance
        }

        if (quest.getRewards().containsKey("suffix_tag")) {
            String tagName = (String) quest.getRewards().get("suffix_tag");
            givePlayerTagPermission(player, tagName);
        }
    }

    // Method to give player permission for a suffix tag reward (you can move this here as well)
    public static void givePlayerTagPermission(Player player, String tagName) {
        // Logic to give player a suffix tag permission (LuckPerms or your permission system)
        questManager.getPlugin().getLuckPerms().getUserManager().getUser(player.getUniqueId()).data()
                .add(PermissionNode.builder("gacha.tags." + tagName.toLowerCase()).build());
        questManager.getPlugin().getLuckPerms().getUserManager().saveUser(
                questManager.getPlugin().getLuckPerms().getUserManager().getUser(player.getUniqueId())
        );
        player.sendMessage("§aYou received the suffix tag: " + tagName + "!");
    }

    public static void saveQuestProgress(Player player, int questId, int progress) {
        UUID playerUUID = player.getUniqueId(); // Get player's unique UUID

        String sql = "INSERT INTO quest_progress (player_uuid, quest_id, progress) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE progress = ?";

        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set the parameters for the prepared statement
            stmt.setString(1, playerUUID.toString()); // player_uuid
            stmt.setInt(2, questId);                  // quest_id
            stmt.setInt(3, progress);                 // progress
            stmt.setInt(4, progress);                 // Update progress if exists

            // Execute the query
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static Integer loadQuestProgress(Player player, int questId) {
        UUID playerUUID = player.getUniqueId(); // Get player's unique UUID

        String sql = "SELECT progress FROM quest_progress WHERE player_uuid = ? AND quest_id = ?";

        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set the parameters for the prepared statement
            stmt.setString(1, playerUUID.toString()); // player_uuid
            stmt.setInt(2, questId);                  // quest_id

            // Execute the query
            ResultSet rs = stmt.executeQuery();

            // If a result is found, return the progress
            if (rs.next()) {
                return rs.getInt("progress");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return null if no progress is found
        return null;
    }
    // Method to get the player's progress for a specific quest
    public static String getQuestProgress(Player player, Quest quest) {
        int questId = quest.getId();
        Integer progress = QuestManager.loadQuestProgress(player, questId); // Load the player's current progress

        if (progress == null || progress == 0) {
            return "Not started"; // The player hasn't started the quest yet
        }

        int requiredAmount = quest.getObjective().getAmount(); // Get the required amount for completion

        // Return progress in the format "currentProgress/requiredAmount"
        return progress + "/" + requiredAmount;
    }

    public static String getTimeUntilNextRefresh() {
        // Define the refresh times in Central Time (CST/CDT)
        ZoneId centralTimeZone = ZoneId.of("America/Chicago");
        LocalTime[] refreshTimes = {
                LocalTime.of(2, 0),
                LocalTime.of(8, 0),
                LocalTime.of(14, 0),
                LocalTime.of(20, 0)
        };

        // Get the current time in Central Time
        LocalDateTime now = LocalDateTime.now(centralTimeZone);
        LocalDateTime nextRefresh = null;

        // Find the next refresh time
        for (LocalTime refreshTime : refreshTimes) {
            LocalDateTime refreshToday = now.toLocalDate().atTime(refreshTime);
            if (now.isBefore(refreshToday)) {
                nextRefresh = refreshToday;
                break;
            }
        }

        // If no refresh time is found today, pick the earliest refresh time for tomorrow
        if (nextRefresh == null) {
            nextRefresh = now.plusDays(1).toLocalDate().atTime(refreshTimes[0]);
        }

        // Calculate the time difference
        long hoursUntilNext = ChronoUnit.HOURS.between(now, nextRefresh);
        long minutesUntilNext = ChronoUnit.MINUTES.between(now, nextRefresh) % 60;

        return hoursUntilNext + "h " + minutesUntilNext + "m";
    }

    // Method to delete a quest from quest_progress for the player
    private static void deleteQuestProgress(Player player, int questId) {
        UUID playerUUID = player.getUniqueId();
        String sql = "DELETE FROM quest_progress WHERE player_uuid = ? AND quest_id = ?";

        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, questId);
            stmt.executeUpdate();
            Bukkit.getLogger().info("Quest " + questId + " removed from quest_progress for player " + player.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Helper method to get the active daily quest for the player (check if they are already working on one)
    public static Quest getActiveDailyQuestForPlayer(Player player) {
        UUID playerUUID = player.getUniqueId(); // Get player's UUID
        String sql = "SELECT quest_id FROM quest_progress WHERE player_uuid = ? AND quest_id BETWEEN 1 AND 1000";

        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int questId = rs.getInt("quest_id");
                return QuestManager.getQuestById(questId); // Fetch and return the quest if found
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // No active daily quest found
    }

    public static boolean isQuestCompleted(Player player, int questId) {
        UUID playerUUID = player.getUniqueId();

        // Check if the quest is non-repeatable
        Quest quest = QuestManager.getQuestById(questId);
        if (quest == null || quest.isRepeatable()) {
            return false; // Repeatable quests can't be "completed" permanently
        }

        // Check if the quest exists in quest_progress
        String sql = "SELECT 1 FROM quest_progress WHERE player_uuid = ? AND quest_id = ? LIMIT 1";

        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, questId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return false; // Quest is still in progress (found in quest_progress)
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true; // Quest is not in quest_progress and is non-repeatable, meaning it's completed
    }

}
