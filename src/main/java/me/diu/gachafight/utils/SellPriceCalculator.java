package me.diu.gachafight.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SellPriceCalculator {

    /**
     * Calculate the sell price of an item based on its rarity and stats.
     *
     * @param item        The item for which the price needs to be calculated.
     * @param rarityIndex The index representing the item's rarity.
     * @return The calculated sell price.
     */
    public static double calculateSellPrice(ItemStack item, int rarityIndex) {
        double basePrice = getBasePriceByRarity(rarityIndex);

        // Modify the price based on the item's stats
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasLore()) {
                // Extract and add damage and armor values to the base price
                double damage = ExtractLore.extractStatFromLore(String.valueOf(meta.getLore()), "Damage:");
                double armor = ExtractLore.extractStatFromLore(String.valueOf(meta.getLore()), "Armor:");

                // You can define your own formula to modify the base price
                basePrice += (damage * 2) + (armor * 1.5);
            }
        }

        return basePrice;
    }

    /**
     * Get the base price based on the item's rarity.
     *
     * @param rarityIndex The index representing the item's rarity.
     * @return The base price for the given rarity.
     */
    private static double getBasePriceByRarity(int rarityIndex) {
        switch (rarityIndex) {
            case 0: return 1.0; // Common
            case 1: return 2.5; // Uncommon
            case 2: return 10.0; // Rare
            case 3: return 30.0; // Epic
            case 4: return 80.0; // Unique
            case 5: return 150.0; // Legendary
            case 6: return 666.0; // Mythic
            case 7: return 777.0; // Event
            default: return 10.0; // Default to Common base price
        }
    }
}
