package me.diu.gachafight.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.playerstats.leaderboard.MoneyEntry;
import me.diu.gachafight.playerstats.leaderboard.LevelEntry;
import me.diu.gachafight.playerstats.leaderboard.MoneyLeaderboard;
import me.diu.gachafight.playerstats.leaderboard.LevelLeaderboard;
import me.diu.gachafight.skills.managers.MobDropSelector;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    private final MoneyLeaderboard moneyLeaderboard = GachaFight.getInstance().getMoneyLeaderboard();
    private final LevelLeaderboard levelLeaderboard = GachaFight.getInstance().getLevelLeaderboard();  // Add reference to LevelLeaderboard

    // Caching leaderboard display
    private final Map<Integer, String> moneyLeaderboardCache = new HashMap<>();
    private final Map<Integer, String> levelLeaderboardCache = new HashMap<>();  // Cache for level leaderboard

    private long lastCacheUpdateTime = 0;
    private final long cacheExpirationTime = 60000; // Cache for 1 minute (60000 ms)

    @Override
    public String getIdentifier() {
        return "Gacha";
    }

    @Override
    public String getAuthor() {
        return "GachaFight";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @NotNull String onRequest(OfflinePlayer offlinePlayer, @NotNull String arg) {
        if (offlinePlayer != null) {
            Player player = offlinePlayer.getPlayer();
            if (player != null) {
                PlayerStats stats = PlayerStats.playerStatsMap.get(player.getUniqueId());  // Access directly from static map

                if (stats == null) {
                    return "0"; // Default value if stats are not found
                }

                switch (arg.toLowerCase()) {
                    // Player stats placeholders
                    case "level":
                        return String.valueOf(stats.getLevel());
                    case "luck":
                        return String.format("%.1f", (stats.getLuck() + stats.getGearStats().getTotalLuck() + stats.getWeaponStats().getLuck()));
                    case "exp":
                        return String.format("%.1f", stats.getExp());
                    case "expreq":
                        return String.valueOf(stats.getRequiredExp());
                    case "damage":
                        return String.format("%.1f", stats.getDamage() + stats.getWeaponStats().getDamage() + stats.getGearStats().getTotalDamage());
                    case "armor":
                        return String.format("%.1f", stats.getArmor() + stats.getGearStats().getTotalArmor() + stats.getWeaponStats().getArmor());
                    case "hp":
                        return String.format("%.1f", (stats.getMaxhp() + stats.getGearStats().getTotalMaxHp() + stats.getWeaponStats().getMaxHp()));
                    case "money":
                        return String.format("%.2f", stats.getMoney());
                    case "gem":
                        return String.valueOf(stats.getGem());
                    case "speed":
                        return String.format("%.0f", stats.getSpeed() * 100) + "%";
                    case "critchance":
                        return String.format("%.1f", stats.getCritChance() * 100) + "%";
                    case "critdmg":
                        return String.format("%.0f", (stats.getCritDmg() * 100)) + "%";
                    case "dodge":
                        return String.format("%.1f", (stats.getDodge() * 100)) + "%";
                    case "test":
                        return String.valueOf(MobDropSelector.getMob());

                    // Money leaderboard placeholders
                    case "money_rank":
                        int moneyRank = moneyLeaderboard.getPlayerPlacement(player.getUniqueId());
                        return moneyRank > 0 ? String.valueOf(moneyRank) : "Not ranked";

                    case "money_top1":
                        return getCachedMoneyLeaderboardEntry(0);
                    case "money_top2":
                        return getCachedMoneyLeaderboardEntry(1);
                    case "money_top3":
                        return getCachedMoneyLeaderboardEntry(2);
                    case "money_top4":
                        return getCachedMoneyLeaderboardEntry(3);
                    case "money_top5":
                        return getCachedMoneyLeaderboardEntry(4);

                    // Level leaderboard placeholders
                    case "level_rank":
                        int levelRank = levelLeaderboard.getPlayerPlacement(player.getUniqueId());
                        return levelRank > 0 ? String.valueOf(levelRank) : "Not ranked";

                    case "level_top1":
                        return getCachedLevelLeaderboardEntry(0);
                    case "level_top2":
                        return getCachedLevelLeaderboardEntry(1);
                    case "level_top3":
                        return getCachedLevelLeaderboardEntry(2);
                    case "level_top4":
                        return getCachedLevelLeaderboardEntry(3);
                    case "level_top5":
                        return getCachedLevelLeaderboardEntry(4);

                    default:
                        return null;
                }
            }
        }
        return null;
    }

    // Helper method to format the money leaderboard entry
    private String formatMoneyLeaderboardEntry(int position) {
        if (moneyLeaderboard == null || position < 0 || position >= moneyLeaderboard.getTop5().size()) {
            return "N/A";
        }

        MoneyEntry entry = moneyLeaderboard.getTop5().get(position);
        return entry.getPlayerName() + ": " + String.format("%.2f", entry.getMoney());
    }

    // Helper method to format the level leaderboard entry
    private String formatLevelLeaderboardEntry(int position) {
        if (levelLeaderboard == null || position < 0 || position >= levelLeaderboard.getTop5().size()) {
            return "N/A";
        }

        LevelEntry entry = levelLeaderboard.getTop5().get(position);
        return entry.getPlayerName() + ": Level " + entry.getLevel();
    }

    // Helper method to get cached money leaderboard entry
    private String getCachedMoneyLeaderboardEntry(int position) {
        long currentTime = System.currentTimeMillis();

        // Check if cache is expired
        if (currentTime - lastCacheUpdateTime > cacheExpirationTime) {
            updateLeaderboardCache();
        }

        return moneyLeaderboardCache.getOrDefault(position, "N/A");
    }

    // Helper method to get cached level leaderboard entry
    private String getCachedLevelLeaderboardEntry(int position) {
        long currentTime = System.currentTimeMillis();

        // Check if cache is expired
        if (currentTime - lastCacheUpdateTime > cacheExpirationTime) {
            updateLeaderboardCache();
        }

        return levelLeaderboardCache.getOrDefault(position, "N/A");
    }

    // Update the leaderboard cache for both money and level leaderboards
    private void updateLeaderboardCache() {
        moneyLeaderboardCache.clear();
        levelLeaderboardCache.clear();

        // Cache money leaderboard
        for (int i = 0; i < 5; i++) {
            String entry = formatMoneyLeaderboardEntry(i);
            moneyLeaderboardCache.put(i, entry);
        }

        // Cache level leaderboard
        for (int i = 0; i < 5; i++) {
            String entry = formatLevelLeaderboardEntry(i);
            levelLeaderboardCache.put(i, entry);
        }

        lastCacheUpdateTime = System.currentTimeMillis(); // Update the cache timestamp
    }

    // Static method to register the PlaceholderAPI hook
    public static void registerHook() {
        new PlaceholderAPIHook().register();
    }
}
