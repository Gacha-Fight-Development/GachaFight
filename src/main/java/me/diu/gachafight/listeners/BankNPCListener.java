package me.diu.gachafight.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.hooks.VaultHook;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BankNPCListener implements Listener {

    public BankNPCListener(GachaFight plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // Handle player interaction with an NPC
    @EventHandler
    public void onPlayerInteractWithNPC(PlayerInteractEntityEvent event) {

        // Check if the NPC's name is "Bank"
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // Ignore offhand interactions
        }
        if (event.getRightClicked().getName().equals("bank")) {
            redeemGold(event.getPlayer());
        }
    }

    // Helper method to remove gold from player's inventory and calculate the total value
    public static int removeGoldFromInventory(Player player, Material material, String displayName, int valuePerItem) {
        Inventory inventory = player.getInventory();
        int totalGoldValue = 0;

        // Iterate over the player's inventory to find gold items
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            if (item != null && item.getType() == material && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(displayName)) {
                    int amount = item.getAmount();
                    totalGoldValue += amount * valuePerItem;

                    // Remove the gold item from the inventory
                    inventory.setItem(i, null);
                }
            }
        }
        return totalGoldValue;
    }

    public static void redeemGold(Player player) {
        // Handle gold conversion
        int goldNuggets = removeGoldFromInventory(player, Material.GOLD_NUGGET, "§6Gold", 1); // Gold nuggets are worth $1 each
        int goldBars = removeGoldFromInventory(player, Material.GOLD_INGOT, "§6Gold Bar", 100); // Gold bars are worth $100 each
        int goldBlocks = removeGoldFromInventory(player, Material.GOLD_BLOCK, "§6Gold Block", 10000); // Gold blocks are worth $10,000 each

        // Total value of gold items
        int totalGoldValue = goldNuggets + goldBars + goldBlocks;

        if (totalGoldValue > 0) {
            // Add money to the player's stats
            PlayerStats playerStats = PlayerStats.getPlayerStats(player);
            VaultHook.addMoney(player, totalGoldValue);

            // Inform the player
            player.sendMessage(ColorChat.chat("&a+&6$&e" + totalGoldValue + "."));
        } else {
            player.sendMessage("You have no gold to exchange.");
        }
    }
}
