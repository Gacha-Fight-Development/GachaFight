package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.di.ServiceLocator;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PayCommand implements CommandExecutor {
    public PayCommand(GachaFight plugin) {
        plugin.getCommand("pay").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command!");
        }
        Player player = (Player) sender;
        PlayerStats playerStats = PlayerStats.getPlayerStats(player);
        if (args.length <= 1) {
            player.sendMessage("/pay <player> <amount>");
            return true;
        }
        String targetPlayerName = args[0].toLowerCase();
        double value = Double.parseDouble(args[1]);
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
        PlayerStats targetPlayerStats = PlayerStats.getPlayerStats(targetPlayer);
        if (playerStats.getMoney() >= value) {
            playerStats.setMoney(playerStats.getMoney() - value);
            player.sendMessage(ColorChat.chat("&aYou have paid $" + value + " to " + targetPlayer.getName()));
            targetPlayerStats.setMoney(targetPlayerStats.getMoney() + value);
            targetPlayer.sendMessage(ColorChat.chat("&a" +player.getName() + " Has sent you $" + value));
            return true;
        } else {
            player.sendMessage(ColorChat.chat("&cNot Enough Money"));
        }
        return true;
    }
}
