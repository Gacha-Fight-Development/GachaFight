package me.diu.gachafight.shop.potion.gui;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PotionShopItemGUI {

    public static void open(Player player, int rarityIndex, GachaFight plugin) {
        String[] rarityNames = {
                "Small Potion",
                "Medium Potion",
                "Large Potion",
                "Extra Large Potion",
                "Premium Potion",
                "Golden Potion",
                "Eagle Potion"
        };

        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize("<gold>" + rarityNames[rarityIndex] + " Shop"));

        List<ItemStack> potions = plugin.getPotionItemManager().getLootTable(rarityIndex); // Retrieve potions based on rarity
        for (int i = 0; i < potions.size(); i++) {
            inv.setItem(i, potions.get(i));
        }

        player.openInventory(inv);
    }

    public static void handleClick(InventoryClickEvent event, GachaFight plugin) {
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        List<String> lore = item.getItemMeta().getLore();

        if (lore == null || lore.size() < 5) {
            return;  // Handle case where lore is not present or is too short
        }

        // Retrieve the price line from the lore
        String priceLine = ChatColor.stripColor(lore.get(4)); // Assume price is on the 5th line

        PlayerStats stats = PlayerStats.getPlayerStats(player);

        if (priceLine.contains("$")) {
            double price = Double.parseDouble(priceLine.replace("$", ""));
            if (stats.getMoney() >= price) {
                stats.setMoney(stats.getMoney() - price);

                // Clone the item and remove the price line from the lore
                ItemStack purchasedItem = item.clone();
                ItemMeta meta = purchasedItem.getItemMeta();
                lore.remove(4);  // Remove the price line
                meta.setLore(lore);
                purchasedItem.setItemMeta(meta);

                // Add the modified item to the player's inventory
                player.getInventory().addItem(purchasedItem);
                String plainDisplayName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Purchased " + plainDisplayName + " for $" + price));
            } else {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You do not have enough money!"));
            }
        } else if (priceLine.contains("gem")) {
            int gemPrice = Integer.parseInt(priceLine.replace(" gem", ""));
            if (stats.getGem() >= gemPrice) {
                stats.setGem(stats.getGem() - gemPrice);

                // Clone the item and remove the price line from the lore
                ItemStack purchasedItem = item.clone();
                ItemMeta meta = purchasedItem.getItemMeta();
                lore.remove(4);  // Remove the price line
                meta.setLore(lore);
                purchasedItem.setItemMeta(meta);

                // Add the modified item to the player's inventory
                player.getInventory().addItem(purchasedItem);
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Purchased " + meta.displayName().toString() + " for " + gemPrice + " gems"));
            } else {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You do not have enough gems!"));
            }
        }
    }
}
