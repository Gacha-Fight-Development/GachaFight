package me.diu.gachafight;

import lombok.Getter;
import me.diu.gachafight.commands.*;
import me.diu.gachafight.commands.tabs.AdminPlayerDataTabCompleter;
import me.diu.gachafight.combat.DamageListener;
import me.diu.gachafight.dungeon.DungeonGUI;
import me.diu.gachafight.listeners.*;
import me.diu.gachafight.playerstats.PlayerDataManager;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.di.DIContainer;
import me.diu.gachafight.di.ServiceLocator;
import me.diu.gachafight.display.Blocks;
import me.diu.gachafight.gacha.listeners.GachaChestListener;
import me.diu.gachafight.gacha.managers.GachaManager;
import me.diu.gachafight.gacha.managers.GachaLootTableManager;
import me.diu.gachafight.hooks.PlaceholderAPIHook;
import me.diu.gachafight.playerstats.PlayerStatsListener;
import me.diu.gachafight.playerstats.leaderboard.MoneyLeaderboard;
import me.diu.gachafight.quest.DatabaseManager;
import me.diu.gachafight.quest.QuestManager;
import me.diu.gachafight.quest.gui.QuestGUI;
import me.diu.gachafight.quest.listeners.QuestNPCListener;
import me.diu.gachafight.scoreboard.Board;
import me.diu.gachafight.shop.equipmentspecialist.EquipmentSpecialistListener;
import me.diu.gachafight.shop.equipmentspecialist.EquipmentSpecialistNPC;
import me.diu.gachafight.shop.potion.listeners.PotionUseListener;
import me.diu.gachafight.shop.sell.ShopManager;
import me.diu.gachafight.shop.potion.listeners.PotionShopListener;
import me.diu.gachafight.shop.potion.managers.PotionItemManager;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.FurnitureDataManager;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Getter
public final class GachaFight extends JavaPlugin implements Listener {
    @Getter
    private static GachaFight instance;
    private DatabaseManager databaseManager;
    private DIContainer diContainer;
    private PlayerDataManager playerDataManager;
    private Board scoreboard;
    private GachaManager gachaManager;
    private GachaLootTableManager GachaLootTableManager;
    private PotionItemManager potionItemManager;
    private LuckPerms luckPerms;
    private final Map<Player, Integer> scoreboardTasks = new HashMap<>();
    private final Map<Player, Integer> saveTasks = new HashMap<>();
    private MoneyLeaderboard moneyLeaderboard;
    private FurnitureDataManager furnitureDataManager;
    private QuestManager questManager;
    private QuestGUI questGUI;
    @Override
    public void onEnable() {
        this.instance = this;
        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        }
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        this.diContainer = new DIContainer(this);
        this.databaseManager = new DatabaseManager(this.getConfig().getString("mysql.username"), this.getConfig().getString("mysql.password"));
        this.playerDataManager = diContainer.getService(PlayerDataManager.class);
        this.scoreboard = new Board(diContainer);
        this.GachaLootTableManager = new GachaLootTableManager(this);
        this.potionItemManager = new PotionItemManager(this);
        this.moneyLeaderboard = new MoneyLeaderboard(this);
        this.questManager= new QuestManager(this, databaseManager);
        this.gachaManager = new GachaManager(this, luckPerms, questManager);
        this.furnitureDataManager = new FurnitureDataManager(this);
        this.questGUI = new QuestGUI(questManager);
        registerEvents();
        registerCommands();
        loadAllPlayerData(diContainer);
        Blocks.spawnTutorialGachaChest();
        Blocks.spawnGachaChest();
        furnitureDataManager.loadMissingFurniture();
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderAPIHook.registerHook();
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Blocks.gachaChest != null) {
                    Blocks.gachaChest.remove();
                    Blocks.tutorialGachaChest.remove();
                }
                Blocks.spawnGachaChest();
                Blocks.spawnTutorialGachaChest();
                questManager.checkAllPlayersForExpiredQuests();
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "citizens reload");
            }
        }.runTaskTimer(this, 6000, 4000);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerStats stats = PlayerStats.getPlayerStats(player);
                    stats.updateActionbar(player); // Update action bar with current HP
                }
            }
        }.runTaskTimer(this, 0L, 20L); // Runs every 20 ticks (1 second)
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        removeAllDisplay();
        playerDataManager.saveAll();
        playerDataManager.deleteCache();
        cancelAllPlayerTasks();
        diContainer.shutdown();
        saveConfig();
        Bukkit.broadcastMessage(ColorChat.chat("&b&lGachaFight Reloading..."));
        try {
            databaseManager.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void onLoad() {

    }
    private void removeAllDisplay() {
        if (Blocks.gachaChest != null) {
            Blocks.gachaChest.remove();
        }

        if (Blocks.tutorialGachaChest != null) {
            Blocks.tutorialGachaChest.remove();
        }
    }


    public static void loadAllPlayerData(ServiceLocator serviceLocator) {
        Board scoreboard = serviceLocator.getService(Board.class);
        PlayerDataManager playerDataManager = serviceLocator.getService(PlayerDataManager.class);
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerDataManager.loadPlayerData(player);
            PlayerStats stats = playerDataManager.getPlayerStats(player.getUniqueId());
            int scoreboardTask = Bukkit.getScheduler().runTaskTimer(GachaFight.getInstance(), () -> scoreboard.setScoreBoard(player), 20, 60).getTaskId();
            ((GachaFight) Bukkit.getPluginManager().getPlugin("GachaFight")).getScoreboardTasks().put(player, scoreboardTask);
        }
    }

    private void registerCommands() {
        new PlayerDataCommand(this, diContainer);
        new Discord(this, diContainer);
        new UpdateScoreboard(this,new Board(diContainer));
        new AdminPlayerDataCommand(this, diContainer);
        new AdminPlayerDataTabCompleter(this);
        new ReloadCommand(this);
        new EditGacha(this);
        new CheckModelCommand(this);
        new HelpCommand(this);
        new EditPotionCommand(this);
        new Baltop(this);
        new AutoSellGachaCommand(this, gachaManager, luckPerms);
        new ViewPlayerCommand(this);
        new StaffCommand(this, luckPerms);
        new ToggleDamageCommand(this, luckPerms);
    }

    private void registerEvents() {
        new Board(diContainer);
        new Join(this, diContainer);
        new Leave(this, diContainer);
        new GachaChestListener(this);
        new PlayerStatsListener(this);
        new DungeonGUI(this);
        new ChatListener(this);
        new ShopManager(this);
        new PotionShopListener(this);
        new HealerNPCListener(this);
        new BankNPCListener(this);
        new PotionUseListener(this);
        new LootChestListener(this, furnitureDataManager);
        new SpawnMessageListener(this);
        new EquipmentSpecialistListener(this);
        new EquipmentSpecialistNPC(this);
        new QuestNPCListener(this, questManager);
        new DamageListener(this);
        questManager.startOnlineTimeTracking();
    }

    public void cancelPlayerTasks(Player player) {
        Integer scoreboardTask = scoreboardTasks.remove(player);
        if (scoreboardTask != null) {
            Bukkit.getScheduler().cancelTask(scoreboardTask);
        }

        Integer saveTask = saveTasks.remove(player);
        if (saveTask != null) {
            Bukkit.getScheduler().cancelTask(saveTask);
        }
    }
    private void cancelAllPlayerTasks() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            cancelPlayerTasks(player);
        }
    }
    public void reloadPlugin() {
        onDisable();
        onEnable();
    }

    public GachaManager getGachaManager() {
        return gachaManager;
    }

    public GachaLootTableManager getGachaLootTableManager() {
        return GachaLootTableManager;
    }

    public PotionItemManager getPotionItemManager() { return potionItemManager;}

}
