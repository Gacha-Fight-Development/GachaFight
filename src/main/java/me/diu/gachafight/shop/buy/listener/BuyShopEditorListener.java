package me.diu.gachafight.shop.buy.listener;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.shop.buy.gui.BuyShopEditorGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BuyShopEditorListener implements Listener {
    private final GachaFight plugin;
    public BuyShopEditorListener(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // If the player is in the Gold or Gem Shop Editor
        if (title.contains("Gold Shop Editor") || title.contains("Gem Shop Editor")) {
            BuyShopEditorGUI.handleEditorClick(event, plugin);
        }
    }
}