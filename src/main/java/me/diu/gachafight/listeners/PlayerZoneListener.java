package me.diu.gachafight.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.GiveItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class PlayerZoneListener implements Listener {

    // AFK zone
    private final int minX = -12, maxX = -3;
    private final int minY = 176, maxY = 180;
    private final int minZ = 326, maxZ = 335;

    // Track players who are in the zone and their reward tasks
    public static final HashMap<UUID, BukkitRunnable> playerTasks = new HashMap<>();

    public PlayerZoneListener(GachaFight plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (!hasPlayerMovedAtLeastOneBlock(from, to)) {
            return;
        }
        // Check if the player is within the zone
        if (isInAFKZone(loc)) {
            // If player is in the zone and not already being rewarded, start rewarding
            if (!playerTasks.containsKey(player.getUniqueId())) {
                player.sendMessage(ColorChat.chat("&aEntered AFK Zone."));
                player.sendMessage(ColorChat.chat("&aYour next reward in 1 minute."));
                startRewardingPlayer(player);
            }
        } else {
            // If player leaves the zone, stop rewarding
            if (playerTasks.containsKey(player.getUniqueId())) {
                stopRewardingPlayer(player);
            }
        }
    }

    // Method to check if the player is within the defined cuboid zone
    private boolean isInAFKZone(Location loc) {
        return loc.getBlockX() >= minX && loc.getBlockX() <= maxX &&
                loc.getBlockY() >= minY && loc.getBlockY() <= maxY &&
                loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ;
    }

    // Method to start rewarding the player every minute
    public static void startRewardingPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        BukkitRunnable rewardTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Give reward to the player
                player.sendMessage(ColorChat.chat("&aAFK Zone, You Have been Rewarded:"));
                // You can add your custom reward logic here
                giveAFKReward(player);
                player.sendMessage(ColorChat.chat("&aNext Reward in 1 Minute"));
            }
        };

        // Run the task every 1 minute (1200 ticks = 1 minute)
        rewardTask.runTaskTimer(GachaFight.getInstance(), 1200L, 1200L);

        // Store the task in the map
        playerTasks.put(playerId, rewardTask);
    }

    // Method to stop rewarding the player when they leave the zone
    private void stopRewardingPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        if (playerTasks.containsKey(playerId)) {
            // Cancel the scheduled task
            playerTasks.get(playerId).cancel();
            playerTasks.remove(playerId);

            // Notify the player
            player.sendMessage("You have left the zone, rewards stopped.");
        }
    }

    // Custom reward method (can be modified as per your needs)
    public static void giveAFKReward(Player player) {
        if (Math.random() < 0.8) {
            GiveItemUtils.giveCommonKey(player, 1);
        }
        if (Math.random() < 0.05) {
            GiveItemUtils.giveUncommonKey(player, 1);
        }
        GiveItemUtils.giveGold(player, (int) (Math.random() * 4)+1);
    }

    private boolean hasPlayerMovedAtLeastOneBlock(Location from, Location to) {
        // Compare block coordinates to ensure it's a full block movement
        return from.getBlockX() != to.getBlockX() ||
                from.getBlockY() != to.getBlockY() ||
                from.getBlockZ() != to.getBlockZ();
    }
}