package me.diu.gachafight.shop.buy.listener;


import me.diu.gachafight.GachaFight;
import me.diu.gachafight.shop.buy.gui.BuyShopClickHandler;
import me.diu.gachafight.shop.buy.gui.BuyShopSelectionGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class BuyShopListener implements Listener {
    private final GachaFight plugin;

    public BuyShopListener(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // If the shop GUI is open
        if (title.contains("Buy with Gold") || title.contains("Buy with Gem")) {
            BuyShopClickHandler.handleClick(event, plugin);  // Handle shop item purchases
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // If the player interacts with a shop NPC (named "Buy Shop")
        if (event.getRightClicked().getName() != null && event.getRightClicked().getName().equalsIgnoreCase("Buy Shop")) {

            event.setCancelled(true);
            BuyShopSelectionGUI.open(event.getPlayer());  // Open the shop GUI for the player
        }
    }
}