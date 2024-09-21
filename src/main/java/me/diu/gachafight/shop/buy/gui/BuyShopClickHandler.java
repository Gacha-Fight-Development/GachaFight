package me.diu.gachafight.shop.buy.gui;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.utils.ColorChat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class BuyShopClickHandler {

    public static void handleClick(InventoryClickEvent event, GachaFight plugin) {
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);  // Prevent item movement in the GUI

        // If the clicked item is null or doesn't have item meta, exit
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();

        if (lore == null || lore.size() < 2) {
            return;  // If the lore is missing or too short, ignore the click
        }

        // Extract price information from lore (assuming price is on the last line)
        String priceLine = ChatColor.stripColor(lore.get(lore.size() - 1));

        PlayerStats stats = PlayerStats.getPlayerStats(player);

        // Check if the item is priced with money
        if (priceLine.contains("$")) {
            double price = Double.parseDouble(priceLine.replace("$", ""));
            if (stats.getMoney() >= price) {
                stats.setMoney(stats.getMoney() - price);

                // Clone the item and remove the price from the lore
                ItemStack purchasedItem = item.clone();
                lore.remove(lore.size() - 1);  // Remove the price line
                meta.setLore(lore);
                purchasedItem.setItemMeta(meta);

                // Add the item to the player's inventory
                player.getInventory().addItem(purchasedItem);
                player.sendMessage(ColorChat.chat("&aPurchased " + meta.getDisplayName() + " for $" + price));
            } else {
                player.sendMessage(ColorChat.chat("&cYou don't have enough money!"));
            }
        }
        // Check if the item is priced with gems
        else if (priceLine.contains("gem")) {
            int gemPrice = Integer.parseInt(priceLine.replace(" gem", ""));
            if (stats.getGem() >= gemPrice) {
                stats.setGem(stats.getGem() - gemPrice);

                // Clone the item and remove the price from the lore
                ItemStack purchasedItem = item.clone();
                lore.remove(lore.size() - 1);  // Remove the price line
                meta.setLore(lore);
                purchasedItem.setItemMeta(meta);

                // Add the item to the player's inventory
                player.getInventory().addItem(purchasedItem);
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Purchased " + meta.getDisplayName() + " for " + gemPrice + " gems"));
            } else {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You don't have enough gems!"));
            }
        }
    }
}