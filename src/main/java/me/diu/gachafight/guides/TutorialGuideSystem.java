package me.diu.gachafight.guides;

import me.diu.gachafight.GachaFight;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class TutorialGuideSystem {

    private final GachaFight plugin;
    private final HashMap<UUID, SpectralArrow> guidingArrows = new HashMap<>();

    public TutorialGuideSystem(GachaFight plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts guiding a player to a location using a spectral arrow.
     *
     * @param player The player who will be guided.
     * @param destination The target location to guide the player to.
     */
    public void guidePlayerToLocation(Player player, Location destination) {
        // Spawn the Spectral Arrow as a guide at the player's location
        Location playerLocation = player.getLocation();
        SpectralArrow arrow = player.getWorld().spawn(playerLocation, SpectralArrow.class, (spectralArrow) -> {
            spectralArrow.setVisibleByDefault(false); // Hide the arrow from everyone
            spectralArrow.setGravity(false);          // Arrow will hover and stay at its location
        }); //-3 101 202

        // Show the arrow to the player only
        player.showEntity(plugin, arrow);

        // Store the arrow for future reference
        guidingArrows.put(player.getUniqueId(), arrow);

        // Start a task to move the arrow towards the destination and guide the player
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!guidingArrows.containsKey(player.getUniqueId())) return;

            // Calculate direction from the arrow's current location to the destination
            Location currentArrowLocation = arrow.getLocation();
            double distanceToTarget = currentArrowLocation.distance(destination);

            if (distanceToTarget <= 1) {
                // If the player is close to the destination, remove the arrow
                removeGuideForPlayer(player);
                player.sendMessage("You have reached your destination!");
            } else {
                // Move the arrow slightly closer to the destination each tick
                Location newLocation = currentArrowLocation.clone().add(
                        destination.toVector().subtract(currentArrowLocation.toVector()).normalize().multiply(0.5)
                );
                arrow.teleport(newLocation);
            }
        }, 0L, 2L); // Run every 2 ticks (10 times per second)
    }

    /**
     * Removes the guiding arrow for a specific player.
     *
     * @param player The player whose guide will be removed.
     */
    public void removeGuideForPlayer(Player player) {
        SpectralArrow arrow = guidingArrows.remove(player.getUniqueId());
        if (arrow != null) {
            arrow.remove(); // Remove the arrow from the world
        }
    }
}
