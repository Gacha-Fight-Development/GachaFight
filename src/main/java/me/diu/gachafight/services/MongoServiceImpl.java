package me.diu.gachafight.services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.diu.gachafight.GachaFight;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CompletableFuture;

public class MongoServiceImpl implements MongoService, AutoCloseable {
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private final GachaFight plugin;
    private final String uri;
    private final String databaseName;

    public MongoServiceImpl(GachaFight plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.uri = config.getString("mongodb.uri");
        this.databaseName = config.getString("mongodb.database");
    }

    @Override
    public CompletableFuture<Void> connect() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    mongoClient = MongoClients.create(uri);
                    mongoDatabase = mongoClient.getDatabase(databaseName);
                    future.complete(null);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        }.runTaskAsynchronously(plugin);

        return future;
    }

    @Override
    public MongoDatabase getDatabase() {
        return mongoDatabase;
    }

    @Override
    public MongoCollection<Document> getCollection(String name) {
        return mongoDatabase.getCollection(name);
    }

    @Override
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            plugin.getLogger().info("MongoDB connection closed.");
        }
    }
}