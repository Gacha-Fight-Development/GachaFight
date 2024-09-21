package me.diu.gachafight.shop.buy.gui;

import me.diu.gachafight.GachaFight;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BuyWithGoldGUI {

    public static void open(Player player, GachaFight plugin) {
        // Create a 27-slot inventory for gold purchases
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize("<gold>Buy with Gold"));

        // Get the items available for purchase with gold
        List<ItemStack> goldItems = plugin.getBuyItemManager().getGoldShopItems(); // This method should return gold-purchasable items
        for (int i = 0; i < goldItems.size(); i++) {
            inv.setItem(i, goldItems.get(i));  // Set each item in the inventory
        }

        // Open the inventory for the player
        player.openInventory(inv);
    }
}

