package me.diu.gachafight.dungeon;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dungeon {

    @Getter
    private final String name;
    private final List<Location> spawnPoints = new ArrayList<>();
    private final Map<Integer, Long> spawnCooldowns = new HashMap<>();
    private int currentSpawn = 0;
    @Getter
    private final String description;

    public Dungeon(String name, String description, List<Location> spawnPoints) {
        this.name = name;
        this.description = description;
        this.spawnPoints.addAll(spawnPoints);
    }

    // Get the next available spawn point that's not on cooldown
    public Location getNextAvailableSpawn() {
        for (int i = 0; i < spawnPoints.size(); i++) {
            int spawnIndex = (currentSpawn + i) % spawnPoints.size();
            if (!isOnCooldown(spawnIndex)) {
                setCooldown(spawnIndex);
                currentSpawn = (spawnIndex + 1) % spawnPoints.size();
                return spawnPoints.get(spawnIndex);
            }
        }
        return null; // All spawn points are on cooldown
    }

    // Check if a spawn point is on cooldown
    private boolean isOnCooldown(int spawnIndex) {
        Long cooldownEnd = spawnCooldowns.get(spawnIndex);
        return cooldownEnd != null && cooldownEnd > System.currentTimeMillis();
    }

    // Set cooldown for a spawn point
    private void setCooldown(int spawnIndex) {
        spawnCooldowns.put(spawnIndex, System.currentTimeMillis() + 40 * 1000); // 40 sec cooldown
    }
}
