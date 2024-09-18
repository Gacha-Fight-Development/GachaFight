package me.diu.gachafight.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class ChatListener implements Listener {

    public ChatListener(GachaFight plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        String playerName = event.getPlayer().getName();
        PlayerStats playerStats = PlayerStats.getPlayerStats(event.getPlayer());
        String prefix = ":player:";

        if (event.getPlayer().hasPermission("gacha.owner")) {
            prefix = ":owner:";
        } else if (event.getPlayer().hasPermission("gacha.manager")) {
            prefix = ":manager:";
        } else if (event.getPlayer().hasPermission("gacha.dev")) {
            prefix = ":dev:";
        } else if (event.getPlayer().hasPermission("gacha.staff")) {
            prefix = ":manager:";
        } else if (event.getPlayer().hasPermission("gacha.mod")) {
            prefix = ":mod:";
        } else if (event.getPlayer().hasPermission("gacha.builder")) {
            prefix = ":builder:";
        } else if (event.getPlayer().hasPermission("gacha.helper")) {
            prefix = ":helper:";
        } else if (event.getPlayer().hasPermission("gacha.youtube")) {
            prefix = ":youtube:";
        } else if (event.getPlayer().hasPermission("gacha.tiktok")) {
            prefix = ":tiktok:";
        } else if (event.getPlayer().hasPermission("gacha.mvpplus")) {
            prefix = ":mvp_plus:";
        } else if (event.getPlayer().hasPermission("gacha.mvp")) {
            prefix = ":mvp:";
        } else if (event.getPlayer().hasPermission("gacha.vipplus")) {
            prefix = ":vip_plus:";
        } else if (event.getPlayer().hasPermission("gacha.vip")) {
            prefix = ":vip:";
        }

        // Modify the format to include prefix and properly handle the player's name and message
        event.setFormat(prefix + " §8[§6" + playerStats.getLevel() + "§8] §f%1$s: %2$s");
    }
}
