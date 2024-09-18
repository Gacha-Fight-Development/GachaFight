package me.diu.gachafight.shop.sell;

import me.diu.gachafight.GachaFight;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ShopInventoryHolder implements InventoryHolder {

    private final GachaFight plugin;

    public ShopInventoryHolder(GachaFight plugin) {
        this.plugin = plugin;
    }

    @Override
    public Inventory getInventory() {
        return null; // Return null because we do not create the inventory here.
    }

    public GachaFight getPlugin() {
        return plugin;
    }
}
