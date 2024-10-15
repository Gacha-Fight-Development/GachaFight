package me.diu.gachafight.skills.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.hooks.VaultHook;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.skills.managers.MobDropSelector;
import me.diu.gachafight.utils.ColorChat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MasterMageListener implements Listener {
    private final GachaFight plugin;

    public MasterMageListener(GachaFight plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractNPC(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked().getName().equalsIgnoreCase("master mage"))) {
            return; // Ensure only the Master Mage is interacted with
        }
        Player player = event.getPlayer();
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        if (stats.getLevel() < 10) {
            player.sendMessage(ColorChat.chat("&cYou need at least level 10!"));
        }
        openGUI(player);

    }

    public static void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, InventoryType.DROPPER, MiniMessage.miniMessage().deserialize("<!i><light_purple>Master Mage"));
        ItemStack filler = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta cyanPaneMeta = filler.getItemMeta();
        cyanPaneMeta.setHideTooltip(true);
        filler.setItemMeta(cyanPaneMeta);
        // Set the layout for the GUI
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }
        ColorChat.createItem(inv, Material.SEA_LANTERN, 1, 4, "&d&lMagical Orb", "&6Cost Per Use: &e$10k",
                "&6Info: &7This Magical Orb will give you hint on", "&7which mob will drop a Rarity of Rare Or Higher",
                "&7Skill Book for a High Price! You will only have", "&c30 Minutes &7To Obtain it after revealed");
        player.openInventory(inv);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = ChatColor.stripColor(event.getView().getTitle());
        Player player = (Player) event.getWhoClicked();
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        if (title.equalsIgnoreCase("master mage")) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType().equals(Material.SEA_LANTERN)) {
                if (MobDropSelector.getMob() != null) {
                    player.sendMessage(ColorChat.chat("&cAnother player has already revealed Mob!"));
                    return;
                }
                if (VaultHook.getBalance(player) < 10000) {
                    player.sendMessage(ColorChat.chat("&cYou need at least 10k to get a hint from the Master Mage!"));
                    return;
                }
                VaultHook.withdraw(player, 10000);
                MobDropSelector.changeMobs(player);
                player.sendMessage(ColorChat.chat("&7[&dMagical Orb&7] &a" + MobDropSelector.getMob() + " &7will now drop Rare+ Skill Book"));
            }
        }
    }
}