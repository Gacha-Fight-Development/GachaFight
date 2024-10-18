package me.diu.gachafight;

import lombok.Getter;
import me.diu.gachafight.Pets.PetCommand;
import me.diu.gachafight.afk.AFKManager;
import me.diu.gachafight.afk.AFKZoneListener;
import me.diu.gachafight.commands.*;
import me.diu.gachafight.commands.tabs.*;
import me.diu.gachafight.combat.DamageListener;
import me.diu.gachafight.dungeon.DungeonGUI;
import me.diu.gachafight.guides.TutorialGuideSystem;
import me.diu.gachafight.guild.GuildManager;
import me.diu.gachafight.hooks.VaultHook;
import me.diu.gachafight.listeners.*;
import me.diu.gachafight.party.PartyManager;
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
import me.diu.gachafight.skills.rarity.epic.GhostSwordSkill;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.DungeonUtils;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
        instance = this;
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        initializeLuckPerms();
        setupVault();
        initializeDatabases();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.broadcastMessage(ColorChat.chat("&b&lGachaFight Reloading..."));
        AFKManager.stopAllAFKSessions();
        removeAllDisplay();
        playerDataManager.saveAll();
        playerDataManager.deleteCache();
        guideSystem.cleanupAll();
        GhostSwordSkill.removeAllGhostSwords();
        TextDisplayUtils.removeAllDisplays();
        cancelAllPlayerTasks();
        Bukkit.getScheduler().cancelTasks(this);
        if (diContainer != null) {
            diContainer.shutdown();
        }
        diContainer.shutdown();

        saveConfig();
        if (databaseManager != null) {
            databaseManager.disconnect();
            getLogger().info("Successfully disconnected from the database.");
        }
    }

    private void initializeLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        }
    }
    private boolean setupVault() {
        if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
            return false;
        }
        return VaultHook.setupEconomy();
    }

    private void initializeDatabases() {
        this.diContainer = new DIContainer(this);
        String mysqlUsername = getConfig().getString("mysql.username");
        String mysqlPassword = getConfig().getString("mysql.password");
        this.databaseManager = new DatabaseManager(this, mysqlUsername, mysqlPassword);

        CompletableFuture<Void> mysqlFuture = databaseManager.initializeAsync();
        CompletableFuture<Void> mongoFuture = diContainer.waitForInitialization();

        CompletableFuture.allOf(mysqlFuture, mongoFuture).thenRun(() ->
                Bukkit.getScheduler().runTask(this, this::completeInitialization)
        ).exceptionally(this::handleInitializationError);
    }

    private void completeInitialization() {
        try {
            initializeServices();
            registerEvents();
            registerCommands();
            loadAllPlayerData();
            scheduleTimers();
            scheduleInfoBroadcast();
            Bukkit.broadcastMessage(ColorChat.chat("&b&lGachaFight Fully Loaded"));
            Bukkit.broadcastMessage(ColorChat.chat("&aFull Heal from Reload"));
        } catch (Exception e) {
            getLogger().severe("Failed to initialize GachaFight: " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
    private Void handleInitializationError(Throwable e) {
        getLogger().severe("Failed to initialize databases: " + e.getMessage());
        Bukkit.getScheduler().runTask(this, () -> Bukkit.getPluginManager().disablePlugin(this));
        return null;
    }

    private void initializeServices() {
        System.out.println("playerDataManager");
        this.playerDataManager = new PlayerDataManager(this, diContainer.getService(MongoService.class));
        System.out.println("dicontainer");
        diContainer.registerService(PlayerDataManager.class, this.playerDataManager);
        System.out.println("score");
        this.scoreboard = new Board(diContainer);
        System.out.println("gacha");
        this.GachaLootTableManager = new GachaLootTableManager(this);
        System.out.println("potion");
        this.potionItemManager = new PotionItemManager(this);
        System.out.println("money");
        this.moneyLeaderboard = new MoneyLeaderboard(this);
        System.out.println("levelleader");
        this.levelLeaderboard = new LevelLeaderboard(this);
        System.out.println("quest");
        this.questManager = new QuestManager(this, getDataFolder(), databaseManager);
        System.out.println("quest 2");
        QuestUtils.initialize(questManager);
        System.out.println("gachamanager");
        this.gachaManager = new GachaManager(this, luckPerms, questManager);
        System.out.println("furniture");
        this.furnitureDataManager = new FurnitureDataManager(this);
        System.out.println("questgui");
        this.questGUI = new QuestGUI(questManager);
        System.out.println("buyitem");
        this.buyItemManager = new BuyItemManager(this);
        System.out.println("guide");
        this.guideSystem = new TutorialGuideSystem(this);
        System.out.println("party");
        PartyManager.initialize(this);

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
            AFKManager.startAFKSession(player);
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
        if (AFKZoneListener.afkDummy != null) {
            AFKZoneListener.afkDummy.remove();
        }
    }

    private void registerCommands() {
        new PlayerDataCommand(this, diContainer);
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
        new PetCommand(this);
        PartyManager.initialize(this);
        GuildManager.initialize(this);
        new PartyCommand(this);
        new PartyTabCompleter(this);
        new GuildCommand(this);
        new GuildTabCompleter(this);
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
        new AFKZoneListener(this);
        new SkillSystem(this);
        new ChunkUnloadListener(this);
    }
    public void scheduleSound() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.stopAllSounds();
                    if (DungeonUtils.isSafezone(player.getLocation())) {
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

    private void scheduleInfoBroadcast() {
        List<String> messages = Arrays.asList(
                "&6TIP: &eBeing in a Party adds 0.05x Gold/EXP Boost for Each Player!",
                "&6TIP: &eCannot Find NPC? use /guide!",
                "&6TIP: &eConfused about something? use /help!"
        );

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                Bukkit.broadcastMessage(ColorChat.chat(messages.get(index)));
                index = (index + 1) % messages.size();
            }
        }.runTaskTimer(this, 20 * 60 * 5, 20 * 60 * 15); // Start after 5 minutes, repeat every 15 minutes
    }


}
