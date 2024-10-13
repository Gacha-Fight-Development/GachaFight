package me.diu.gachafight.dungeon.utils;

import org.bukkit.Location;

public class DungeonUtils {

    public static boolean isGoblin(Location location) {
        //MinX, MaxX, MinZ, MaxZ
        return location.getX() > -807 && location.getX() < -600 && location.getZ() > 264 && location.getZ() < 471;
    }

    // Check if the chest location is one of the designated RPG Loot Chest locations
    public static boolean isRPG(Location location) {
        //MinX, MaxX, MinZ, MaxZ
        return location.getX() > -969 && location.getX() < -838 && location.getZ() > -263 && location.getZ() < 469;
    }
}
