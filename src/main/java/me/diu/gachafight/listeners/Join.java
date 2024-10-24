package me.diu.gachafight.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.commands.GuideCommand;
import me.diu.gachafight.display.Blocks;
import me.diu.gachafight.hooks.VaultHook;
import me.diu.gachafight.playerstats.PlayerDataManager;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.di.ServiceLocator;
import me.diu.gachafight.quest.utils.QuestUtils;
import me.diu.gachafight.scoreboard.Board;
import me.diu.gachafight.siege.SiegeGameMode;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.TutorialBossBar;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
        // ============PLAYER STATS=============
        playerDataManager.loadPlayerData(player);
        PlayerStats stats = playerDataManager.getPlayerStats(player.getUniqueId());
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(stats.getSpeed() * 0.1);
        // ============NEW PLAYERS===============
        if (!player.hasPlayedBefore() || (VaultHook.getBalance(player) < 0.1 && stats.getGem() < 1)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "warp tutorial " + player.getName());
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set gacha.tutorial");
                }
            }.runTaskLater(plugin, 10L);
            new BukkitRunnable() {
                public void run() {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "mm m kill GachaChestSmall");
                    Blocks.spawnTutorialGachaChest();
                }
            }.runTaskLater(plugin, 100L);
        } else {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission unset gacha.tutorial");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission unset gacha.tutorial.1");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission unset gacha.tutorial.2");

        }
        // ===========TUTORIAL=============
        if (player.hasPermission("gacha.tutorial") && !player.hasPermission("op")) {
            TutorialBossBar.showTutorialBossBar(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getGuideSystem().guidePlayerToLocation(player, GuideCommand.preSetLocations.get("tutorialgacha"));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
            }.runTaskLater(plugin, 60L);
        }
        // ===========QUEST===============
        for (int i = 1; i < 9; i++) {
            QuestUtils.loadQuestProgress(player, i);
        }
        SiegeGameMode.resumeScheduledTasks(player);
        // =========Scoreboard===========
        scoreboard.setScoreBoard(player);
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> scoreboard.setScoreBoard(player), 20, 60);
    }
}