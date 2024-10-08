package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuyCommand implements CommandExecutor {
    public BuyCommand(GachaFight plugin) {
        plugin.getCommand("buy").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        player.performCommand("csbuy");
        return true;
    }
}
