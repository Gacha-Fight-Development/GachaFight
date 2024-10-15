package me.diu.gachafight;

import io.lumine.mythic.bukkit.commands.spawners.RemoveCommand;
import lombok.Getter;
import me.diu.gachafight.commands.*;
import me.diu.gachafight.commands.tabs.AdminPlayerDataTabCompleter;
import me.diu.gachafight.combat.DamageListener;
import me.diu.gachafight.commands.tabs.GuideTabCompleter;
import me.diu.gachafight.commands.tabs.ShopTabCompleter;
import me.diu.gachafight.commands.tabs.SkillTabCompleter;
import me.diu.gachafight.dungeon.DungeonGUI;
import me.diu.gachafight.guides.TutorialGuideSystem;
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
import me.diu.gachafight.playerstats.leaderboard.LevelLeaderboard;
import me.diu.gachafight.playerstats.leaderboard.MoneyLeaderboard;
import me.diu.gachafight.quest.DatabaseManager;
import me.diu.gachafight.quest.listeners.QuestClickListener;
import me.diu.gachafight.quest.managers.QuestManager;
import me.diu.gachafight.quest.gui.QuestGUI;
import me.diu.gachafight.quest.listeners.QuestNPCListener;
import me.diu.gachafight.quest.utils.DailyQuestScheduler;
import me.diu.gachafight.quest.utils.QuestUtils;
import me.diu.gachafight.quest.utils.SideQuestScheduler;
import me.diu.gachafight.scoreboard.Board;
import me.diu.gachafight.services.MongoService;
import me.diu.gachafight.shop.buy.BuyItemManager;
import me.diu.gachafight.shop.buy.listener.ShopItemUseListener;
import me.diu.gachafight.shop.equipmentspecialist.EquipmentSpecialistListener;
import me.diu.gachafight.shop.equipmentspecialist.EquipmentSpecialistNPC;
import me.diu.gachafight.shop.overseer.OverseerManager;
import me.diu.gachafight.shop.potion.listeners.PotionUseListener;
import me.diu.gachafight.shop.sell.ShopManager;
import me.diu.gachafight.shop.potion.listeners.PotionShopListener;
import me.diu.gachafight.shop.potion.managers.PotionItemManager;
import me.diu.gachafight.skills.SkillSystem;
import me.diu.gachafight.skills.managers.MobDropSelector;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.FurnitureDataManager;
import me.diu.gachafight.utils.TextDisplayUtils;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
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
    private LevelLeaderboard levelLeaderboard;
    private FurnitureDataManager furnitureDataManager;
    private QuestManager questManager;
    private QuestGUI questGUI;
    private BuyItemManager buyItemManager;
    private TutorialGuideSystem guideSystem;
    @Override
    public void onEnable() {
        this.instance = this;
        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        }
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        // Initialize DIContainer
        this.diContainer = new DIContainer(this);

        String mysqlUsername = getConfig().getString("mysql.username");
        String mysqlPassword = getConfig().getString("mysql.password");
        this.databaseManager = new DatabaseManager(mysqlUsername, mysqlPassword);

        // Wait for MongoDB connection and then initialize other services
        diContainer.waitForInitialization().thenRun(() -> {
            // Run the rest of the initialization on the main thread
            Bukkit.getScheduler().runTask(this, () -> {
                try {
                    initializeServices();
                    registerEvents();
                    registerCommands();
                    loadAllPlayerData();
                    scheduleTimers();

                    Bukkit.broadcastMessage(ColorChat.chat("&b&lGachaFight Fully Loaded"));
                    Bukkit.broadcastMessage(ColorChat.chat("&aFull Heal from Reload"));
                } catch (Exception e) {
                    getLogger().severe("Failed to initialize GachaFight: " + e.getMessage());
                    e.printStackTrace();
                    Bukkit.getPluginManager().disablePlugin(this);
                }
            });
        }).exceptionally(e -> {
            getLogger().severe("Failed to initialize MongoDB connection: " + e.getMessage());
            Bukkit.getScheduler().runTask(this, () -> Bukkit.getPluginManager().disablePlugin(this));
            return null;
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.broadcastMessage(ColorChat.chat("&b&lGachaFight Reloading..."));
        removeAllDisplay();
        playerDataManager.saveAll();
        playerDataManager.deleteCache();
        guideSystem.cleanupAll();
        TextDisplayUtils.removeAllDisplays();
        cancelAllPlayerTasks();
        Bukkit.getScheduler().cancelTasks(this);
        if (diContainer != null) {
            diContainer.shutdown();
        }
        diContainer.shutdown();
        saveConfig();
        try {
            if (databaseManager != null) {
                databaseManager.disconnect();
            }
        } catch (SQLException e) {
            getLogger().severe("Error disconnecting from MySQL: " + e.getMessage());
        }
    }

    private void initializeServices() {
        this.playerDataManager = new PlayerDataManager(this, diContainer.getService(MongoService.class));
        diContainer.registerService(PlayerDataManager.class, this.playerDataManager);
        this.scoreboard = new Board(diContainer);
        this.GachaLootTableManager = new GachaLootTableManager(this);
        this.potionItemManager = new PotionItemManager(this);
        this.moneyLeaderboard = new MoneyLeaderboard(this);
        this.levelLeaderboard = new LevelLeaderboard(this);
        this.questManager = new QuestManager(this, getDataFolder(), databaseManager);
        QuestUtils.initialize(questManager);
        this.gachaManager = new GachaManager(this, luckPerms, questManager);
        this.furnitureDataManager = new FurnitureDataManager(this);
        this.questGUI = new QuestGUI(questManager);
        this.buyItemManager = new BuyItemManager(this);
        this.guideSystem = new TutorialGuideSystem(this);

        File skillsDir = new File(getDataFolder(), "Skills");
        if (!skillsDir.exists()) {
            skillsDir.mkdirs();
        }

        MobDropSelector.init();
        Blocks.spawnGachaChest();
        Blocks.spawnTutorialGachaChest();
        furnitureDataManager.loadMissingFurniture();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderAPIHook.registerHook();
        }
    }

    private void loadAllPlayerData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerDataManager.loadPlayerData(player);
            PlayerStats stats = playerDataManager.getPlayerStats(player.getUniqueId());
            int scoreboardTask = Bukkit.getScheduler().runTaskTimer(this, () -> scoreboard.setScoreBoard(player), 20, 60).getTaskId();
            scoreboardTasks.put(player, scoreboardTask);
            PlayerZoneListener.startRewardingPlayer(player);
        }
    }

    private void scheduleTimers() {
        SideQuestScheduler.scheduleQuestClearTask(this);
        DailyQuestScheduler.scheduleDailyQuestRefresh(this);
        MobDropSelector.scheduleTimer();
        DamageListener.handleFireTicks();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (Blocks.gachaChest != null) {
                    Blocks.gachaChest.remove();
                }
                Blocks.spawnGachaChest();
                guideSystem.cleanupAll();
            }
        }.runTaskTimer(this, 6000, 4000);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerStats stats = PlayerStats.getPlayerStats(player);
                    stats.updateActionbar(player);
                }
            }
        }.runTaskTimerAsynchronously(this, 0L, 20L);

        scheduleSound();
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
            PlayerZoneListener.startRewardingPlayer(player);
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
        new EditShopCommand(this);
        new PayCommand(this);
        new TagsCommand(this);
        new ShopCommand(this);
        new ShopTabCompleter(this);
        new RefreshQuestCommand(this);
        new KickCommand(this);
        new GuideCommand(this);
        new GuideTabCompleter(this);
        new AFKCommand(this);
        new BuyCommand(this);
        new PromoteCommand(this, luckPerms);
        new RemoveTextDisplayCommand(this);
        new SkillCommand(this);
        new SkillTabCompleter(this);
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
        new QuestClickListener(this);
        new OverseerManager(this);
        new ShopItemUseListener(this);
        new FoodConsumeListener(this);
        new PlayerZoneListener(this);
        new SkillSystem(this);
        new ChunkUnloadListener(this);
    }
    public void scheduleSound() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.stopAllSounds();
                    if (DamageListener.isSafezone(player.getLocation())) {
                        player.playSound(player, "custom:celestrial", SoundCategory.MUSIC, 5, 1);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 3000L); //2640
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
            player.closeInventory();
        }
    }
    public void reloadPlugin() {
        onDisable();
        onEnable();
    }

}
