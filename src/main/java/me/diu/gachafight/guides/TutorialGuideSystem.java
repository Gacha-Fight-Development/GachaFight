package me.diu.gachafight.guides;

import me.diu.gachafight.GachaFight;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TutorialGuideSystem {

    private final GachaFight plugin;
    private final Map<UUID, ItemDisplay> guidingDisplays = new HashMap<>();

    public TutorialGuideSystem(GachaFight plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts guiding a player to a location using an ItemDisplay.
     *
     * @param player      The player who will be guided.
     * @param destination The target location to guide the player to.
     */
    public void guidePlayerToLocation(Player player, Location destination) {
        // Spawn the ItemDisplay 2 blocks in front of the player at eye level
        Location displayLocation = getDisplaySpawnLocation(player);
        ItemDisplay itemDisplay = player.getWorld().spawn(displayLocation, ItemDisplay.class, (display) -> {
            display.setItemStack(new ItemStack(Material.SPECTRAL_ARROW)); // Set item to arrow
            display.setVisibleByDefault(false);                  // Hide from other players
        });

        // Show the ItemDisplay to the player only
        player.showEntity(plugin, itemDisplay);

        // Store the ItemDisplay for future reference
        guidingDisplays.put(player.getUniqueId(), itemDisplay);

        // Start a task to move the display towards the destination and guide the player
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!guidingDisplays.containsKey(player.getUniqueId())) return;

            // Calculate distance from player to the destination
            Location playerLocation = player.getLocation();
            double distanceToTarget = playerLocation.distance(destination);

            if (distanceToTarget <= 1.8) {
                // If the player is close to the destination, remove the display
                removeGuideForPlayer(player);
                player.sendMessage("You have reached your destination!");
            } else {
                // Move the display closer to the destination and rotate it
                Location currentDisplayLocation = itemDisplay.getLocation();
                Vector directionToTarget = destination.toVector().subtract(currentDisplayLocation.toVector()).normalize();

                // Move the display closer to the target
                Location newLocation = currentDisplayLocation.clone().add(directionToTarget.multiply(0.5));
                itemDisplay.teleport(newLocation);

            }
        }, 0L, 2L); // Run every 2 ticks (10 times per second)
    }

    /**
     * Removes the guiding ItemDisplay for a specific player.
     *
     * @param player The player whose guide will be removed.
     */
    public void removeGuideForPlayer(Player player) {
        ItemDisplay display = guidingDisplays.remove(player.getUniqueId());
        if (display != null) {
            display.remove(); // Remove the display from the world
        }
    }

    /**
     * Calculates the spawn location of the guiding display, 2 blocks in front of the player at eye level.
     *
     * @param player The player to calculate the location for.
     * @return The location where the guiding display will spawn.
     */
    private Location getDisplaySpawnLocation(Player player) {
        // Get the player's eye location (at head level)
        Location eyeLocation = player.getEyeLocation();

        // Get the player's facing direction (normalized direction vector)
        Vector direction = eyeLocation.getDirection().normalize();

        // Calculate the location 2 blocks in front of the player in the direction they are facing
        Location displaySpawnLocation = eyeLocation.add(direction.multiply(2));

        return displaySpawnLocation;
    }

}
