package me.diu.gachafight.shop.overseer;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.shop.overseer.listeners.OverseerListener;
import me.diu.gachafight.shop.overseer.listeners.OverseerShopClickListener;
import org.bukkit.Bukkit;

public class OverseerManager {
    public OverseerManager(GachaFight plugin) {
        Bukkit.getPluginManager().registerEvents(new OverseerListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new OverseerShopClickListener(), plugin);
    }
}
