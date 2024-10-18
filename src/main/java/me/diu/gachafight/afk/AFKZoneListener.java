package me.diu.gachafight.afk;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.GiveItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class AFKZoneListener implements Listener {

    private static final Location afkDummyLoc = new Location(Bukkit.getWorld("Spawn"), -718, 4, -65);
    public static ActiveMob afkDummy;

    // AFK zone
    private final int minX = -772, maxX = -680;
    private final int minY = 0, maxY = 46;
    private final int minZ = -104, maxZ = -24;

    // Track players who are in the zone and their reward tasks
    public static final HashMap<UUID, BukkitRunnable> playerTasks = new HashMap<>();

    public AFKZoneListener(GachaFight plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        initializeAFKDummy();
    }

    public static void initializeAFKDummy() {
        Chunk chunk = afkDummyLoc.getChunk();
        if (!chunk.isLoaded()) {
            chunk.load();
            spawnAFKDummy(chunk);
        } else {
            spawnAFKDummy(chunk);
        }
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
        if (AFKManager.isInAFKZone(loc)) {
            // If player is in the zone and not already being rewarded, start rewarding
            if (!AFKManager.afkTasks.containsKey(player.getUniqueId())) {
                player.sendMessage(ColorChat.chat("&aEntered AFK Zone."));
                AFKManager.startAFKSession(player);
            }
        } else {
            // If player leaves the zone, stop rewarding
            AFKManager.stopAFKSession(player);
        }
    }

    // Method to check if the player is within the defined cuboid zone

    private boolean hasPlayerMovedAtLeastOneBlock(Location from, Location to) {
        // Compare block coordinates to ensure it's a full block movement
        return from.getBlockX() != to.getBlockX() ||
                from.getBlockY() != to.getBlockY() ||
                from.getBlockZ() != to.getBlockZ();
    }
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // Check if the loaded chunk is the one where the AFK Dummy should be
        if (event.getChunk().getX() == -772 >> -680 && event.getChunk().getZ() == -104 >> -24) {
            if (afkDummy == null || afkDummy.getEntity().isDead()) {
                if (!afkDummyLoc.isChunkLoaded()) {
                    afkDummyLoc.getChunk().load();
                }
                spawnAFKDummy(event.getChunk());
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (event.getChunk().getX() == -772 >> -680 && event.getChunk().getZ() == -104 >> -24) {
            removeAFKDummy();
        }
    }
    private void removeAFKDummy() {
        if (afkDummy != null) {
            afkDummy.remove();
            afkDummy = null;
            Bukkit.getLogger().info("AFK Dummy removed.");
        }
    }

    private static void spawnAFKDummy(Chunk chunk) {

        // Ensure the chunk is loaded
        if (!chunk.isLoaded()) {
            chunk.load();
        }

        MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob("AFKDummy").orElse(null);
        if (mob != null) {
            // spawns mob
            afkDummy = mob.spawn(BukkitAdapter.adapt(afkDummyLoc), 1);

            // get mob as bukkit entity
            Entity entity = afkDummy.getEntity().getBukkitEntity();
            entity.setRotation(90, 0);
        }

        if (mob != null) {
            Bukkit.getLogger().info("AFK Dummy spawned successfully.");
        } else {
            Bukkit.getLogger().warning("Failed to spawn AFK Dummy. MythicMob 'AFKDummy' not found.");
        }
    }
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (AFKManager.afkTasks.containsKey(player.getUniqueId())) {
            AFKManager.updateAFKSwordItem(player);
        }
    }
}