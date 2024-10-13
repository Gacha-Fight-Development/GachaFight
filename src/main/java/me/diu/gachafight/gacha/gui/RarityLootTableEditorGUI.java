package me.diu.gachafight.gacha.gui;

import me.diu.gachafight.GachaFight;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RarityLootTableEditorGUI {

    public static void openGacha(Player player, int rarityIndex, GachaFight plugin) {
        Component rarityName = MiniMessage.miniMessage().deserialize(RaritySelectionGUI.RARITY_NAMES[rarityIndex]);
        Inventory inv = Bukkit.createInventory(null, 54, rarityName + " Loot Table");

        // Load items from the loot table manager and add them to the inventory
        List<ItemStack> lootItems = plugin.getGachaLootTableManager().getLootTable(rarityIndex);
        for (int i = 0; i < lootItems.size(); i++) {
            inv.setItem(i, lootItems.get(i));
        }

        player.openInventory(inv);
    }

    public static void handleClick(InventoryClickEvent event, GachaFight plugin) {
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        int rarityIndex = getRarityIndex(title);

        if (rarityIndex == -1) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Error: Could not determine the rarity from the inventory title."));
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() != null) {
            ItemStack currentItem = event.getCurrentItem();

            if (event.isLeftClick() && !currentItem.getType().isAir()) {
                // Left-click to add an item to the loot table
                plugin.getGachaLootTableManager().addItemToLootTable(rarityIndex, currentItem.clone());
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Item added to " + RaritySelectionGUI.RARITY_NAMES[rarityIndex] + " loot table."));
                openGacha(player, rarityIndex, plugin); // Refresh inventory
            } else if (event.isRightClick() && currentItem != null) {
                // Right-click to remove an item from the loot table
                plugin.getGachaLootTableManager().removeItemFromLootTable(rarityIndex, event.getSlot());
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Item removed from " + RaritySelectionGUI.RARITY_NAMES[rarityIndex] + " loot table."));
                openGacha(player, rarityIndex, plugin); // Refresh inventory
            }  else if (event.getClick() == ClickType.MIDDLE) {
                player.getInventory().addItem(event.getCurrentItem());
            }
        }
    }

    private static int getRarityIndex(String title) {
        for (int i = 0; i < RaritySelectionGUI.RARITY_NAMES.length; i++) {
            if (title.contains(MiniMessage.miniMessage().deserialize(RaritySelectionGUI.RARITY_NAMES[i]).toString())) {
                return i;
            }
        }
        return -1;
    }
}
