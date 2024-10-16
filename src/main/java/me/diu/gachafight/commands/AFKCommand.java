package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.combat.DamageListener;
import me.diu.gachafight.listeners.PlayerZoneListener;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.DungeonUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AFKCommand implements CommandExecutor {
    public AFKCommand(GachaFight plugin) {
        plugin.getCommand("afk").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (DungeonUtils.isSafezone(player.getLocation())) {
            player.teleport(new Location(player.getWorld(), -8, 177, 331));
            if (!PlayerZoneListener.playerTasks.containsKey(player.getUniqueId())) {
                PlayerZoneListener.startRewardingPlayer(player);
            }
        } else {
            player.sendMessage(ColorChat.chat("&cYou are not in safezone"));
        }
        return true;
    }
}
