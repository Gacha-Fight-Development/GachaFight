package me.diu.gachafight.shop.equipmentspecialist;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

public class EquipmentSpecialistNPC implements Listener {

    private final JavaPlugin plugin;

    public EquipmentSpecialistNPC(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onNPCInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getName().equalsIgnoreCase("Equipment")) {
            Player player = event.getPlayer();
            openMainMenu(player);
        }
    }

    // Opens the first GUI (Main Menu)
    public void openMainMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, "Equipment Specialist");

        // Creating the "Level Up Equipment" Item (Anvil)
        ItemStack levelUp = new ItemStack(Material.ANVIL);
        ItemMeta levelUpMeta = levelUp.getItemMeta();
        levelUpMeta.setDisplayName("§aLevel Up Equipment");
        levelUp.setItemMeta(levelUpMeta);

        // Creating the "Coming Soon!" Item (Barrier)
        ItemStack comingSoon = new ItemStack(Material.BARRIER);
        ItemMeta comingSoonMeta = comingSoon.getItemMeta();
        comingSoonMeta.setDisplayName("§cComing Soon!");
        comingSoon.setItemMeta(comingSoonMeta);

        // Creating the "Reroll Stats" Item (Grindstone)
        ItemStack rerollStats = new ItemStack(Material.GRINDSTONE);
        ItemMeta rerollStatsMeta = rerollStats.getItemMeta();
        rerollStatsMeta.setDisplayName("§bReroll Stats §c(Coming Soon!)");
        rerollStats.setItemMeta(rerollStatsMeta);

        // Adding cyan stained glass pane as the border
        ItemStack cyanPane = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta cyanPaneMeta = cyanPane.getItemMeta();
        cyanPaneMeta.setHideTooltip(true);
        cyanPane.setItemMeta(cyanPaneMeta);

        // Set the layout for the GUI
        for (int i = 0; i < menu.getSize(); i++) {
            menu.setItem(i, cyanPane);  // Fill with stained glass
        }
        menu.setItem(10, levelUp);
        menu.setItem(13, comingSoon);
        menu.setItem(16, rerollStats);

        // Open the menu
        player.openInventory(menu);
    }
}
