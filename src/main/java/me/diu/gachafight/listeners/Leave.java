package me.diu.gachafight.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.guides.TutorialGuideSystem;
import me.diu.gachafight.playerstats.PlayerDataManager;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.di.ServiceLocator;
import me.diu.gachafight.utils.ExtractLore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Leave implements Listener {

    private final PlayerDataManager playerDataManager;
    private final GachaFight plugin;
    public Leave(GachaFight plugin, ServiceLocator serviceLocator) {
        this.plugin = plugin;
        this.playerDataManager = serviceLocator.getService(PlayerDataManager.class);
        Bukkit.getPluginManager().registerEvents(this, GachaFight.getInstance());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerDataManager.savePlayerData(player);
        PlayerStats.playerStatsMap.remove(player.getUniqueId());
        plugin.getGuideSystem().removeGuideForPlayer(player);
        GachaFight.getInstance().cancelPlayerTasks(player);
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        playerDataManager.savePlayerData(player);
        PlayerStats.playerStatsMap.remove(player.getUniqueId());
        plugin.getGuideSystem().removeGuideForPlayer(player);
        GachaFight.getInstance().cancelPlayerTasks(player);
    }
}
