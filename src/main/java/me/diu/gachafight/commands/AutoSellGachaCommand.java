package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.gacha.managers.GachaManager;
import me.diu.gachafight.utils.ColorChat;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class AutoSellGachaCommand implements CommandExecutor, Listener {

    private final GachaManager gachaManager;
    private final LuckPerms luckPerms;
    private final Plugin plugin;
    private final Map<String, String> rarityPermissions;

    public AutoSellGachaCommand(GachaFight plugin, GachaManager gachaManager, LuckPerms luckPerms) {
        this.gachaManager = gachaManager;
        this.luckPerms = luckPerms;
        this.plugin = plugin;

        plugin.getCommand("autosellgacha").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Map rarities to permissions
        rarityPermissions = new HashMap<>();
        rarityPermissions.put("Common", "gacha.autosell.common");
        rarityPermissions.put("Uncommon", "gacha.autosell.uncommon");
        rarityPermissions.put("Rare", "gacha.autosell.rare");
        rarityPermissions.put("Epic", "gacha.autosell.epic");
        rarityPermissions.put("Unique", "gacha.autosell.unique");
        rarityPermissions.put("Legendary", "gacha.autosell.legendary");
        rarityPermissions.put("Mythic", "gacha.autosell.mythic");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("gacha.autosell") || player.hasPermission("gacha.vip")) {
                openAutoSellGUI(player);
                return true;
            } else {
                player.sendMessage(ColorChat.chat("&cBuy from /Buy or with Gem at Buy Shop"));
                return true;
            }
        }
        return false;
    }

    // Method to open the Auto-Sell GUI
    private void openAutoSellGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ColorChat.chat("&6Auto-Sell Gacha Rarities"));

        // Create items for each rarity
        for (Map.Entry<String, String> entry : rarityPermissions.entrySet()) {
            String rarity = entry.getKey();
            ItemStack item = new ItemStack(Material.PAPER);  // Use PAPER as a placeholder item
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ColorChat.chat("&aAuto-Sell &e" + rarity));
                item.setItemMeta(meta);
            }
            gui.addItem(item);
        }

        player.openInventory(gui);
    }

    // Handle clicks in the GUI
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ColorChat.chat("&6Auto-Sell Gacha Rarities"))) {
            event.setCancelled(true);  // Prevent item movement

            if (event.getWhoClicked() instanceof Player) {
                Player player = (Player) event.getWhoClicked();
                ItemStack clickedItem = event.getCurrentItem();

                if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                    String itemName = clickedItem.getItemMeta().getDisplayName();
                    String rarity = itemName.replace(ColorChat.chat("&aAuto-Sell &e"), "");

                    // Get the permission associated with the clicked rarity
                    String permission = rarityPermissions.get(rarity);
                    if (permission != null) {
                        // Give the player the permission
                        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
                        if (user != null) {
                            user.data().add(Node.builder(permission).build());
                            luckPerms.getUserManager().saveUser(user);

                            player.sendMessage(ColorChat.chat("&aAuto-sell enabled for &e" + rarity));
                        }
                    }
                }
            }
        }
    }

    // Optional: Handle when player closes the inventory
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ColorChat.chat("&6Auto-Sell Gacha Rarities"))) {
            event.getPlayer().sendMessage(ColorChat.chat("&eAuto-sell settings updated!"));
        }
    }
}
