package me.diu.gachafight.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.FurnitureDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class LootChestListener implements Listener {

    private final GachaFight plugin;
    private final FurnitureDataManager furnitureDataManager;
    private final int respawnTime = 5 * 60 * 20; // 5 minutes in ticks

    public LootChestListener(GachaFight plugin, FurnitureDataManager furnitureDataManager) {
        this.plugin = plugin;
        this.furnitureDataManager = furnitureDataManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // Player right-clicking (interacting) with the chest
    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();
            Location location = block.getLocation();

            // Check if it's a Goblin or RPG Loot Chest
            if (isGoblinLootChest(location)) {
                // Goblin Loot Chest logic
                block.setType(Material.AIR);  // Remove the chest
                furnitureDataManager.removeFurnitureState(location);
                spawnGoblinLoot(location);
                event.getPlayer().sendMessage(ColorChat.chat("&aGoblin Loot Crate Opened!"));
                furnitureDataManager.saveFurnitureState(location, "minecraft:chest");
                respawnChest(location);
            } else if (isRPGLootChest(location)) {
                // RPG Loot Chest logic
                block.setType(Material.AIR);  // Remove the chest
                furnitureDataManager.removeFurnitureState(location);
                spawnRPGLoot(location);
                event.getPlayer().sendMessage(ColorChat.chat("&aRPG Loot Crate Opened!"));
                furnitureDataManager.saveFurnitureState(location, "minecraft:chest");
                respawnChest(location);
            }
        }
    }

    // Respawn the chest after a delay
    private void respawnChest(Location location) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Respawn the chest at the location
                location.getBlock().setType(Material.CHEST);
                furnitureDataManager.removeFurnitureState(location);
            }
        }.runTaskLater(plugin, respawnTime);
    }

    // Spawn Goblin loot at the location
    private void spawnGoblinLoot(Location location) {
        // Example Goblin loot spawning commands
        if (Math.random() < 0.5) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 991 " +
                    (int) (Math.floor(Math.random() * 2) + 1) + " Spawn " +
                    location.getX() + " " + location.getY() + 1 + " " +
                    location.getZ());
        }
        if (Math.random() < 0.3) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 901 " +
                    (int) (Math.floor(Math.random() * 1) + 1) + " Spawn " + location.getX() + " " +
                    location.getY() + 1 + " " + location.getZ());
        }
        if (Math.random() < 0.5) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 611 " +
                    (int) (Math.floor(Math.random() * 12) + 1) + " Spawn " + location.getX() + " " +
                    location.getY() + 1 + " " + location.getZ());
        }
        if (Math.random() < 0.05) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 992 " +
                    (int) (Math.floor(Math.random() * 1) + 1) + " Spawn " + location.getX() + " " +
                    location.getY() + 1 + " " + location.getZ());
        }
    }

    // Spawn RPG loot at the location
    private void spawnRPGLoot(Location location) {
        // Example RPG loot spawning commands
        if (Math.random() < 0.5) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 991 " +
                    (int) (Math.floor(Math.random() * 2) + 1) + " Spawn " +
                    location.getX() + " " + location.getY() + 1 + " " +
                    location.getZ());
        }
        if (Math.random() < 0.3) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 901 " +
                    (int) (Math.floor(Math.random() * 1) + 1) + " Spawn " + location.getX() + " " +
                    location.getY() + 1 + " " + location.getZ());
        }
        if (Math.random() < 0.5) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 611 " +
                    (int) (Math.floor(Math.random() * 12) + 1) + " Spawn " + location.getX() + " " +
                    location.getY() + 1 + " " + location.getZ());
        }
        if (Math.random() < 0.05) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 992 " +
                    (int) (Math.floor(Math.random() * 1) + 1) + " Spawn " + location.getX() + " " +
                    location.getY() + 1 + " " + location.getZ());
        }
    }

    // Check if the chest location is one of the designated Goblin Loot Chest locations
    private boolean isGoblinLootChest(Location location) {
        Location[] goblinLootChestLocations = {
                new Location(location.getWorld(), -702.5, 4, 290.5), // Goblin start
                new Location(location.getWorld(), -703.5, 4, 317.5),
                new Location(location.getWorld(), -636.5, 4, 369.5),
                new Location(location.getWorld(), -648.5, 4, 414.5),
                new Location(location.getWorld(), -669.5, 4, 420.5),
                new Location(location.getWorld(), -700.5, 4, 397.5),
                new Location(location.getWorld(), -714.5, 4, 433.5),
                new Location(location.getWorld(), -770.5, 4, 382.5),
                new Location(location.getWorld(), -745.5, 4, 347.5),
                new Location(location.getWorld(), -655.5, 4, 328.5) // Goblin end
        };

        for (Location chestLocation : goblinLootChestLocations) {
            if (location.getWorld().equals(chestLocation.getWorld())
                    && location.getBlockX() == chestLocation.getBlockX()
                    && location.getBlockY() == chestLocation.getBlockY()
                    && location.getBlockZ() == chestLocation.getBlockZ()) {
                return true;
            }
        }
        return false;
    }

    // Check if the chest location is one of the designated RPG Loot Chest locations
    private boolean isRPGLootChest(Location location) {
        Location[] rpgLootChestLocations = {
                new Location(location.getWorld(), -927, 8, 430), // RPG start
                new Location(location.getWorld(), -855, 5, 303),
                new Location(location.getWorld(), -853, 5, 368),
                new Location(location.getWorld(), -945, 5, 323),
                new Location(location.getWorld(), -910, 5, 304),
                new Location(location.getWorld(), -916, 5, 419),
                new Location(location.getWorld(), -877, 5, 431),
                new Location(location.getWorld(), -922, 5, 292),
                new Location(location.getWorld(), -960, 5, 356),
                new Location(location.getWorld(), -949, 5, 452),
                new Location(location.getWorld(), -851, 5, 439),
                new Location(location.getWorld(), -851, 5, 447),
                new Location(location.getWorld(), -950, 5, 385),
                new Location(location.getWorld(), -867, 5, 306),
                new Location(location.getWorld(), -913, 5, 290),
                new Location(location.getWorld(), -864, 5, 295),
        };

        for (Location chestLocation : rpgLootChestLocations) {
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
