package me.diu.gachafight.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.playerstats.leaderboard.MoneyEntry;
import me.diu.gachafight.playerstats.leaderboard.MoneyLeaderboard;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    private final MoneyLeaderboard moneyLeaderboard = GachaFight.getInstance().getMoneyLeaderboard();

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
                    case "level":
                        return String.valueOf(stats.getLevel());
                    case "luck":
                        return String.valueOf((stats.getLuck() + stats.getGearStats().getTotalLuck() + stats.getWeaponStats().getLuck()));
                    case "exp":
                        return String.valueOf(stats.getExp());
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
                        return String.format("%.0f", stats.getSpeed()*100) + "%";
                    case "critchance":
                        return String.format("%.1f", stats.getCritChance()*100) + "%";
                    case "critdmg":
                        return String.format("%.0f", (stats.getCritDmg()*100)) + "%";
                    case "dodge":
                        return String.format("%.1f", (stats.getDodge()*100)) + "%";

                    // Placeholder for leaderboard rank of the player
                    case "money_rank":
                        int rank = moneyLeaderboard.getPlayerPlacement(player.getUniqueId());
                        return rank > 0 ? String.valueOf(rank) : "Not ranked";

                    // Placeholder for top 5 leaderboard entries
                    case "top1":
                        return formatLeaderboardEntry(0);
                    case "top2":
                        return formatLeaderboardEntry(1);
                    case "top3":
                        return formatLeaderboardEntry(2);
                    case "top4":
                        return formatLeaderboardEntry(3);
                    case "top5":
                        return formatLeaderboardEntry(4);

                    default:
                        return null;
                }
            }
        }
        return null;
    }
    // Helper method to format the leaderboard entry
    private String formatLeaderboardEntry(int position) {
        if (moneyLeaderboard == null || position < 0 || position >= moneyLeaderboard.getTop5().size()) {
            return "N/A";
        }

        MoneyEntry entry = moneyLeaderboard.getTop5().get(position);
        return entry.getPlayerName() + ": " + String.format("%.2f", entry.getMoney());
    }


    public static void registerHook() {
        new PlaceholderAPIHook().register();
    }
}
