package me.diu.gachafight.utils;

import me.diu.gachafight.playerstats.PlayerStats;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GiveItemUtils {
    public static void giveCommonKey(Player player, int amount) {
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 991 " + amount + " " + player.getName() + " true");
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <white>" +amount+ "x Common Gacha Key"));
    }
    public static void giveUncommonKey(Player player, int amount) {
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 992 " + amount + " " + player.getName() + " true");
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>" +amount+ "x Uncommon Gacha Key"));
    }
    public static void giveGold(Player player, int amount) {
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 611 " + amount + " " + player.getName() + " true");
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gold>" +amount+ "x Gold"));
    }
    public static void giveEXP(Player player, double amount) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        stats.setExp(stats.getExp() + amount);
        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <aqua>" +String.format("%.1f", amount)+ " EXP"));
    }

    public static void convertGold(Player player) {
        Inventory inventory = player.getInventory();
        int goldNuggetsCount = 0;

        // First, count the total number of gold nuggets in the player's inventory
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == Material.GOLD_NUGGET) {
                goldNuggetsCount += item.getAmount();
            }
        }

        // Check if the player has at least 100 gold nuggets
        if (goldNuggetsCount < 100) {
            return; // Not enough gold nuggets, exit the method
        }

        // Now, remove exactly 100 gold nuggets
        int nuggetsToRemove = 100;

        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == Material.GOLD_NUGGET) {
                int itemAmount = item.getAmount();

                if (itemAmount > nuggetsToRemove) {
                    // If the stack has more than needed, just reduce the stack size
                    item.setAmount(itemAmount - nuggetsToRemove);
                    nuggetsToRemove = 0;
                    break;
                } else {
                    // Remove the entire stack and keep track of how many more nuggets we need to remove
                    inventory.remove(item);
                    nuggetsToRemove -= itemAmount;
                }
            }
        }

        // After removing 100 gold nuggets, add 1 gold ingot
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "si give 612 1 " + player.getName() + " true");
        player.sendMessage("You have successfully converted 100 gold nuggets into 1 gold ingot.");
    }
}
