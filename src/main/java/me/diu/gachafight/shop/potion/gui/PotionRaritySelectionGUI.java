package me.diu.gachafight.shop.potion.gui;

import me.diu.gachafight.GachaFight;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PotionRaritySelectionGUI {

    private static final String[] RARITY_NAMES = {
            "Small Potion",
            "Medium Potion",
            "Large Potion",
            "Extra Large Potion",
            "Premium Potion",
            "Golden Potion",
            "Eagle Potion"
    };

    private static final Material[] RARITY_MATERIALS = {
            Material.LEATHER_HORSE_ARMOR,
            Material.LEATHER_HORSE_ARMOR,
            Material.LEATHER_HORSE_ARMOR,
            Material.LEATHER_HORSE_ARMOR,
            Material.LEATHER_HORSE_ARMOR,
            Material.LEATHER_HORSE_ARMOR,
            Material.LEATHER_HORSE_ARMOR,
    };

    public static void openShop(Player player, GachaFight plugin) {
        Inventory inv = Bukkit.createInventory(null, 9, MiniMessage.miniMessage().deserialize("<gold>Select Potion Rarity"));

        for (int i = 0; i < RARITY_NAMES.length; i++) {
            ItemStack item = new ItemStack(RARITY_MATERIALS[i]);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MiniMessage.miniMessage().deserialize("<!i><yellow>" + RARITY_NAMES[i]));
            if (RARITY_NAMES[i].equals("Small Potion")) {
                meta.setCustomModelData(10250);
            } else if (RARITY_NAMES[i].equals("Medium Potion")) {
                meta.setCustomModelData(10225);
            } else if (RARITY_NAMES[i].equals("Large Potion")) {
                meta.setCustomModelData(10050);
            } else if (RARITY_NAMES[i].equals("Extra Large Potion")) {
                meta.setCustomModelData(10050);
            } else if (RARITY_NAMES[i].equals("Premium Potion")) {
                meta.setCustomModelData(10175);
            } else if (RARITY_NAMES[i].equals("Golden Potion")) {
                meta.setCustomModelData(10125);
            } else if (RARITY_NAMES[i].equals("Eagle Potion")) {
                meta.setCustomModelData(10000);
            }
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        player.openInventory(inv);
    }

    public static void openSelectionEditor(Player player, GachaFight plugin) {
        Inventory inv = Bukkit.createInventory(null, 9, MiniMessage.miniMessage().deserialize("<gold>Edit Potion Rarity"));

        for (int i = 0; i < RARITY_NAMES.length; i++) {
            ItemStack item = new ItemStack(RARITY_MATERIALS[i]);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MiniMessage.miniMessage().deserialize("<yellow>" + RARITY_NAMES[i]));
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        player.openInventory(inv);
    }

    public static void handleClick(InventoryClickEvent event, GachaFight plugin) {
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }

        String displayName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        for (int i = 0; i < RARITY_NAMES.length; i++) {
            if (RARITY_NAMES[i].equalsIgnoreCase(displayName)) {
                player.closeInventory();
                if (ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("Edit Potion Rarity")) {
                    PotionShopEditorGUI.open(player, i, plugin); // Opens the editor GUI for admin
                } else {
                    PotionShopItemGUI.open(player, i, plugin); // Opens the purchase GUI for players
                }
                return;
            }
        }
    }
}
