package me.diu.gachafight.playerstats;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.di.ServiceLocator;
import me.diu.gachafight.services.MongoService;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerDataManager {
    private final GachaFight plugin;
    private final MongoService service;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;

    public PlayerDataManager(GachaFight plugin, ServiceLocator serviceLocator) {
        this.plugin = plugin;
        this.service = serviceLocator.getService(MongoService.class);
        this.database = service.getDatabase();
        this.collection = service.getCollection("PlayerData");
    }

    public void loadPlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        Document document = collection.find(Filters.eq("uuid", playerId.toString())).first();
        PlayerStats stats;

        if (document != null) {
            stats = PlayerStats.playerStatsMap.computeIfAbsent(playerId, k -> new PlayerStats(player.getUniqueId()));
            stats.setLevel(document.getInteger("level", 1));
            stats.setExp(document.getDouble("exp"));
            stats.setDamage(document.getDouble("damage"));
            stats.setArmor(document.getDouble("armor"));
            stats.setMaxhp(document.getDouble("hp"));
            stats.setLuck(document.getInteger("luck", 5));
            stats.setMoney(document.getDouble("money"));
            stats.setGem(document.getInteger("gem", 0));
            if (document.getDouble("speed") != null) {
                stats.setSpeed(document.getDouble("speed"));
            };


            PlayerStatsListener.updateSpecificGearStats(stats, player.getInventory().getHelmet(), PlayerArmorChangeEvent.SlotType.HEAD);
            PlayerStatsListener.updateSpecificGearStats(stats, player.getInventory().getChestplate(), PlayerArmorChangeEvent.SlotType.CHEST);
            PlayerStatsListener.updateSpecificGearStats(stats, player.getInventory().getLeggings(), PlayerArmorChangeEvent.SlotType.LEGS);
            PlayerStatsListener.updateSpecificGearStats(stats, player.getInventory().getBoots(), PlayerArmorChangeEvent.SlotType.FEET);
            PlayerStatsListener.updateOffhandStats(stats, player.getInventory().getItemInOffHand());
            PlayerStatsListener.updateWeaponStats(stats, player.getInventory().getItemInMainHand());

            plugin.getLogger().info("Loaded data for player: " + player.getName());
        } else {
            stats = new PlayerStats(player.getUniqueId());
            PlayerStats.playerStatsMap.put(playerId, stats);
            plugin.getLogger().info("No data found for player: " + player.getName() + ", using default stats.");
        }

        // Sync health with hearts after loading data
        stats.syncHealthWithHearts(player);
        stats.updateActionbar(player); // Update the actionbar with current health

        // Update gear and weapon stats based on the player's current equipment

        // Update weapon stats (main hand item)
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        PlayerStatsListener.updateWeaponStats(stats, mainHandItem);

        // Update armor stats (helmet, chestplate, leggings, boots)
        ItemStack helmet = player.getInventory().getHelmet();
        PlayerStatsListener.updateSpecificGearStats(stats, helmet, PlayerArmorChangeEvent.SlotType.HEAD);

        ItemStack chestplate = player.getInventory().getChestplate();
        PlayerStatsListener.updateSpecificGearStats(stats, chestplate, PlayerArmorChangeEvent.SlotType.CHEST);

        ItemStack leggings = player.getInventory().getLeggings();
        PlayerStatsListener.updateSpecificGearStats(stats, leggings, PlayerArmorChangeEvent.SlotType.LEGS);

        ItemStack boots = player.getInventory().getBoots();
        PlayerStatsListener.updateSpecificGearStats(stats, boots, PlayerArmorChangeEvent.SlotType.FEET);
    }


    public void savePlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerStats stats = PlayerStats.playerStatsMap.get(playerId);
        if (stats != null) {
            Document document = new Document();
            document.put("uuid", player.getUniqueId().toString());
            document.put("name", player.getName().toLowerCase());
            document.put("level", stats.getLevel());
            document.put("exp", stats.getExp());
            document.put("damage", stats.getDamage());
            document.put("armor", stats.getArmor());
            document.put("hp", stats.getMaxhp());
            document.put("luck", stats.getLuck());
            document.put("money", stats.getMoney());
            document.put("gem", stats.getGem());
            document.put("speed", stats.getSpeed());
            collection.replaceOne(Filters.eq("uuid", player.getUniqueId().toString()), document, new ReplaceOptions().upsert(true));
            plugin.getLogger().info("Saved data for player: " + player.getName());
        } else {
            plugin.getLogger().warning("Failed to save data: PlayerStats not found for player " + player.getName());
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
