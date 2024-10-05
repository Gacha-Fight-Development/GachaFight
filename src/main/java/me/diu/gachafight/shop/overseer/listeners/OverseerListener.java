package me.diu.gachafight.shop.overseer.listeners;

import me.diu.gachafight.shop.overseer.gui.OverseerShopGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class OverseerListener implements Listener {

    @EventHandler
    public void onPlayerInteractWithEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        // Check if the entity is named "Overseer"
        if (entity.getName() != null && entity.getName().equalsIgnoreCase("Overseer")) {
            event.setCancelled(true);  // Prevent further interaction
            // Open the Overseer GUI
            OverseerShopGUI.openOverseerGUI(player);
        }
    }
}

