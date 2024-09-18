package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.leaderboard.MoneyLeaderboard;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Baltop implements CommandExecutor {

    private final GachaFight plugin;

    public Baltop(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getCommand("baltop").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            MoneyLeaderboard leaderboardManager = plugin.getMoneyLeaderboard();
            leaderboardManager.displayLeaderboard(player);
        } else {
            sender.sendMessage("This command can only be used by a player.");
        }
        return true;
    }
}
