package me.diu.gachafight.dungeon;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DungeonInstance {

    private final String dungeonName;
    private final Map<Integer, Long> spawnCooldowns = new HashMap<>();
    private final List<Location> spawnPoints = new ArrayList<>();
    private int currentInstance = 1;

    public DungeonInstance(String dungeonName) {
        this.dungeonName = dungeonName;
        initializeSpawnPoints();
    }

    // Initialize spawn points for the dungeon
    private void initializeSpawnPoints() {
        spawnPoints.add(new Location(Bukkit.getWorld("Spawn"), -632.5, 4, 452.5)); // Spawn point 1
        spawnPoints.add(new Location(Bukkit.getWorld("Spawn"), -773.5, 4, 442.5)); // Spawn point 2
        spawnPoints.add(new Location(Bukkit.getWorld("Spawn"), -773.5, 4, 286.5)); // Spawn point 3
        spawnPoints.add(new Location(Bukkit.getWorld("Spawn"), -622.5, 4, 289)); // Spawn point 4
    }

    // Get the next available spawn point that's not on cooldown
    public Location getNextAvailableSpawn() {
        for (int i = 0; i < spawnPoints.size(); i++) {
            int spawnIndex = (currentInstance + i - 1) % spawnPoints.size();
            Location spawn = spawnPoints.get(spawnIndex);

            // Check if the spawn point is on cooldown
            if (!isOnCooldown(spawnIndex)) {
                // Set cooldown for this spawn point
                setCooldown(spawnIndex);

                // Move to the next spawn point for the next teleportation
                currentInstance = (spawnIndex + 1) % spawnPoints.size() + 1;

                return spawn;
            }
        }

        // If all spawn points are on cooldown, create a new instance
        currentInstance++;
        initializeSpawnPoints(); // Initialize new set of spawn points for the new instance
        return getNextAvailableSpawn();
    }

    // Check if a spawn point is on cooldown
    private boolean isOnCooldown(int spawnIndex) {
        Long cooldownEnd = spawnCooldowns.get(spawnIndex);
        return cooldownEnd != null && cooldownEnd > System.currentTimeMillis();
    }

    // Set cooldown for a spawn point
    private void setCooldown(int spawnIndex) {
        spawnCooldowns.put(spawnIndex, System.currentTimeMillis() + 60 * 1200); // 3-minute cooldown
    }
}
