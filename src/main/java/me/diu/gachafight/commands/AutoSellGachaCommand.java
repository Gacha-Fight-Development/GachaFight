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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class AutoSellGachaCommand implements CommandExecutor, Listener {

    private final GachaManager gachaManager;
    private final LuckPerms luckPerms;
    private final Plugin plugin;

    private final Map<String, Integer> rarityPermissions;

    public AutoSellGachaCommand(GachaFight plugin, GachaManager gachaManager, LuckPerms luckPerms) {
        this.gachaManager = gachaManager;
        this.luckPerms = luckPerms;
        this.plugin = plugin;

        plugin.getCommand("autosellgacha").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);



        // Map equipment percent to defaults
        rarityPermissions = new HashMap<>();
        rarityPermissions.put("Common", 0);
        rarityPermissions.put("Uncommon", 0);
        rarityPermissions.put("Rare", 0);
        rarityPermissions.put("Epic", 0);
        rarityPermissions.put("Unique", 0);
        rarityPermissions.put("Legendary", 0);
        rarityPermissions.put("Mythic", 0);
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
        for (Map.Entry<String, Integer> entry : rarityPermissions.entrySet()) {
            String rarity = entry.getKey();

            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            String getCurrentPerm = user.getNodes().stream()
                    .filter(node -> node.getKey().startsWith("gacha.autosell." + rarity.toLowerCase() + "."))
                    .map(Node::getKey)
                    .collect(Collectors.joining(", "));

            if(getCurrentPerm.isEmpty()){
                getCurrentPerm = "gacha.autosell." + rarity.toLowerCase() + "." + entry.getValue();
                user.data().add(Node.builder(getCurrentPerm).build());
                luckPerms.getUserManager().saveUser(user);
            }
            int damageValue =  Integer.parseInt(getCurrentPerm.split("\\.")[3]);
            ItemStack item = new ItemStack(Material.PAPER);  // Use PAPER as a placeholder item
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ColorChat.chat("&aAuto-Sell &e" + rarity + " below " + damageValue + "%"));
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
                    String[] splitString = (itemName.replace(ColorChat.chat("&aAuto-Sell &e"), "").replace(" below", "").replace("%", "")).split(" ");
                    String rarity = splitString[0];
                    int itemPercentValue = Integer.parseInt(splitString[1]);
                    // Get the permission associated with the clicked rarity
                    String permission = "gacha.autosell." + rarity + "." + itemPercentValue;
                    User user = luckPerms.getUserManager().getUser(player.getUniqueId());
                    if (user != null) {
                                                // Check if the player already has the permission
                        if (player.hasPermission(permission)) {
                            // Remove old permission
                            user.data().remove(Node.builder(permission).build());
                            luckPerms.getUserManager().saveUser(user);
                            // Update itemPercentValue based on click type and rebuild permission string
                            itemPercentValue = updateItemPercent(itemPercentValue,event.getClick());
                            permission = "gacha.autosell." + rarity + "." + itemPercentValue;
                            // Add new permission
                            user.data().add(Node.builder(permission).build());
                            luckPerms.getUserManager().saveUser(user);

                            // Send message indicating that auto-sell is updated
                            player.sendMessage(ColorChat.chat("&cAuto-sell for &e" + rarity + " now set to values of " + itemPercentValue + " or less"));
                            ItemMeta meta = clickedItem.getItemMeta();
                            meta.setDisplayName(ColorChat.chat("&aAuto-Sell &e" + rarity + " below " + itemPercentValue + "%"));
                            clickedItem.setItemMeta(meta);

                        } else {
                            player.sendMessage(ColorChat.chat("&cAuto-sell permission not found.  Please make a bug report! :)"));
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

    // This method will change the damage value used to determine if an item is automatically sold
    public int updateItemPercent(int percent, ClickType clickType){
        switch (clickType){
            case ClickType.LEFT:
                percent += 5;
                break;
            case ClickType.SHIFT_LEFT:
                percent += 25;
                break;
            case ClickType.RIGHT:
                percent -= 5;
                break;
            case ClickType.SHIFT_RIGHT:
                percent -= 25;
                break;
        }

        if(percent > 100) percent = 100;
        if(percent < 0) percent = 0;

        return percent;
    }
}
