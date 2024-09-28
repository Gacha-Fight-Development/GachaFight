package me.diu.gachafight.quest.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Collections;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.quest.DatabaseManager;
import me.diu.gachafight.quest.utils.DailyQuestScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.diu.gachafight.quest.Quest;

public class DailyQuestManager {

    // Method to check if the player has completed their daily quest today
    public static boolean hasCompletedDailyQuestToday(Player player) {
        UUID playerUUID = player.getUniqueId();
        LocalDate today = LocalDate.now(); // Get today's date

        String sql = "SELECT last_completion_date FROM daily_quest_completion WHERE player_uuid = ?";

        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LocalDate lastCompletionDate = rs.getDate("last_completion_date").toLocalDate();
                return lastCompletionDate.equals(today); // Check if the completion date is today
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // Player hasn't completed a daily quest today
    }

    // Method to mark the player as having completed their daily quest today
    public static void markDailyQuestCompleted(Player player) {
        UUID playerUUID = player.getUniqueId();
        LocalDate today = LocalDate.now(); // Get today's date

        String sql = "INSERT INTO daily_quest_completion (player_uuid, last_completion_date) " +
                "VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE last_completion_date = ?";

        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setDate(2, java.sql.Date.valueOf(today));
            stmt.setDate(3, java.sql.Date.valueOf(today)); // Update the completion date if it already exists
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to assign a random daily quest to the player
    public static Quest assignRandomDailyQuest(Player player) {
        // Fetch all available daily quests (IDs 1-1000)
        List<Quest> availableDailyQuests = QuestManager.getQuestsInRange(1, 1000);

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

    public static void saveAssignedDailyQuest(Player player, Quest dailyQuest) {
        UUID playerUUID = player.getUniqueId();
        int questId = dailyQuest.getId();

        String sql = "INSERT INTO quest_progress (player_uuid, quest_id, progress) " +
                "VALUES (?, ?, 0) " +
                "ON DUPLICATE KEY UPDATE quest_id = quest_id"; // Prevent duplicate insertion

        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, questId);
            stmt.executeUpdate();

            // Add logging to track saving
            Bukkit.getLogger().info("Daily quest saved: " + dailyQuest.getName() + " for player: " + player.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Method to clear daily quest completion data (called during quest refresh times)
    public static void clearDailyQuestCompletionData() {
        String sql = "DELETE FROM daily_quest_completion";
        DailyQuestScheduler.clearDailyQuestsFromProgress();

        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate(); // Clear all daily quest completion data
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}