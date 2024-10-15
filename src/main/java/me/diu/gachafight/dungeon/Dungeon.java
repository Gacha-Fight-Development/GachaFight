package me.diu.gachafight.dungeon;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dungeon {
    private static final long COOLDOWN_DURATION = 40 * 1000; // 40 seconds in milliseconds

    @Getter
    private final String name;
    @Getter
    private final List<String> description;
    private final List<Location> spawnPoints;
    private final long[] spawnCooldowns;
    private int currentSpawnIndex;

    public Dungeon(String name, List<String> description, List<Location> spawnPoints) {
        this.name = name;
        this.description = new ArrayList<>(description);
        this.spawnPoints = new ArrayList<>(spawnPoints);
        this.spawnCooldowns = new long[spawnPoints.size()];
        this.currentSpawnIndex = 0;
    }

    public Location getNextAvailableSpawn() {
        for (int i = 0; i < spawnPoints.size(); i++) {
            int spawnIndex = (currentSpawnIndex + i) % spawnPoints.size();
            if (!isOnCooldown(spawnIndex)) {
                setCooldown(spawnIndex);
                currentSpawnIndex = (spawnIndex + 1) % spawnPoints.size();
                return spawnPoints.get(spawnIndex);
            }
        }
        return null; // All spawn points are on cooldown
    }

    // Check if a spawn point is on cooldown
    private boolean isOnCooldown(int spawnIndex) {
        return System.currentTimeMillis() < spawnCooldowns[spawnIndex];
    }

    // Set cooldown for a spawn point
    private void setCooldown(int spawnIndex) {
        spawnCooldowns[spawnIndex] = System.currentTimeMillis() + COOLDOWN_DURATION;
    }
}
