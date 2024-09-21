package me.diu.gachafight.shop.potion.gui;

import me.diu.gachafight.GachaFight;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PotionShopEditorGUI {

    private static final String[] rarityNames = {
            "Small Potion", "Medium Potion", "Large Potion",
            "Extra Large Potion", "Premium Potion", "Golden Potion",
            "Eagle Potion"
    };

    public static void open(Player player, int rarityIndex, GachaFight plugin) {

        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize("<gold>" + rarityNames[rarityIndex] + " Editor"));

        // Load items from the loot table manager and add them to the inventory
        List<ItemStack> lootItems = plugin.getPotionItemManager().getLootTable(rarityIndex);
        for (int i = 0; i < lootItems.size(); i++) {
            inv.setItem(i, lootItems.get(i));
        }

        player.openInventory(inv);
    }

    public static void handleClick(InventoryClickEvent event, GachaFight plugin) {
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        if (event.getClickedInventory() != null) {
            ItemStack currentItem = event.getCurrentItem();
            int rarityIndex = getRarityIndex(event.getView().getTitle());

            if (rarityIndex != -1) {
                if (event.isLeftClick() && !currentItem.getType().isAir()) {
                    // Add the item from player's cursor
                    plugin.getPotionItemManager().addItemToLootTable(rarityIndex, currentItem.clone());
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Item added to " + rarityNames[rarityIndex] + " loot table!"));
                    open(player, rarityIndex, plugin); // Refresh inventory
                } else if (event.isRightClick()) {
                    // Right-click to remove an item from the loot table
                    plugin.getPotionItemManager().removeItemFromLootTable(rarityIndex, event.getSlot());
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Item removed"));
                    open(player, rarityIndex, plugin); // Refresh inventory
                } else if (event.getClick() == ClickType.MIDDLE) {
                    player.getInventory().addItem(event.getCurrentItem());
                }
            }
        }
    }

    private static int getRarityIndex(String title) {
        String[] rarityNames = {
                "Small Potion", "Medium Potion", "Large Potion",
                "Extra Large Potion", "Premium Potion", "Golden Potion",
                "Eagle Potion"
        };

        for (int i = 0; i < rarityNames.length; i++) {
            if (title.contains(rarityNames[i])) {
                return i;
            }
        }
        return -1;
    }
}

