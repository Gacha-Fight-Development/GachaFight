package me.diu.gachafight.listeners;

import me.diu.gachafight.GachaFight;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.entity.Player;
import me.diu.gachafight.utils.ColorChat;

public class SpawnMessageListener implements Listener {

    public SpawnMessageListener(GachaFight plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        // Check if the command is /spawn
        if (command.equals("/spawn")) {
            // Send the custom message to the player
            player.sendMessage(ColorChat.chat("&6[Server] &eYou cannot use /spawn directly."));
            player.sendMessage(ColorChat.chat("&6[Server] &eFind the exit to teleport to spawn!"));
        }
    }
}
