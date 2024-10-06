package me.diu.gachafight.quest.utils;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.quest.managers.SideQuestManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SideQuestScheduler {

    // Schedule the quest clear task
    public static void scheduleQuestClearTask(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (isCentralTimeForClear()) {
                SideQuestManager.clearSideQuests();
                clearSideQuestsFromProgress();
                SideQuestManager.clearSideQuests();
                Bukkit.getLogger().info("Side quests cleared at specified time.");
            }
        }, 0L, 20 * 60 * 60); // Run every hour (in ticks)
    }

    // Check if the current time matches the specified clear times (2 AM, 8 AM, 2 PM, 8 PM CST/CDT)
    private static boolean isCentralTimeForClear() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Chicago"));
        int hour = now.getHour();
        int minute = now.getMinute();
        if (minute == 0) {
            return hour == 2 || hour == 8 || hour == 14 || hour == 20;
        } else {
            return false;
        }
    }
    private static void clearSideQuestsFromProgress() {
        String sql = "DELETE FROM quest_progress WHERE quest_id >= 1001";

        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            Bukkit.getLogger().info("All side quests have been cleared from quest_progress.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
