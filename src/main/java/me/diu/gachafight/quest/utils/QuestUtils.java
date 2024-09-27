package me.diu.gachafight.quest.utils;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.quest.Quest;
import me.diu.gachafight.quest.managers.DailyQuestManager;
import me.diu.gachafight.quest.managers.QuestManager;
import net.luckperms.api.node.types.PermissionNode;
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

    // Initialize QuestUtils with a QuestManager instance
    public static void initialize(QuestManager manager) {
        questManager = manager;
    }

    // Helper method to fetch a quest by its ID and player
    public static Quest getQuestById(int questId, Player player) {
        return questManager.getQuestById(questId);
    }

    // Method to increment quest progress for the player
    public static void incrementQuestProgress(Player player, Quest quest, int incrementAmount) {
        if (quest == null) {
            return; // Quest does not exist
        }

        // Load the current progress
        Integer currentProgress = loadQuestProgress(player, quest.getId());
        if (currentProgress == null) {
            return; // Player does not have the quest active
        }

        // Increment the quest progress
        currentProgress += incrementAmount;

        // Get the required amount from the quest objective
        int requiredAmount = quest.getObjective().getAmount();

        // Save the new progress
        saveQuestProgress(player, quest.getId(), currentProgress);

        // Notify the player about the progress
        player.sendMessage("§aProgress: " + currentProgress + "/" + requiredAmount);

        // If the quest is completed, handle completion
        if (currentProgress >= requiredAmount) {
            completeQuest(player, quest);
        }
    }

    // Helper method to complete a quest
    public static void completeQuest(Player player, Quest quest) {
        player.sendMessage("§aYou have completed the quest: " + quest.getName());

        // Apply quest rewards (this method can be expanded to handle more complex rewards)
        applyRewards(player, quest);

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
}
