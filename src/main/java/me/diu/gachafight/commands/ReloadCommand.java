package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final GachaFight plugin;

    public ReloadCommand(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getCommand("gachareload").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("GachaFight.reload")) {
            plugin.reloadPlugin();
            sender.sendMessage(ColorChat.chat("&aPlugin reloaded successfully!"));
        } else {
            sender.sendMessage(ColorChat.chat("&cYou do not have permission to use this command."));
        }
        return true;
    }
}
