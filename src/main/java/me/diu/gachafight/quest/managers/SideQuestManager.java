package me.diu.gachafight.quest.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.quest.DatabaseManager;
import me.diu.gachafight.quest.Quest;
import org.bukkit.entity.Player;

public class SideQuestManager {

    // Save side quests for the player (slot 1-5 contain quest IDs)
    public static void saveSideQuests(Player player, int[] questIds) {
        if (questIds.length != 5) {
            throw new IllegalArgumentException("There must be exactly 5 quest IDs.");
        }

        UUID playerUUID = player.getUniqueId();
        String sqlSideQuests = "INSERT INTO side_quests (player_uuid, slot_1, slot_2, slot_3, slot_4, slot_5) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE slot_1 = ?, slot_2 = ?, slot_3 = ?, slot_4 = ?, slot_5 = ?";

        String sqlQuestProgress = "INSERT INTO quest_progress (player_uuid, quest_id, progress) " +
                "VALUES (?, ?, 0) ON DUPLICATE KEY UPDATE quest_id = quest_id";

        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection()) {
            // Save to side_quests table
            try (PreparedStatement stmt = conn.prepareStatement(sqlSideQuests)) {
                stmt.setString(1, playerUUID.toString());
                for (int i = 0; i < 5; i++) {
                    stmt.setInt(i + 2, questIds[i]); // slot_1 -> slot_5
                    stmt.setInt(i + 7, questIds[i]); // Update part
                }
                stmt.executeUpdate();
            }

            // Save each quest to quest_progress table with initial progress of 0
            for (int questId : questIds) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlQuestProgress)) {
                    stmt.setString(1, playerUUID.toString());
                    stmt.setInt(2, questId);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static int[] getSideQuests(Player player) {
        UUID playerUUID = player.getUniqueId();
        String sql = "SELECT slot_1, slot_2, slot_3, slot_4, slot_5 FROM side_quests WHERE player_uuid = ?";

        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new int[] {
                        rs.getInt("slot_1"),
                        rs.getInt("slot_2"),
                        rs.getInt("slot_3"),
                        rs.getInt("slot_4"),
                        rs.getInt("slot_5")
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return an empty array if no data is found
        return new int[5];
    }

    public static void clearSideQuests() {
        // Query to retrieve all players and their quest IDs from side_quests
        String selectSQL = "SELECT player_uuid, slot_1, slot_2, slot_3, slot_4, slot_5 FROM side_quests";
        String deleteProgressSQL = "DELETE FROM quest_progress WHERE player_uuid = ? AND quest_id = ?";
        String clearSideQuestsSQL = "DELETE FROM side_quests";

        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection()) {
            // Step 1: Retrieve side quests for all players
            PreparedStatement selectStmt = conn.prepareStatement(selectSQL);
            ResultSet rs = selectStmt.executeQuery();

            while (rs.next()) {
                String playerUUID = rs.getString("player_uuid");
                int[] slots = {
                        rs.getInt("slot_1"),
                        rs.getInt("slot_2"),
                        rs.getInt("slot_3"),
                        rs.getInt("slot_4"),
                        rs.getInt("slot_5")
                };

                // Step 2: For each slot, check if the quest ID is in quest_progress and clear it if found
                for (int questId : slots) {
                    if (questId != 0) { // Ensure it's a valid quest ID
                        PreparedStatement deleteStmt = conn.prepareStatement(deleteProgressSQL);
                        deleteStmt.setString(1, playerUUID);
                        deleteStmt.setInt(2, questId);
                        deleteStmt.executeUpdate();
                        deleteStmt.close();
                    }
                }
            }

            // Step 3: Clear the entire side_quests table
            PreparedStatement clearStmt = conn.prepareStatement(clearSideQuestsSQL);
            clearStmt.executeUpdate();
            clearStmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void assignSideQuestsIfNecessary(Player player) {
        int[] sideQuests = SideQuestManager.getSideQuests(player);

        // Check if the player already has side quests
        boolean hasSideQuests = false;
        for (int questId : sideQuests) {
            if (questId > 0) { // Check if any side quest slot is filled
                hasSideQuests = true;
                break;
            }
        }

        // If the player doesn't have side quests, assign new ones
        if (!hasSideQuests) {
            int[] newSideQuests = assignRandomSideQuests(player);
            SideQuestManager.saveSideQuests(player, newSideQuests);
        }
    }

    // Assign random side quests to the player (5 quests from the range 1001 to max ID)
    public static int[] assignRandomSideQuests(Player player) {
        List<Quest> availableSideQuests = QuestManager.getQuestsInRange(1001, Integer.MAX_VALUE); // Get all quests from 1001+
        Collections.shuffle(availableSideQuests); // Randomize the list

        // Prepare an array to store the assigned side quests (5 quests)
        int[] assignedQuests = new int[5];
        int assignedCount = 0;

        // Loop through the available quests and assign them if they are not completed
        for (Quest quest : availableSideQuests) {
            // Check if the quest is non-repeatable and completed by the player
            if (!quest.isRepeatable() && isQuestCompletedByPlayer(player, quest.getId())) {
                continue; // Skip this quest as it's already completed
            }

            // Assign the quest to the player
            assignedQuests[assignedCount] = quest.getId();
            assignedCount++;

            // Stop once we have assigned 5 quests
            if (assignedCount >= 5) {
                break;
            }
        }

        // If not enough quests were assigned, the remaining slots will be 0 by default
        return assignedQuests;
    }
    public static boolean isQuestCompletedByPlayer(Player player, int questId) {
        UUID playerUUID = player.getUniqueId(); // Get the player's UUID

        // SQL query to check if the quest exists in the completed_quests table
        String sql = "SELECT 1 FROM completed_quests WHERE player_uuid = ? AND quest_id = ? LIMIT 1";

        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, questId);

            // Execute the query and return true if a result is found (quest is completed)
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // If there was an error or no result was found, return false (quest is not completed)
    }
}
