package me.diu.gachafight.shop.potion.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.shop.potion.gui.PotionShopEditorGUI;
import me.diu.gachafight.shop.potion.gui.PotionRaritySelectionGUI;
import me.diu.gachafight.shop.potion.gui.PotionShopItemGUI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PotionShopListener implements Listener {
    private final GachaFight plugin;

    public PotionShopListener(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // Check if the event is triggered in the Potion Rarity Selection GUI
        if (title.contains("Select Potion Rarity")) {
            PotionRaritySelectionGUI.handleClick(event, plugin);
        }
        // Check if the event is triggered in the Potion Shop Item GUI
        else if (title.contains("Potion Shop")) {
            PotionShopItemGUI.handleClick(event, plugin);
        }
        // Check if the event is triggered in the Potion Loot Table Editor GUI
        else if (title.contains("Edit Potion Rarity")) {
            PotionRaritySelectionGUI.handleClick(event, plugin);
        }

        else if (title.contains("Potion Editor")) {
            PotionShopEditorGUI.handleClick(event, plugin);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // Ignore offhand interactions
        }
        if (event.getRightClicked().getName() != null && event.getRightClicked().getName().equalsIgnoreCase("Potion Shop")) {
            event.setCancelled(true);
            PotionRaritySelectionGUI.openShop(event.getPlayer(), plugin); // Opens rarity selection GUI
        }
    }
}

