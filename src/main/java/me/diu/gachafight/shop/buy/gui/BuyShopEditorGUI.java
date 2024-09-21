package me.diu.gachafight.shop.buy.gui;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.ColorChat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BuyShopEditorGUI {

    public static void openGoldShopEditor(Player player, GachaFight plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize("<gold>Gold Shop Editor"));

        // Load items from the Gold Shop and add them to the editor inventory
        List<ItemStack> goldItems = plugin.getBuyItemManager().getGoldShopItems();
        for (int i = 0; i < goldItems.size(); i++) {
            inv.setItem(i, goldItems.get(i));
        }

        player.openInventory(inv);
    }

    public static void openGemShopEditor(Player player, GachaFight plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize("<green>Gem Shop Editor"));

        // Load items from the Gem Shop and add them to the editor inventory
        List<ItemStack> gemItems = plugin.getBuyItemManager().getGemShopItems();
        for (int i = 0; i < gemItems.size(); i++) {
            inv.setItem(i, gemItems.get(i));
        }

        player.openInventory(inv);
    }

    // Handle clicks in the editor GUI for both Gold and Gem shops
    public static void handleEditorClick(InventoryClickEvent event, GachaFight plugin) {
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        event.setCancelled(true);  // Prevent default inventory behavior

        if (event.getClickedInventory() != null) {
            ItemStack currentItem = event.getCurrentItem();
            if (currentItem != null && currentItem.getType() != null) {
                // Determine if it's a Gold or Gem shop based on the title
                boolean isGoldShop = title.contains("Gold Shop Editor");
                boolean isGemShop = title.contains("Gem Shop Editor");

                // Left-click to add the item to the shop
                if (event.isLeftClick() && !currentItem.getType().isAir()) {
                    if (isGoldShop) {
                        plugin.getBuyItemManager().addItemToGoldShop(currentItem.clone());
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Item added to Gold Shop!"));
                    } else if (isGemShop) {
                        plugin.getBuyItemManager().addItemToGemShop(currentItem.clone());
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Item added to Gem Shop!"));
                    }
                    refreshEditor(player, plugin, isGoldShop);  // Refresh the editor
                }
                // Right-click to remove the item from the shop
                else if (event.isRightClick()) {
                    if (isGoldShop) {
                        plugin.getBuyItemManager().removeItemFromGoldShop(event.getSlot());
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Item removed from Gold Shop!"));
                    } else if (isGemShop) {
                        plugin.getBuyItemManager().removeItemFromGemShop(event.getSlot());
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Item removed from Gem Shop!"));
                    }
                    refreshEditor(player, plugin, isGoldShop);  // Refresh the editor
                }
                else if (event.getClick() == ClickType.MIDDLE) {
                    if (isGoldShop || isGemShop) {
                        event.getWhoClicked().getInventory().addItem(event.getCurrentItem());
                    }
                }
            }
        }
    }

    // Refresh the editor based on whether it's for Gold or Gem
    private static void refreshEditor(Player player, GachaFight plugin, boolean isGoldShop) {
        if (isGoldShop) {
            openGoldShopEditor(player, plugin);
        } else {
            openGemShopEditor(player, plugin);
        }
    }
}
