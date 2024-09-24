package me.diu.gachafight.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.display.Blocks;
import me.diu.gachafight.playerstats.PlayerDataManager;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.di.ServiceLocator;
import me.diu.gachafight.scoreboard.Board;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Join implements Listener {

    private final GachaFight plugin;
    private final PlayerDataManager playerDataManager;
    private final Board scoreboard;

    public Join(GachaFight plugin, ServiceLocator serviceLocator) {
        this.playerDataManager = serviceLocator.getService(PlayerDataManager.class);
        this.scoreboard = new Board(serviceLocator);
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "warp tutorial " + player.getName());
                }
            }.runTaskLater(plugin, 300L);
            new BukkitRunnable() {
                public void run() {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mm m kill GachaChestSmall");
                    Blocks.spawnTutorialGachaChest();
                }
            }.runTaskLater(plugin, 350L);
        }
        playerDataManager.loadPlayerData(player);
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(playerDataManager.getPlayerStats(player.getUniqueId()).getSpeed()*0.1);
        for (int i = 1; i < 9; i++) {
            plugin.getQuestManager().loadQuestProgress(player, i);
        }
        // Other login tasks
        scoreboard.setScoreBoard(player);
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> scoreboard.setScoreBoard(player), 20, 60);
    }
}