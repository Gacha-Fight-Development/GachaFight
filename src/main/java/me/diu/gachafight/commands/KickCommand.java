package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand implements CommandExecutor {

    public KickCommand(GachaFight plugin) {
        plugin.getCommand("kick").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player target = Bukkit.getServer().getPlayerExact(args[0]);
        StringBuilder reason = new StringBuilder();
        if(args.length > 1) {
            for (int i = 1; i < args.length; i++){
                reason.append(args[i]).append(" ");
            }
        }else {
            reason = new StringBuilder("unspecify");
        }
        target.kickPlayer(reason.toString());
        Bukkit.broadcastMessage(ColorChat.chat("&c" + target.getName() + " &7has been kicked!"));
        Bukkit.broadcastMessage(ColorChat.chat("&7Reason: " + reason));
        return true;
    }
}
