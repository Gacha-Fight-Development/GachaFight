package me.diu.gachafight.dungeon;

import lombok.Getter;
import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Dungeon {
    private static final long COOLDOWN_DURATION = 40 * 1000; // 40 seconds in milliseconds

    @Getter
    private final String name;
    @Getter
    private final List<String> description;
    private final List<Location> spawnPoints;
    private final ConcurrentHashMap<Integer, Long> spawnCooldowns = new ConcurrentHashMap<>();
    private int currentSpawnIndex;

    public Dungeon(String name, List<String> description, List<Location> spawnPoints) {
        this.name = name;
        this.description = new ArrayList<>(description);
        this.spawnPoints = new ArrayList<>(spawnPoints);
        for (int i = 0; i < spawnPoints.size(); i++) {
            spawnCooldowns.put(i, 0L);
        }
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
        return System.currentTimeMillis() < spawnCooldowns.getOrDefault(spawnIndex, 0L);
    }

    // Set cooldown for a spawn point
    private void setCooldown(int spawnIndex) {
        spawnCooldowns.put(spawnIndex, System.currentTimeMillis() + COOLDOWN_DURATION);
    }

    public List<Location> getAllSpawnPoints() {
        return new ArrayList<>(spawnPoints);
    }

    public boolean areAllSpawnPointsOnCooldown() {
        for (int i = 0; i < spawnPoints.size(); i++) {
            if (!isOnCooldown(i)) {
                return false;
            }
        }
        return true;
    }

    public long getRemainingCooldown(int spawnIndex) {
        if (spawnIndex < 0 || spawnIndex >= spawnPoints.size()) {
            throw new IllegalArgumentException("Invalid spawn index");
        }
        long cooldownEnd = spawnCooldowns.getOrDefault(spawnIndex, 0L);
        long remainingTime = cooldownEnd - System.currentTimeMillis();
        return Math.max(0, remainingTime);
    }

    public void resetAllCooldowns() {
        for (int i = 0; i < spawnPoints.size(); i++) {
            spawnCooldowns.put(i, 0L);
        }
    }
}
