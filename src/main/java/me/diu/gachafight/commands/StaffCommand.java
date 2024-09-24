package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StaffCommand implements CommandExecutor {
    private LuckPerms luckPerms;
    public StaffCommand(GachaFight plugin, LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
        plugin.getCommand("staff").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (sender.hasPermission("gacha.builder") && !user.getPrimaryGroup().contains("builder")) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + sender.getName() + " parent set builder");
        } else if (sender.hasPermission("gacha.builder") && user.getPrimaryGroup().contains("builder")) {
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + sender.getName() + " parent set default");
        }
        return false;
    }

}
