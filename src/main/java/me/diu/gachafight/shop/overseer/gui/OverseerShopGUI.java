package me.diu.gachafight.shop.overseer.gui;

import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.shop.equipmentspecialist.EquipmentSpecialistListener;
import me.diu.gachafight.utils.Calculations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class OverseerShopGUI {

    public static void openOverseerGUI(Player player) {
        // Create the GUI inventory (9 slots)
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize("<gold>Overseer"));

        // Get the player's current stats
        PlayerStats stats = PlayerStats.getPlayerStats(player);

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta cyanPaneMeta = filler.getItemMeta();
        cyanPaneMeta.setHideTooltip(true);
        filler.setItemMeta(cyanPaneMeta);

        // Set the layout for the GUI
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);  // Fill with stained glass
        }
        // Create the items with the cost in the lore
        inv.setItem(0+10, createStatItem(Material.RED_DYE, "<!i><red>HP", Calculations.overseerHPCost(stats.getMaxhp())));
        inv.setItem(1+10, createStatItem(Material.ORANGE_DYE, "<!i><gold>Damage", Calculations.overseerDamageCost(stats.getDamage())));
        inv.setItem(2+10, createStatItem(Material.LIME_DYE, "<!i><green>Armor", Calculations.overseerArmorCost(stats.getArmor())));
        inv.setItem(3+10, createStatItem(Material.YELLOW_DYE, "<!i><yellow>Crit Rate", Calculations.overseerCritChanceCost(stats.getCritChance())));
        inv.setItem(4+10, createStatItem(Material.GLOWSTONE_DUST, "<!i><yellow>Crit Damage", Calculations.overseerCritDmgCost(stats.getCritDmg())));
        inv.setItem(5+10, createStatItem(Material.SUGAR, "<!i><white>Speed", Calculations.overseerSpeedCost(stats.getSpeed())));
        inv.setItem(6+10, createStatItem(Material.LIGHT_GRAY_DYE, "<gray><!i>Dodge Chance", Calculations.overseerDodgeCost(stats.getDodge())));

        // Optional: Add a 'back' button or other options in other slots (e.g., a compass)
        inv.setItem(2+20, EquipmentSpecialistListener.createCustomItem(Material.COMPASS, "§dOverseer Guide", 1, "§6Increase Stats Here.", "§6No Charge Backs.")); // No cost for back

        // Open the GUI for the player
        player.openInventory(inv);
    }

    // Helper method to create stat items with cost in the lore
    private static ItemStack createStatItem(Material material, String displayName, double cost) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize(displayName));
        String costString = String.format("%.2f", cost); //round

        // Set the lore to display the cost
        List<Component> lore = new ArrayList<>();
        lore.add(MiniMessage.miniMessage().deserialize("<!i><gray>Cost: <gold>" + costString + " coins"));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }
}
