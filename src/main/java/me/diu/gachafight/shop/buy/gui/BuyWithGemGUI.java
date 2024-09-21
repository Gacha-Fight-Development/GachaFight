package me.diu.gachafight.shop.buy.gui;

import me.diu.gachafight.GachaFight;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BuyWithGemGUI {

    public static void open(Player player, GachaFight plugin) {
        // Create a 27-slot inventory for gem purchases
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize("<green>Buy with Gems"));

        // Get the items available for purchase with gems
        List<ItemStack> gemItems = plugin.getBuyItemManager().getGemShopItems();  // This method should return gem-purchasable items
        for (int i = 0; i < gemItems.size(); i++) {
            inv.setItem(i, gemItems.get(i));  // Set each item in the inventory
        }

        // Open the inventory for the player
        player.openInventory(inv);
    }
}