package me.diu.gachafight.di;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerDataManager;
import me.diu.gachafight.scoreboard.Board;
import me.diu.gachafight.services.MongoService;
import me.diu.gachafight.services.MongoServiceImpl;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DIContainer implements ServiceLocator {
    private final Map<Class<?>, Object> services = new HashMap<>();
    private final GachaFight plugin;

    public DIContainer(GachaFight plugin) {
        this.plugin = plugin;
        initializeServices();
    }

    private void initializeServices() {
        // MongoDB
        FileConfiguration config = plugin.getConfig();
        MongoService mongoService = new MongoServiceImpl(plugin, config);
        services.put(MongoService.class, mongoService);

        // Connect to MongoDB asynchronously
        mongoService.connect().thenRun(() -> {
            plugin.getLogger().info("Connected to MongoDB successfully!");
        }).exceptionally(e -> {
            plugin.getLogger().severe("Failed to connect to MongoDB: " + e.getMessage());
            return null;
        });


        // Initialize services that don't depend on MongoDB
        Board board = new Board(this);
        services.put(Board.class, board);

        // Other services that don't depend on MongoDB can be initialized here
    }

    public <T> void registerService(Class<T> serviceClass, T serviceInstance) {
        services.put(serviceClass, serviceInstance);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        return (T) services.get(serviceClass);
    }

    public void shutdown() {
        MongoService mongoService = getService(MongoService.class);
        if (mongoService != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    mongoService.close();
                }
            }.runTaskAsynchronously(plugin);
        }
    }

    public CompletableFuture<Void> waitForInitialization() {
        MongoService mongoService = getService(MongoService.class);
        if (mongoService != null) {
            return mongoService.connect();
        }
        return CompletableFuture.completedFuture(null);
    }
}
