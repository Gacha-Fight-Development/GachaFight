package me.diu.gachafight.quest.utils;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.quest.managers.DailyQuestManager;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.ZoneId;

public class DailyQuestScheduler {

    public static void scheduleDailyQuestRefresh(GachaFight plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (isTimeForDailyQuestRefresh()) {
                DailyQuestManager.clearDailyQuestCompletionData();
                GachaFight.getInstance().getLogger().info("Daily quests have been reset.");
            }
        }, 0L, 1200); // Check every hour (in ticks)
    }

    private static boolean isTimeForDailyQuestRefresh() {
        LocalTime now = LocalTime.now(ZoneId.of("America/Chicago")); // Use Central Time
        int hour = now.getHour();
        int minute = now.getMinute();
        if (minute == 0) {
            return hour == 14; // 2 pm
        } else {
            return false;
        }
    }

    public static void clearDailyQuestsFromProgress() {
        String sql = "DELETE FROM quest_progress WHERE quest_id BETWEEN 1 AND 1000";


        try (Connection conn = GachaFight.getInstance().getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            GachaFight.getInstance().getLogger().info("All daily quests have been cleared from quest_progress.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

