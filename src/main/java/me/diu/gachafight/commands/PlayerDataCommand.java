package me.diu.gachafight.commands;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.di.ServiceLocator;
import me.diu.gachafight.services.MongoService;
import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlayerDataCommand implements CommandExecutor {
    private final MongoService mongoService;

    public PlayerDataCommand(GachaFight plugin, ServiceLocator serviceLocator) {
        plugin.getCommand("playerdata").setExecutor(this);
        this.mongoService = serviceLocator.getService(MongoService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        MongoDatabase database = mongoService.getDatabase();
        MongoCollection<Document> collection = database.getCollection("PlayerData");

        if (args.length == 0) {
            // Retrieve player data
            Document playerData = collection.find(Filters.eq("uuid", player.getUniqueId().toString())).first();
            if (playerData != null) {
                player.sendMessage("Player data: " + playerData.toJson());
            } else {
                player.sendMessage("No data found for you.");
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("save")) {
            // Save player data
            Document playerData = new Document("uuid", player.getUniqueId().toString())
                    .append("name", player.getName())
                    .append("lastLogin", System.currentTimeMillis());

            collection.updateOne(Filters.eq("uuid", player.getUniqueId().toString()),
                    new Document("$set", playerData),
                    new UpdateOptions().upsert(true));

            player.sendMessage("Your data has been saved.");
        } else {
            player.sendMessage("Usage: /playerdata [save]");
        }

        return true;
    }
}