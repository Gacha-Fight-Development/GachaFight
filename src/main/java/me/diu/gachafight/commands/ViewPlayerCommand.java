package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.di.ServiceLocator;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.services.MongoService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ViewPlayerCommand implements CommandExecutor {

    public ViewPlayerCommand(GachaFight plugin) {
        plugin.getCommand("viewplayer").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/viewplayer <player>");
        }
        String targetPlayerName = args[0].toLowerCase();

        Player targetPlayer = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(targetPlayerName)) {
                targetPlayer = p;
                break;
            }
        }

        if (targetPlayer == null) {
            sender.sendMessage("Player not found online: " + targetPlayerName);
            return true;
        }

        PlayerStats stats = PlayerStats.getPlayerStats(targetPlayer);
        sender.sendMessage(stats.showStats(targetPlayer));
        return true;
    }
}
