package me.diu.gachafight.di;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerDataManager;
import me.diu.gachafight.scoreboard.Board;
import me.diu.gachafight.services.MongoService;
import me.diu.gachafight.services.MongoServiceImpl;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class DIContainer implements ServiceLocator {
    private final Map<Class<?>, Object> services = new HashMap<>();

    public DIContainer(GachaFight plugin) {
        //MongoDB
        FileConfiguration config = plugin.getConfig();
        MongoService mongoService = new MongoServiceImpl(config);
        services.put(MongoService.class, mongoService);

        //PlayerDataManager
        PlayerDataManager playerDataManager = new PlayerDataManager(plugin, this);
        services.put(PlayerDataManager.class, playerDataManager);
        //Board
        Board board = new Board(this);
        services.put(Board.class, board);
        //damage calculator
//        DamageCalculator damageCalculator = new DamageCalculator();
//        services.put(DamageCalculator.class, damageCalculator);
        //playerhologram
        //AdvancementPacketListener advancementPacketListener = new AdvancementPacketListener((Core) plugin, this);
        //services.put(AdvancementPacketListener.class, advancementPacketListener);
        //sneaklistener
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        return (T) services.get(serviceClass);
    }

    public void shutdown() {
        MongoService mongoService = getService(MongoService.class);
        if (mongoService != null) {
            mongoService.close();
        }
    }
}