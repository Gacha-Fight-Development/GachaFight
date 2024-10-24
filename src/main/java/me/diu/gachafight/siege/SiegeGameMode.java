package me.diu.gachafight.siege;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.hooks.VaultHook;
import me.diu.gachafight.party.PartyManager;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.siege.utils.TeleportUtils;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.DungeonUtils;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class SiegeGameMode {
    private final GachaFight plugin;

    private Player player;
    private Arena arena;
    private static int wave;
    private static int SIEGE_MONSTERS_PER_WAVE;
    public static Map<UUID, List<UUID>> activeMonsters = new HashMap<>();
    public static Map<UUID, Map<Integer, Integer>> playerWave = new HashMap<>();

    public SiegeGameMode(GachaFight plugin, Player player, Arena arena, int wave) {
        this.plugin = plugin;
        this.player = player;
        this.arena = arena;
        this.wave = wave;
    }

    public void start() {
        // Teleport player to the arena
        arena.start(player);
        ArenaGateListener.playerUsedKey.remove(player.getUniqueId());
        Map<Integer, Integer> waveMap = new HashMap<>();
        waveMap.put(arena.getArenaId(), wave);
        playerWave.put(player.getUniqueId(), waveMap);
        // Start spawning monsters for each wave
        spawnMonstersForWave();

        // Send a message to the player to start the game
        player.sendMessage(ColorChat.chat("&aSiege game started! Kill all monsters in the arena to progress."));
    }

    // ...

    private void rewardPlayer() {
        // Reward the player for completing all waves
        player.sendMessage(ColorChat.chat("&aCongratulations, you have completed the siege game!"));
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        playerWave.remove(player.getUniqueId());
        activeMonsters.remove(player.getUniqueId());

        // Give the player gold and XP

        // Reward party members if they are at the arena
        if (PartyManager.isInParty(player)) {
            Set<OfflinePlayer> partyMembers = PartyManager.getPartyMembers(PartyManager.getPartyLeader(player));
            for (Player member : partyMembers.stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).collect(Collectors.toSet())) {
                if (arena.getLocation().getNearbyEntities(64, 64, 64).stream().anyMatch(entity -> entity instanceof Player)) {
                    Player onlineMember = member.getPlayer();
                    VaultHook.deposit(onlineMember, arena.getRewardGold());
                    PlayerStats memberstats = PlayerStats.getPlayerStats(onlineMember);
                    memberstats.addExpWithMulti(arena.getRewardXp(), onlineMember);
                    if (!onlineMember.hasPermission(arena.getPermissionReward())) {
                        User user = LuckPermsProvider.get().getUserManager().getUser(onlineMember.getUniqueId());
                        user.data().add(PermissionNode.builder(arena.getPermissionReward()).build());
                        LuckPermsProvider.get().getUserManager().saveUser(user);
                    }
                    member.getPlayer().sendMessage(ColorChat.chat("&aYou have been rewarded for completing the siege game with your party!"));

                }
            }
        } else {
            VaultHook.addMoneyWithMulti(player, arena.getRewardGold());
            stats.addExpWithMulti(arena.getRewardXp(), player);
            if (!player.hasPermission(arena.getPermissionReward())) {
                User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
                user.data().add(PermissionNode.builder(arena.getPermissionReward()).build());
                LuckPermsProvider.get().getUserManager().saveUser(user);
            }
        }
        activeMonsters.remove(player.getUniqueId());
        // Give player 5 seconds before leaving the arena
        player.sendMessage(ColorChat.chat("&aYou will be leaving the arena in 10 seconds..."));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Leave the arena
            arena.leave(player);
        }, 200); // 100 ticks = 5 seconds
    }
    private void spawnMonstersForWave() {
        // Spawn monsters for this wave
        int monstersSpawned = 0;
        List<UUID> listmob = new ArrayList<>();
        SIEGE_MONSTERS_PER_WAVE = (int) Math.ceil(10 + Math.pow(wave, 1.9));
        while (monstersSpawned < SIEGE_MONSTERS_PER_WAVE) {
            for (SiegeMob siegeMob : arena.getSiegeMobs()) {
                if (wave >= siegeMob.getWave()) {
                    // Spawn a monster
                    Location spawnLocation = arena.getLocation().clone().add((Math.random()-0.5) * 60, 0, (Math.random()-0.5) * 60);
                    spawnLocation = getSafeSpawnLocation(spawnLocation);
                    AbstractLocation location = BukkitAdapter.adapt(spawnLocation);
                    MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob(siegeMob.getMobId()).orElse(null);
                    ActiveMob activeMob = mob.spawn(location, 1);
                    for (Player player : Objects.requireNonNull(Bukkit.getWorld("Spawn")).getNearbyEntitiesByType(Player.class, BukkitAdapter.adapt(activeMob.getLocation()), 64, 1)) {
                        activeMob.setTarget(BukkitAdapter.adapt(player));
                        break;
                    }
                    listmob.add(activeMob.getUniqueId());
                    monstersSpawned++;
                    if (monstersSpawned >= SIEGE_MONSTERS_PER_WAVE) {
                        activeMonsters.put(player.getUniqueId(), listmob);
                        break;
                    }
                }
            }
        }

        // Wait for player to kill all monsters in this wave
        Bukkit.getScheduler().runTaskLater(plugin, this::checkIfWaveIsComplete, 20);
        // Store the task ID and wave number
    }

    private Location getSafeSpawnLocation(Location spawnLocation) {
        int solidBlocks = 0;

        // Check the surrounding blocks for solid blocks
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 || z == 0) { // only check blocks directly adjacent to the spawn location
                    Location blockLocation = spawnLocation.clone().add(x, 0, z);
                    if (blockLocation.getBlock().getType().isSolid()) {
                        solidBlocks++;
                    }
                }
            }
        }

        // If there are 2 or more solid blocks surrounding the spawn location, try to find a new spawn location
        if (solidBlocks >= 2) {
            // Try to find a new spawn location within the arena's radius
            Location arenaCenter = arena.getLocation();
            int arenaRadius = 30; // adjust this value to match the radius of your arena
            for (int x = -arenaRadius; x <= arenaRadius; x++) {
                for (int z = -arenaRadius; z <= arenaRadius; z++) {
                    Location newLocation = arenaCenter.clone().add(x, 0, z);
                    int newSolidBlocks = 0;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            Location newBlockLocation = newLocation.clone().add(dx, 0, dz);
                            if (newBlockLocation.getBlock().getType().isSolid()) {
                                newSolidBlocks++;
                            }
                        }
                    }
                    if (newSolidBlocks < 2) {
                        return newLocation;
                    }
                }
            }
        }
        return spawnLocation;
    }

    public void checkIfWaveIsComplete() {
        Iterator<UUID> iterator = activeMonsters.get(player.getUniqueId()).iterator();
        while (iterator.hasNext()) {
            UUID activeMobUUID = iterator.next();
            ActiveMob activeMob = MythicBukkit.inst().getMobManager().getActiveMob(activeMobUUID).orElse(null);
            if (activeMob == null) {
                iterator.remove();
                continue;
            }
            if (activeMob.isDead()) {
                iterator.remove();
                continue;
            }
            for (Player player : Objects.requireNonNull(Bukkit.getWorld("Spawn")).getNearbyEntitiesByType(Player.class, BukkitAdapter.adapt(activeMob.getLocation()), 64, 1)) {
                activeMob.setTarget(BukkitAdapter.adapt(player));
                break;
            }
        }
        if (activeMonsters.get(player.getUniqueId()).size() == 0) {
            if (wave >= arena.getWaves()) {
                // Reward player for completing all waves
                rewardPlayer();
                return;
            }
            // Increment wave counter
            wave++;
            SIEGE_MONSTERS_PER_WAVE = (int) Math.ceil(10 + Math.pow(wave, 1.7));
            Map<Integer, Integer> arenaWaves = SiegeGameMode.playerWave.getOrDefault(player.getUniqueId(), new HashMap<>());
            arenaWaves.put(arena.getArenaId(), wave);
            SiegeGameMode.playerWave.put(player.getUniqueId(), arenaWaves);

            // Check if player is still in the arena
            if (!arena.getLocation().getNearbyEntities(64, 64, 64).stream().anyMatch(entity -> entity.getUniqueId().equals(player.getUniqueId()))) {
                // If player is not in the arena, check if any party members are in the arena
                if (!PartyManager.isInParty(player)) {
                    // If player is not in a party, inform them that the siege failed
                    player.sendMessage(ColorChat.chat("&cSiege failed!"));
                    List<UUID> mobs = activeMonsters.get(player.getUniqueId());
                    for (UUID mobUUID : mobs) {
                        ActiveMob activeMob = MythicBukkit.inst().getMobManager().getActiveMob(mobUUID).orElse(null);
                        activeMob.remove();
                    }
                    activeMonsters.remove(player.getUniqueId());
                    playerWave.remove(player.getUniqueId());
                    return;
                } else {
                    // If player is in a party, check if any party members are in the arena
                    Set<OfflinePlayer> partyMembers = PartyManager.getPartyMembers(PartyManager.getPartyLeader(player));
                    if (!partyMembers.stream().anyMatch(member -> member.isOnline() && arena.getLocation().getNearbyEntities(64, 64, 64).stream().anyMatch(entity -> entity.getUniqueId().equals(member.getUniqueId())))) {
                        // If no party members are in the arena, inform them that the siege failed
                        player.sendMessage(ColorChat.chat("&cSiege failed!"));
                        List<UUID> mobs = activeMonsters.get(player.getUniqueId());
                        for (UUID mobUUID : mobs) {
                            ActiveMob activeMob = MythicBukkit.inst().getMobManager().getActiveMob(mobUUID).orElse(null);
                            activeMob.remove();
                        }
                        activeMonsters.remove(player.getUniqueId());
                        return;
                    }
                }
            }

            // Give player 10 seconds of recovery time
            OfflinePlayer partyLeader = PartyManager.getPartyLeader(player);
            if (partyLeader != null) {
                Set<OfflinePlayer> partyMembers = PartyManager.getPartyMembers(partyLeader);
                for (Player member : partyMembers.stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).collect(Collectors.toSet())) {
                    member.sendMessage(ColorChat.chat("&aWave " + (wave - 1) + " complete! You have 10 seconds to recover before the next wave."));
                }
            } else {
                player.sendMessage(ColorChat.chat("&aWave " + (wave - 1) + " complete! You have 10 seconds to recover before the next wave."));
            }
            // Spawn monsters for next wave
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, this::spawnMonstersForWave, 200); // 200 ticks = 10 seconds
        } else {
            // Schedule another check
            Bukkit.getScheduler().runTaskLater(plugin, this::checkIfWaveIsComplete, 20);
        }
    }
    public static void resumeScheduledTasks(Player player) {
        if (activeMonsters.containsKey(player.getUniqueId()) && playerWave.containsKey(player.getUniqueId())) {
            Map<Integer, Integer> arenaWaves = SiegeGameMode.playerWave.get(player.getUniqueId());
            SiegeGameMode siege;
            for (Integer arenaId : arenaWaves.keySet()) {
                siege = new SiegeGameMode(GachaFight.getInstance(), player, getArena(arenaId), arenaWaves.get(arenaId));
                siege.checkIfWaveIsComplete();
            }
        }
    }
    public static Arena getArena(Integer id) {
        if (id == 1) {
            return new Arena1();
        }
        else {
            return null;
        }
    }
}