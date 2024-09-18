package me.diu.gachafight.gacha.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.gacha.gui.RarityLootTableEditorGUI;
import me.diu.gachafight.gacha.gui.RaritySelectionGUI;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GachaChestListener implements Listener {

    private final GachaFight plugin;

    public GachaChestListener(GachaFight plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked().getType().equals(EntityType.PIG) && event.getRightClicked().getName().contains("Gacha Chest")) {
            Player player = event.getPlayer();
            ItemStack key = player.getInventory().getItemInMainHand();

            if (key != null && key.hasItemMeta()) {
                ItemMeta meta = key.getItemMeta();
                if (meta != null && meta.hasDisplayName() && meta.getDisplayName().contains("Gacha Key")) {
                    plugin.getGachaManager().openGacha(player, key);
                    event.setCancelled(true);
                }
            } else {
                player.sendMessage("You need a Gacha Key to interact with the Gacha Chest!");
            }
        }
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String inventoryTitle = event.getView().getTitle();
        if (inventoryTitle.contains(ColorChat.chat("&eGacha Select Rarity"))) {
            RaritySelectionGUI.handleClick(event, plugin);
        } else if (inventoryTitle.contains("Loot Table")) {
            RarityLootTableEditorGUI.handleClick(event, plugin);
        }
    }
}
