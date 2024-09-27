package me.diu.gachafight.quest.utils;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.quest.managers.DailyQuestManager;
import org.bukkit.Bukkit;

import java.time.LocalTime;
import java.time.ZoneId;

public class DailyQuestScheduler {

    public static void scheduleDailyQuestRefresh(GachaFight plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (isTimeForDailyQuestRefresh()) {
                DailyQuestManager.clearDailyQuestCompletionData();
                Bukkit.getLogger().info("Daily quests have been reset.");
            }
        }, 0L, 20 * 60 * 60); // Check every hour (in ticks)
    }

    private static boolean isTimeForDailyQuestRefresh() {
        LocalTime now = LocalTime.now(ZoneId.of("America/Chicago")); // Use Central Time
        int hour = now.getHour();
        return hour == 2 || hour == 8 || hour == 14 || hour == 20; // 2AM, 8AM, 2PM, 8PM
    }
}

