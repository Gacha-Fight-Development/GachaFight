package me.diu.gachafight.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.concurrent.CompletableFuture;

public interface MongoService {
    CompletableFuture<Void> connect();
    MongoDatabase getDatabase();
    MongoCollection<Document> getCollection(String name);
    void close();
}