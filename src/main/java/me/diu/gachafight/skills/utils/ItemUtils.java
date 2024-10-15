package me.diu.gachafight.skills.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemUtils {

    // Utility method to check if an item is a Netherite Upgrade Smithing Template
    public static boolean isNetheriteUpgradeTemplate(ItemStack item) {
        return item != null && item.getType() == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE;
    }
}
