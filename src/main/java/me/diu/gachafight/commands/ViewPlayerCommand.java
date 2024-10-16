package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.di.ServiceLocator;
import me.diu.gachafight.party.PartyManager;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.services.MongoService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ViewPlayerCommand implements CommandExecutor {
    private final GachaFight plugin;

    public ViewPlayerCommand(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getCommand("viewplayer").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/viewplayer <player>");
            return true;
        }
        String targetPlayerName = args[0].toLowerCase();

        Player targetPlayer = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(targetPlayerName)) {
                PlayerStats stats = PlayerStats.getPlayerStats(p);
                if (stats != null) {
                    sender.sendMessage(stats.showStats(p));
                } else {
                    sender.sendMessage("No stats found for online player: " + p.getName());
                }
                return true;
            }
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetPlayerName);
        UUID uuid = offlinePlayer.getUniqueId();
        PlayerStats stats = PlayerStats.getPlayerStats(uuid);

        if (stats != null) {
            sender.sendMessage(stats.showStats(uuid));
        } else {
            // If stats are null, we need to load them from the database
            sender.sendMessage("Loading stats for offline player: " + targetPlayerName);

            // Assuming you have a method to load player data asynchronously
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                PlayerStats loadedStats = plugin.getPlayerDataManager().loadOfflinePlayerData(args[0], uuid);
                if (loadedStats != null) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        sender.sendMessage(loadedStats.showStats(uuid));
                        // Remove the loaded stats after showing them
                        PlayerStats.playerStatsMap.remove(uuid);
                    });
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                            sender.sendMessage("No stats found for player: " + targetPlayerName)
                    );
                }
            });
        }
        return true;
    }
}
