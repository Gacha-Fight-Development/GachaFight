package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.TextDisplayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveTextDisplayCommand implements CommandExecutor {

    public RemoveTextDisplayCommand(GachaFight plugin) {
        plugin.getCommand("removetextdisplay").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(TextDisplayUtils.activeDisplays.toString());
        TextDisplayUtils.removeAllDisplays();
        sender.sendMessage("completed");
        return true;
    }
}
