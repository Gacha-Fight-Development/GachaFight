package me.diu.gachafight.siege;

import me.diu.gachafight.party.PartyManager;
import me.diu.gachafight.utils.DungeonUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Arena1 extends Arena {
    public Arena1() {
        super(1, new Location(Bukkit.getWorld("Spawn"), -788.5, 5, -247.5), 60, List.of(
                new SiegeMob("rpg_slime_cube", 1),
                new SiegeMob("rpg_mushroom", 1),
                new SiegeMob("rpg_mushroom_red", 2),
                new SiegeMob("rpg_poison_slime_cube", 2),
                new SiegeMob("rpg_rat", 3),
                new SiegeMob("rpg_bat", 4),
                new SiegeMob("rpg_zombie", 5),
                new SiegeMob("rpg_skeleton", 7),
                new SiegeMob("rpg_rat_undead", 6),
                new SiegeMob("rpg_skeleton_crossbow", 8),
                new SiegeMob("rpg_sand_golem", 10),
                new SiegeMob("rpg_stone_golem", 10)
        ), 10, 800, 200, "gacha.dungeons.1");
    }

    @Override
    public void start(OfflinePlayer player) {
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) {
            return;
        }
        if (!PartyManager.isInParty(player)) {
            onlinePlayer.teleport(getLocation());
            return;
        }
        if (PartyManager.getPartyLeader(player) == null) {
            onlinePlayer.teleport(getLocation());
            return;
        }
        // Teleport party to the arena
        Set<OfflinePlayer> partyMembers = PartyManager.getPartyMembers(PartyManager.getPartyLeader(player));
        for (Player member : partyMembers.stream().filter(member -> member.isOnline()).map(member -> member.getPlayer()).collect(Collectors.toSet())) {
            if (DungeonUtils.isSafezone(member.getLocation())) {
                member.teleport(getLocation());
            }
        }
        System.out.println("Starting arena 1");
        // Start the siege
        // ...
    }

    @Override
    public void leave(OfflinePlayer player) {
        if (player == null) {
            return;
        }
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) {
            return;
        }
        if (!PartyManager.isInParty(player)) {
            DungeonUtils.teleportPlayerToSpawn(onlinePlayer);
        }
        if (PartyManager.getPartyLeader(player) == null) {
            DungeonUtils.teleportPlayerToSpawn(onlinePlayer);
        }
        // Teleport party to the arena
        Set<OfflinePlayer> partyMembers = PartyManager.getPartyMembers(Objects.requireNonNull(PartyManager.getPartyLeader(player)));
        for (Player member : partyMembers.stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).collect(Collectors.toSet())) {
            if (DungeonUtils.isArena(member.getLocation())) {
                DungeonUtils.teleportPlayerToSpawn(member);
            }
        }
        // End the siege
        // ...
    }
}