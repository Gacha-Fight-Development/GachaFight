package me.diu.gachafight.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public interface MongoService {
    void connect();
    MongoDatabase getDatabase();
    MongoCollection<Document> getCollection(String name);
    void close();
}
