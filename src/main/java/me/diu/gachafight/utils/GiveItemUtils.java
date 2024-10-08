package me.diu.gachafight.utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GiveItemUtils {
    public static void giveCommonKey(Player player, int amount) {
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 991 " + amount + " " + player.getName() + " true");
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <white>" +amount+ "x Common Gacha Key"));
    }
    public static void giveUncommonKey(Player player, int amount) {
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 992 " + amount + " " + player.getName() + " true");
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>" +amount+ "x Uncommon Gacha Key"));
    }
    public static void giveGold(Player player, int amount) {
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 611 " + amount + " " + player.getName() + " true");
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gold>" +amount+ "x Gold"));
    }
}
