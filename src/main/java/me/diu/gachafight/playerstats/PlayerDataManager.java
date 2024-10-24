package me.diu.gachafight.playerstats;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.di.ServiceLocator;
import me.diu.gachafight.hooks.VaultHook;
import me.diu.gachafight.services.MongoService;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerDataManager {
    private final GachaFight plugin;
    private final MongoService service;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public PlayerDataManager(GachaFight plugin, MongoService service) {
        this.plugin = plugin;
        this.service = service;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            this.database = service.getDatabase();
            this.collection = database.getCollection("PlayerData");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize MongoDB connection: " + e.getMessage());
        }
    }

    public void loadPlayerData(Player player) {
        if (collection == null) {
            plugin.getLogger().warning("MongoDB collection is not initialized. Using default stats for player: " + player.getName());
            useDefaultStats(player);
            return;
        }

        UUID playerId = player.getUniqueId();
        Document document = collection.find(Filters.eq("uuid", playerId.toString())).first();
        PlayerStats stats = PlayerStats.playerStatsMap.computeIfAbsent(playerId, k -> new PlayerStats(playerId));

        if (document != null) {
            loadStatsFromDocument(stats, document);
            plugin.getLogger().info("Loaded data for player: " + player.getName());
        } else {
            plugin.getLogger().info("No data found for player: " + player.getName() + ", using default stats.");
        }

        updatePlayerStats(player, stats);
    }

    public PlayerStats loadOfflinePlayerData(String playerName, UUID playerUUID) {
        Document document = collection.find(Filters.eq("uuid", playerUUID.toString())).first();
        if (document == null) {
            plugin.getLogger().info("No data found for offline player: " + playerName);
            return null;
        }
        PlayerStats stats = PlayerStats.playerStatsMap.computeIfAbsent(playerUUID, k -> new PlayerStats(playerUUID));
        loadStatsFromDocument(stats, document);
        plugin.getLogger().info("Loaded data for offline player: " + playerName);
        return stats;
    }

    public static void loadStatsFromDocument(PlayerStats stats, Document document) {
        stats.setLevel(document.getInteger("level", 1));
        stats.setGem(document.getInteger("gem", 0));
        if (document.getDouble("exp") != null) stats.setExp(document.getDouble("exp"));
        if (document.getDouble("hp") != null) stats.setMaxhp(document.getDouble("hp"));
        if (document.getDouble("damage") != null) stats.setDamage(document.getDouble("damage"));
        if (document.getDouble("armor") != null) stats.setArmor(document.getDouble("armor"));
        if (document.getDouble("speed") != null) stats.setSpeed(document.getDouble("speed"));
        if (document.getDouble("dodge") != null) stats.setDodge(document.getDouble("dodge"));
        if (document.getDouble("luck") != null) stats.setLuck(document.getDouble("luck"));
        if (document.getDouble("critchance") != null) stats.setCritChance(document.getDouble("critchance"));
        if (document.getDouble("critdmg") != null) stats.setCritDmg(document.getDouble("critdmg"));
    }

    public static void updatePlayerStats(Player player, PlayerStats stats) {
        updateGearStats(player, stats);
        stats.setHp(stats.getMaxhp());
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(stats.getSpeed() * 0.1);

        stats.syncHealthWithHearts(player);
        stats.updateActionbar(player);
    }

    public static void updateGearStats(Player player, PlayerStats stats) {
        PlayerStatsListener.updateSpecificGearStats(stats, player.getInventory().getHelmet(), PlayerArmorChangeEvent.SlotType.HEAD);
        PlayerStatsListener.updateSpecificGearStats(stats, player.getInventory().getChestplate(), PlayerArmorChangeEvent.SlotType.CHEST);
        PlayerStatsListener.updateSpecificGearStats(stats, player.getInventory().getLeggings(), PlayerArmorChangeEvent.SlotType.LEGS);
        PlayerStatsListener.updateSpecificGearStats(stats, player.getInventory().getBoots(), PlayerArmorChangeEvent.SlotType.FEET);
        PlayerStatsListener.updateOffhandStats(stats, player.getInventory().getItemInOffHand());
        PlayerStatsListener.updateWeaponStats(stats, player.getInventory().getItemInMainHand());
    }

    private void useDefaultStats(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerStats stats = new PlayerStats(playerId);
        PlayerStats.playerStatsMap.put(playerId, stats);
        updatePlayerStats(player, stats);
    }

    public void savePlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerStats stats = PlayerStats.playerStatsMap.get(playerId);
        if (stats != null) {
            savePlayerStats(player, player.getName(), stats);
        } else {
            plugin.getLogger().warning("Failed to save data: PlayerStats not found for player " + player.getName());
        }
    }

    public void saveOfflinePlayerData(Player player, String playerName, PlayerStats stats) {
        if (stats != null) {
            savePlayerStats(player, playerName, stats);
        } else {
            plugin.getLogger().warning("Failed to save data: PlayerStats not found for player " + playerName);
        }
    }

    private void savePlayerStats(Player player, String playerName, PlayerStats stats) {
        if ((VaultHook.getBalance(player) < 1 && stats.getGem() < 1) || stats.getLevel() == 1) {
            return; // Skip saving if both money and gems are less than 1
        }
        UUID playerUUID = player.getUniqueId();

        Document document = new Document()
                .append("uuid", playerUUID.toString())
                .append("name", playerName.toLowerCase())
                .append("level", stats.getLevel())
                .append("exp", stats.getExp())
                .append("hp", stats.getMaxhp())
                .append("damage", stats.getDamage())
                .append("armor", stats.getArmor())
                .append("critchance", stats.getCritChance())
                .append("critdmg", stats.getCritDmg())
                .append("luck", stats.getLuck())
                .append("speed", stats.getSpeed())
                .append("dodge", stats.getDodge())
                .append("gem", stats.getGem());

        try {
            collection.replaceOne(
                    Filters.eq("uuid", playerUUID.toString()),
                    document,
                    new ReplaceOptions().upsert(true)
            );
            plugin.getLogger().info("Saved data for player: " + playerName);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save data for player " + playerName + ": " + e.getMessage());
        }
    }

    public PlayerStats getPlayerStats(UUID uuid) {
        return PlayerStats.playerStatsMap.get(uuid);
    }

    public void saveAllPlayerData() {
        for (UUID uuid : PlayerStats.playerStatsMap.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                savePlayerData(player);
            }
        }
    }

    public void saveAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            savePlayerData(player);
        }
    }

    public void deleteCache() {
        PlayerStats.playerStatsMap.clear();
        plugin.getLogger().info("Player data cache cleared.");
    }

}
