package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.gacha.managers.GachaManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AutoSellGachaCommand implements CommandExecutor {

    private final GachaManager gachaManager;
    private final LuckPerms luckPerms;

    public AutoSellGachaCommand(GachaFight plugin, GachaManager gachaManager, LuckPerms luckPerms) {
        this.gachaManager = gachaManager;
        this.luckPerms = luckPerms;
        plugin.getCommand("autosellgacha").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Check if the player has the "gacha.vip" permission
            if (player.hasPermission("gacha.vip")) {
                if (args.length == 1) {
                    String action = args[0].toLowerCase();

                    // Handle enabling auto-sell
                    if (action.equals("on")) {
                        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
                        if (user != null) {
                            // Add the "gacha.autosell.gacha" permission
                            user.data().add(Node.builder("gacha.autosell.gacha").build());
                            luckPerms.getUserManager().saveUser(user);
                            player.sendMessage("Auto-sell enabled.");
                        }
                        return true;
                    }

                    // Handle disabling auto-sell
                    if (action.equals("off")) {
                        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
                        if (user != null && !player.isOp()) {
                            // Remove the "gacha.autosell.gacha" permission
                            user.data().remove(Node.builder("gacha.autosell.gacha").build());
                            luckPerms.getUserManager().saveUser(user);
                            player.sendMessage("Auto-sell disabled.");
                        }
                        return true;
                    }
                } else {
                    player.sendMessage("Usage: /autosellgacha <on|off>");
                    return true;
                }
            } else {
                player.sendMessage("You need VIP permission (gacha.vip) to use this command.");
                return true;
            }
        }
        return false;
    }
}
