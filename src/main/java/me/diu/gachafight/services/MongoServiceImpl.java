package me.diu.gachafight.services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;

public class MongoServiceImpl implements MongoService {
    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;

    public MongoServiceImpl(FileConfiguration config) {
        String uri = config.getString("mongodb.uri");
        String databaseName = config.getString("mongodb.database");

        mongoClient = MongoClients.create(uri);
        mongoDatabase = mongoClient.getDatabase(databaseName);
    }

    @Override
    public void connect() {
        // Connection is established in the constructor
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
        }
    }
}