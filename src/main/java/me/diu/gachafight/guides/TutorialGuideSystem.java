package me.diu.gachafight.guides;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.commands.GuideCommand;
import me.diu.gachafight.utils.TutorialBossBar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TutorialGuideSystem implements Listener {

    private final GachaFight plugin;
    public static final Map<UUID, Integer> guidingTasks = new HashMap<>();  // Store scheduler task IDs per player
    public static final Map<UUID, ItemDisplay> guidingDisplays = new HashMap<>();  // Store ItemDisplays per player

    public TutorialGuideSystem(GachaFight plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin); // Register listener for player quit
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
            ItemStack arrow = new ItemStack(Material.PAPER);
            ItemMeta meta = arrow.getItemMeta();
            meta.setCustomModelData(10101);
            arrow.setItemMeta(meta);
            display.setItemStack(arrow); // Set item to arrow
            display.setVisibleByDefault(false);                  // Hide from other players
        });

        // Show the ItemDisplay to the player only
        player.showEntity(plugin, itemDisplay);

        // Store the ItemDisplay for future reference
        guidingDisplays.put(player.getUniqueId(), itemDisplay);

        // Task to move the arrow towards the destination
        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!guidingDisplays.containsKey(player.getUniqueId())) return;

            // Calculate distance from player to the destination
            Location playerLocation = player.getLocation();
            double distanceToTarget = playerLocation.distance(destination);

            if (distanceToTarget <= 2.3) {
                // If the player is close to the destination, remove the display
                removeGuideForPlayer(player);
                player.sendMessage("You have reached your destination!");
                // ===================TUTORIAL=====================
                if (player.hasPermission("gacha.tutorial.2") && !player.hasPermission("op")) {
                    TutorialBossBar.showFinalTutorialBossBar(player);
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission unset gacha.tutorial.2");
                    new BukkitRunnable() {
                        public void run() {
                            plugin.getGuideSystem().guidePlayerToLocation(player, GuideCommand.preSetLocations.get("dungeon"));
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        }
                    }.runTaskLater(plugin, 100*3);
                }
            } else {
                // Move the display closer to the destination at a slower speed
                Location currentDisplayLocation = itemDisplay.getLocation();
                Vector directionToTarget = destination.toVector().subtract(currentDisplayLocation.toVector()).normalize();

                // Slower movement towards the target (multiply by a smaller factor)
                Location newLocation = currentDisplayLocation.clone().add(directionToTarget.multiply(0.7)); // Slower movement
                itemDisplay.teleport(newLocation);
            }
        }, 0L, 3L).getTaskId(); // Run every 4 ticks (slower movement)

        // Store task ID to later cancel it
        guidingTasks.put(player.getUniqueId(), taskId);

        // Task to reset the arrow's location to the player's position every 4 seconds
        int resetTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!guidingDisplays.containsKey(player.getUniqueId())) return;

            // Reset the arrow back to the player's location every 4 seconds
            Location newSpawnLocation = getDisplaySpawnLocation(player);
            itemDisplay.teleport(newSpawnLocation);
        }, 80L, 80L).getTaskId(); // 80 ticks = 4 seconds

        // Store the reset task ID
        guidingTasks.put(player.getUniqueId(), resetTaskId);
    }

    /**
     * Removes the guiding ItemDisplay for a specific player and cancels the tasks.
     *
     * @param player The player whose guide will be removed.
     */
    public void removeGuideForPlayer(Player player) {
        ItemDisplay display = guidingDisplays.get(player.getUniqueId());
        guidingDisplays.remove(player.getUniqueId());
        if (display != null) {
            display.remove(); // Remove the display from the world
        }

        // Cancel the scheduled tasks for the player
        Integer taskId = guidingTasks.get(player.getUniqueId());
        guidingTasks.remove(player.getUniqueId());
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
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

    /**
     * Cleans up all tasks and displays for all players (used when the plugin disables).
     */
    public void cleanupAll() {
        // Stop guiding for all players and clean up displays
        for (UUID playerId : guidingDisplays.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                removeGuideForPlayer(player);
            }
        }
    }
}
