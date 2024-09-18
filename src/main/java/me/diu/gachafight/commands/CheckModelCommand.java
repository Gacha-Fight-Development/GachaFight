package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckModelCommand implements CommandExecutor {

    public CheckModelCommand(GachaFight plugin) {
        plugin.getCommand("checkmodel").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        player.sendMessage(String.valueOf(player.getItemInHand().getItemMeta().getCustomModelData()));
        return false;
    }
}
