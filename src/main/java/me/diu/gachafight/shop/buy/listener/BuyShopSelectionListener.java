package me.diu.gachafight.shop.buy.listener;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.shop.buy.gui.BuyWithGoldGUI;
import me.diu.gachafight.shop.buy.gui.BuyWithGemGUI;
import me.diu.gachafight.shop.buy.gui.BuyShopSelectionGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BuyShopSelectionListener implements Listener {
    private final GachaFight plugin;

    public BuyShopSelectionListener(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = ChatColor.stripColor(event.getView().getTitle());
        Player player = (Player) event.getWhoClicked();

        if (title.equalsIgnoreCase("Buy Shop Selection")) {
            event.setCancelled(true);  // Cancel item moving behavior

            if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()) {
                String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

                // Check if the player clicked "Buy with Gold"
                if (itemName.equalsIgnoreCase("Buy with Gold")) {
                    player.closeInventory();
                    BuyWithGoldGUI.open(player, plugin);  // Open the Gold shop GUI
                }

                // Check if the player clicked "Buy with Gems"
                if (itemName.equalsIgnoreCase("Buy with Gems")) {
                    player.closeInventory();
                    BuyWithGemGUI.open(player, plugin);  // Open the Gem shop GUI
                }
            }
        }
    }
}
