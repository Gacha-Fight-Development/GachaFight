package me.diu.gachafight.utils;

import dev.lone.itemsadder.api.CustomFurniture;
import me.diu.gachafight.GachaFight;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class FurnitureDataManager {
    private final JavaPlugin plugin;
    private File furnitureDataFile;
    private FileConfiguration furnitureDataConfig;

    public FurnitureDataManager(GachaFight plugin) {
        this.plugin = plugin;
        createFurnitureDataFile();
    }

    // Create and load the furniture_data.yml file
    private void createFurnitureDataFile() {
        furnitureDataFile = new File(plugin.getDataFolder(), "furniture_data.yml");
        if (!furnitureDataFile.exists()) {
            try {
                furnitureDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        furnitureDataConfig = YamlConfiguration.loadConfiguration(furnitureDataFile);
    }

    // Save furniture data to the file
    public void saveFurnitureState(Location location, String furnitureID) {
        String path = "furniture." + locationToString(location);
        furnitureDataConfig.set(path + ".id", furnitureID);
        furnitureDataConfig.set(path + ".world", location.getWorld().getName());
        furnitureDataConfig.set(path + ".x", location.getBlockX());
        furnitureDataConfig.set(path + ".y", location.getBlockY());
        furnitureDataConfig.set(path + ".z", location.getBlockZ());
        saveFurnitureData();
    }

    // Remove furniture data from the file after respawning
    public void removeFurnitureState(Location location) {
        String path = "furniture." + locationToString(location);
        furnitureDataConfig.set(path, null); // Remove the entry
        saveFurnitureData();
    }

    // Load furniture data on plugin start and check for missing furniture
    public void loadMissingFurniture() {
        if (furnitureDataConfig.contains("furniture")) {
            Set<String> keys = furnitureDataConfig.getConfigurationSection("furniture").getKeys(false);
            for (String key : keys) {
                String path = "furniture." + key;
                String worldName = furnitureDataConfig.getString(path + ".world");
                int x = furnitureDataConfig.getInt(path + ".x");
                int y = furnitureDataConfig.getInt(path + ".y");
                int z = furnitureDataConfig.getInt(path + ".z");
                String furnitureID = furnitureDataConfig.getString(path + ".id");

                Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                if (!isFurnitureAtLocation(location)) {
                    CustomFurniture.spawn(furnitureID, location.getBlock());
                }
            }
        }
    }

    // Helper to save changes to the file
    private void saveFurnitureData() {
        try {
            furnitureDataConfig.save(furnitureDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Check if a furniture exists at the location
    private boolean isFurnitureAtLocation(Location location) {
        return location.getBlock().getType() != Material.AIR; // Check for a valid block (can be customized)
    }

    // Convert location to string for storing in the YML
    private String locationToString(Location location) {
        return location.getWorld().getName() + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
    }
}

