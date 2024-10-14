package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PromoteCommand implements CommandExecutor {
    private final LuckPerms luckPerms;
    public PromoteCommand(GachaFight plugin, LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
        plugin.getCommand("promote").setExecutor(this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length > 1) {
            if (player.hasPermission("gacha.helper") || player.hasPermission("gacha.builder")) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + "parent add " + args[1].toLowerCase());
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + "parent set " + args[1].toLowerCase());
            }
        }
        return true;
    }
}
