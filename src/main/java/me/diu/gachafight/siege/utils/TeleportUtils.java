package me.diu.gachafight.siege.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportUtils {
    public static void teleportPlayerToLocation(Player player, Location location) {
        player.teleport(location);
    }

}
