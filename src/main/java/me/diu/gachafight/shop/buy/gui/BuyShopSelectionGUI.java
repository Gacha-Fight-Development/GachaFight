package me.diu.gachafight.shop.buy.gui;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.shop.potion.gui.PotionShopEditorGUI;
import me.diu.gachafight.shop.potion.gui.PotionShopItemGUI;
import me.diu.gachafight.utils.ColorChat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BuyShopSelectionGUI {

    public static void open(Player player) {
        // Create a 9-slot inventory for the selection (simpler layout)
        Inventory inv = Bukkit.createInventory(null, 9, MiniMessage.miniMessage().deserialize("<gold>Buy Shop Selection"));

        // Create the "Buy with Gold" item
        ItemStack buyWithGold = new ItemStack(Material.STICK);
        ItemMeta goldMeta = buyWithGold.getItemMeta();
        goldMeta.setCustomModelData(10007);
        goldMeta.displayName(MiniMessage.miniMessage().deserialize("<!i><yellow>Buy with Gold"));
        buyWithGold.setItemMeta(goldMeta);

        // Create the "Buy with Gem" item
        ItemStack buyWithGem = new ItemStack(Material.IRON_INGOT);
        ItemMeta gemMeta = buyWithGem.getItemMeta();
        gemMeta.setCustomModelData(10002);
        gemMeta.displayName(MiniMessage.miniMessage().deserialize("<!i><green>Buy with Gems"));
        buyWithGem.setItemMeta(gemMeta);

        // Set the items into the inventory
        inv.setItem(3, buyWithGold); // Position 3 for Gold
        inv.setItem(5, buyWithGem);   // Position 5 for Gems

        // Open the inventory for the player
        player.openInventory(inv);
    }
}
