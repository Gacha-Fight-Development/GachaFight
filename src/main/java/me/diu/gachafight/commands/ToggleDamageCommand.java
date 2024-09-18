package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.ColorChat;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ToggleDamageCommand implements CommandExecutor {
    private LuckPerms luckPerms;
    public ToggleDamageCommand(GachaFight plugin, LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
        plugin.getCommand("toggledamage").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (player.hasPermission("gachafight.toggledamage")) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + sender.getName() + " permission unset gachafight.toggledamage");
                player.sendMessage(ColorChat.chat("&6Damage Indicator: &aOn"));
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + sender.getName() + " permission set gachafight.toggledamage");
                player.sendMessage(ColorChat.chat("&6Damage Indicator: &cOff"));
            }

        }
        return true;
    }
}
