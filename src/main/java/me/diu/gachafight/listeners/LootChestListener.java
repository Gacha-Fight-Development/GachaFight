package me.diu.gachafight.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.DungeonUtils;
import me.diu.gachafight.utils.FurnitureDataManager;
import me.diu.gachafight.utils.LootChestDrop;
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
            if (DungeonUtils.isGoblin(location)) {
                // Goblin Loot Chest logic
                block.setType(Material.AIR);
                furnitureDataManager.removeFurnitureState(location);
                spawnGoblinLoot(location);
                event.getPlayer().sendMessage(ColorChat.chat("&aGoblin Loot Crate Opened!"));
                furnitureDataManager.saveFurnitureState(location, "minecraft:chest");
                respawnChest(location);
            } else if (DungeonUtils.isRPG(location)) {
                // RPG Loot Chest logic
                block.setType(Material.AIR);
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

    private void spawnRPGLoot(Location location) {
        // Example RPG loot spawning commands
        LootChestDrop.dropCommonKey(location, 0.5, 2);
        LootChestDrop.dropSmallHpPot(location, 0.3, 1);
        LootChestDrop.dropGold(location, 0.7, 7);
        LootChestDrop.dropUncommonKey(location, 0.1, 1);
        LootChestDrop.dropGold(location, 1, 2);
    }

    // Spawn Goblin loot at the location
    private void spawnGoblinLoot(Location location) {
        // Example Goblin loot spawning commands
        LootChestDrop.dropCommonKey(location, 0.7, 4);
        LootChestDrop.dropSmallHpPot(location, 0.3, 2);
        LootChestDrop.dropGold(location, 1, 12);
        LootChestDrop.dropUncommonKey(location, 0.35, 1);
        LootChestDrop.dropUncommonKey(location, 0.35, 1);
        LootChestDrop.dropRareKey(location, 0.05, 1);
        LootChestDrop.dropGold(location, 1, 2);
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
