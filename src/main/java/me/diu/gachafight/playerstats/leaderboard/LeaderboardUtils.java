package me.diu.gachafight.playerstats.leaderboard;

import me.diu.gachafight.GachaFight;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LeaderboardUtils {
    public static Set<UUID> dirtyPlayers = new HashSet<>(); // Track players with changed data

    public static void markPlayerDirty(UUID uuid) {
        dirtyPlayers.add(uuid);
    }
    // Save only the changed (dirty) players' money data to the config file
    public static void saveDirtyMoneyData() {
        Bukkit.getScheduler().runTaskAsynchronously(GachaFight.getInstance(), () -> {
            for (UUID uuid : dirtyPlayers) {
                MoneyLeaderboard.leaderboardConfig.set("players." + uuid + ".money", MoneyLeaderboard.moneyData.get(uuid));
                MoneyLeaderboard.leaderboardConfig.set("players." + uuid + ".name", Bukkit.getOfflinePlayer(uuid).getName());
            }

            try {
                MoneyLeaderboard.leaderboardConfig.save(MoneyLeaderboard.leaderboardFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            dirtyPlayers.clear(); // Clear the dirty set after saving
        });
    }
    public static void saveDirtyLevelData() {
        Bukkit.getScheduler().runTaskAsynchronously(GachaFight.getInstance(), () -> {
            for (UUID uuid : dirtyPlayers) {
                LevelLeaderboard.leaderboardConfig.set("players." + uuid + ".level", LevelLeaderboard.levelData.get(uuid));
                LevelLeaderboard.leaderboardConfig.set("players." + uuid + ".name", Bukkit.getOfflinePlayer(uuid).getName());
            }

            try {
                LevelLeaderboard.leaderboardConfig.save(LevelLeaderboard.leaderboardFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            dirtyPlayers.clear(); // Clear the dirty set after saving
        });
    }
}
