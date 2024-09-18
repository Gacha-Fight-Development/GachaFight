package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.di.ServiceLocator;
import me.diu.gachafight.utils.ColorChat;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Discord implements CommandExecutor {
    private final ServiceLocator serviceLocator;

    public Discord(GachaFight plugin, ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
        plugin.getCommand("discord").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            TextComponent message = new TextComponent(ColorChat.chat("&dClick Me!"));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ColorChat.chat("&bClick to join Discord Server!"))));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/XkbVC7pPWh"));
            player.spigot().sendMessage(message);
            return true;
        } else {
            sender.sendMessage("This command can only be used by players.");
            return false;
        }
    }
}
