package me.diu.gachafight.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.hooks.PlaceholderAPIHook;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.utils.ColorChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

public class ChatListener implements Listener {

    public ChatListener(GachaFight plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String playerName = event.getPlayer().getName();
        PlayerStats playerStats = PlayerStats.getPlayerStats(event.getPlayer());
        String prefix = "%img_player%";

        if (event.getPlayer().hasPermission("gacha.owner")) {
            prefix = "%img_manager%";
        } else if (event.getPlayer().hasPermission("gacha.manager")) {
            prefix = "%img_manager%";
        } else if (event.getPlayer().hasPermission("gacha.dev")) {
            prefix = "%img_dev%";
        } else if (event.getPlayer().hasPermission("gacha.admin")) {
            prefix = "%img_admin%";
        } else if (event.getPlayer().hasPermission("gacha.staff")) {
            prefix = "%img_staff%";
        } else if (event.getPlayer().hasPermission("gacha.mod")) {
            prefix = "%img_mod%";
        } else if (event.getPlayer().hasPermission("gacha.builder")) {
            prefix = "%img_builder%";
        } else if (event.getPlayer().hasPermission("gacha.helper")) {
            prefix = "%img_helper%";
        } else if (event.getPlayer().hasPermission("gacha.youtube")) {
            prefix = "%img_youtube%";
        } else if (event.getPlayer().hasPermission("gacha.tiktok")) {
            prefix = "%img_tiktok%";
        } else if (event.getPlayer().hasPermission("gacha.mvpplus")) {
            prefix = "%img_mvp_plus%";
        } else if (event.getPlayer().hasPermission("gacha.mvp")) {
            prefix = "%img_mvp%";
        } else if (event.getPlayer().hasPermission("gacha.vipplus")) {
            prefix = "%img_vip_plus%";
        } else if (event.getPlayer().hasPermission("gacha.vip")) {
            prefix = "%img_vip%";
        }

        // Modify the format to include prefix and properly handle the player's name and message
        prefix = PlaceholderAPI.setPlaceholders(event.getPlayer(), prefix);
        String rawSuffix = PlaceholderAPI.setPlaceholders(event.getPlayer(), "%luckperms_suffix%");
        if (rawSuffix.isEmpty()) {
            event.setFormat(prefix + " §8[§6" + playerStats.getLevel() + "§8] §f%1$s: %2$s");
        } else {
            // Parse the suffix using MiniMessage for color codes
            rawSuffix = rawSuffix.replace("&", "§");
            event.setFormat(prefix + " §8[§6" + playerStats.getLevel() + "§8] §f%1$s " + rawSuffix +  "§f: %2$s");
        }
    }
}
