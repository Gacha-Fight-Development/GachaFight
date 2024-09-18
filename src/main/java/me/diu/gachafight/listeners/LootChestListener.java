package me.diu.gachafight.listeners;

import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.FurnitureDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class LootChestListener implements Listener {

    private final GachaFight plugin;
    private final FurnitureDataManager furnitureDataManager; // Add the data manager
    private final int respawnTime = 5 * 60 * 20; // 5 minutes in ticks (5 * 60 seconds * 20 ticks)

    public LootChestListener(GachaFight plugin, FurnitureDataManager furnitureDataManager) {
        this.plugin = plugin;
        this.furnitureDataManager = furnitureDataManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // This method will be triggered when a player interacts with a block
    @EventHandler
    public void onPlayerRightClick(FurnitureInteractEvent event) {
        // Check if the clicked block is a goblin loot chest
        if (isGoblinLootChest(event.getFurniture().getEntity().getLocation())) {
            // Spawn loot and remove the furniture temporarily
            spawnLoot(event.getFurniture().getEntity().getLocation());

            event.getFurniture().remove(false);
            furnitureDataManager.removeFurnitureState(event.getFurniture().getEntity().getLocation());
            furnitureDataManager.saveFurnitureState(event.getFurniture().getEntity().getLocation(), event.getNamespacedID());
            // Schedule respawn after the delay
            respawnFurniture(event.getFurniture().getEntity().getLocation(), event.getNamespacedID());
        }
    }

    // Respawn the furniture after a delay
    private void respawnFurniture(Location location, String furnitureID) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Retrieve the block at the given location and respawn the furniture
                CustomFurniture.spawn(furnitureID, location.getBlock());  // Spawn the furniture at the location
                furnitureDataManager.removeFurnitureState(location);
            }
        }.runTaskLater(plugin, respawnTime);
    }

    // Spawn loot at the location
    private void spawnLoot(Location location) {
        // Example loot spawning commands
        if (Math.random() < 0.5) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 991 " +  //common Key
                    (int) (Math.floor(Math.random() * 2) + 1) + " " + "Spawn " +
                    location.getX() + " " + location.getY() + 1 + " " +
                    location.getZ());
        }
        if (Math.random() < 0.3) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 901 " +  //small hp
                    (int) (Math.floor(Math.random() * 1) + 1) + " " + "Spawn " + location.getX() + " " +
                    location.getY() + 1 + " " + location.getZ());
        }
        if (Math.random() < 0.5) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 611 " +  //gold
                    (int) (Math.floor(Math.random() * 12) + 1) + " " + "Spawn " + location.getX() + " " +
                    location.getY() + 1 + " " + location.getZ());
        }
        if (Math.random() < 0.05) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 992 " +  //uncommon Key
                    (int) (Math.floor(Math.random() * 1) + 1) + " " + "Spawn " + location.getX() + " " +
                    location.getY() + 1 + " " + location.getZ());
        }
    }


    private boolean isGoblinLootChest(Location location) {
        Location[] lootChestLocations = {
                new Location(location.getWorld(), -702.5, 4, 290.5),
                new Location(location.getWorld(), -703.5, 4, 317.5),
                new Location(location.getWorld(), -636.5, 4, 369.5),
                new Location(location.getWorld(), -648.5, 4, 414.5),
                new Location(location.getWorld(), -669.5, 4, 420.5),
                new Location(location.getWorld(), -700.5, 4, 397.5),
                new Location(location.getWorld(), -714.5, 4, 433.5),
                new Location(location.getWorld(), -770.5, 4, 382.5),
                new Location(location.getWorld(), -745.5, 4, 347.5),
                new Location(location.getWorld(), -655.5, 4, 328.5)
        };

        for (Location chestLocation : lootChestLocations) {
            // Compare only the world and (x, y, z) coordinates
            if (location.getWorld().equals(chestLocation.getWorld())
                    && location.getBlockX() == chestLocation.getBlockX()
                    && location.getBlockY() == chestLocation.getBlockY()
                    && location.getBlockZ() == chestLocation.getBlockZ()) {
                return true;
            }
        }
        return false;
    }

    public void saveFurnitureState(Location location, String furnitureID) {
        FileConfiguration config = plugin.getConfig();

        // Save furniture location and ID
        String path = "furniture." + locationToString(location);
        config.set(path + ".id", furnitureID);
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getBlockX());
        config.set(path + ".y", location.getBlockY());
        config.set(path + ".z", location.getBlockZ());

        plugin.saveConfig(); // Save the configuration file
    }

    // Convert location to a string key
    private String locationToString(Location location) {
        return location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }
}
