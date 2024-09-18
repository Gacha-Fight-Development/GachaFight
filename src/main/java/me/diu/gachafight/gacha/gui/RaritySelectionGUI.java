package me.diu.gachafight.gacha.gui;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RaritySelectionGUI {

    public static final String[] RARITY_NAMES = {
            "<white>Common",
            "<gray>Uncommon",
            "<green>Rare",
            "<light_blue>Epic",
            "<magenta>Unique",
            "<gold>Legendary",
            "<red>Mythic",
            "<rainbow>Event/Custom"
    };

    public static final String[] RARITY_MATERIALS = {
            "WHITE_WOOL",
            "GRAY_WOOL",
            "LIME_WOOL",
            "LIGHT_BLUE_WOOL",
            "PURPLE_WOOL",
            "ORANGE_WOOL",
            "RED_WOOL",
            "PINK_WOOL"
    };

    public static void openGachaRarity(Player player, GachaFight plugin) {
        Inventory inv = plugin.getServer().createInventory(null, 9, ColorChat.chat("&eGacha Select Rarity"));

        for (int i = 0; i < RARITY_NAMES.length; i++) {
            ColorChat.createItem(inv, RARITY_MATERIALS[i], 1, i, RARITY_NAMES[i]);
        }

        player.openInventory(inv);
    }

    public static void handleClick(InventoryClickEvent event, GachaFight plugin) {
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }

        String displayName = event.getCurrentItem().getItemMeta().getDisplayName();

        for (int i = 0; i < RARITY_NAMES.length; i++) {
            if (ColorChat.chat(RARITY_NAMES[i]).equals(displayName)) {
                player.closeInventory();
                RarityLootTableEditorGUI.openGacha(player, i, plugin);
                return;
            }
        }
    }
}