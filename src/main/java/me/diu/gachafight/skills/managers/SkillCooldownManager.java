package me.diu.gachafight.skills.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillCooldownManager {

    // Map to store cooldowns per player per slot (slot -> cooldown end time)
    public static final Map<UUID, Map<Integer, Long>> cooldowns = new HashMap<>();

    // Set cooldown for a specific slot
    public static void setCooldown(UUID playerUUID, int slot, int seconds) {
        long cooldownEndTime = System.currentTimeMillis() + (seconds * 1000);

        // Check if the player already has cooldowns tracked
        if (!cooldowns.containsKey(playerUUID)) {
            cooldowns.put(playerUUID, new HashMap<>());
        }

        // Set the cooldown for the specific slot
        cooldowns.get(playerUUID).put(slot, cooldownEndTime);
    }

    // Check if a player has a cooldown for a specific slot
    public static boolean isOnCooldown(UUID playerUUID, int slot) {
        if (!cooldowns.containsKey(playerUUID)) {
            return false;
        }

        Map<Integer, Long> playerCooldowns = cooldowns.get(playerUUID);
        Long cooldownEndTime = playerCooldowns.get(slot);

        return cooldownEndTime != null && System.currentTimeMillis() < cooldownEndTime;
    }

    // Get remaining cooldown time for a specific slot
    public static long getRemainingCooldown(UUID playerUUID, int slot) {
        if (!cooldowns.containsKey(playerUUID)) {
            return 0;
        }

        Map<Integer, Long> playerCooldowns = cooldowns.get(playerUUID);
        Long cooldownEndTime = playerCooldowns.get(slot);

        if (cooldownEndTime == null || System.currentTimeMillis() >= cooldownEndTime) {
            return 0;
        }

        return (cooldownEndTime - System.currentTimeMillis()) / 1000; // Return remaining time in seconds
    }
}
