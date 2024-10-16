package me.diu.gachafight.utils;

import org.bukkit.Location;

public class DungeonUtils {
    public static String getDungeonName(Location location) {
        if (isGoblin(location)) {
            return "Goblin";
        } else if (isRPG(location)) {
            return "RPG";
        } else if (isSafezone(location)){
            return "Safezone";
        } else {
            return null;
        }
    }

    public static boolean isGoblin(Location location) {
        //MinX, MaxX, MinZ, MaxZ
        return location.getX() > -807 && location.getX() < -600 && location.getZ() > 264 && location.getZ() < 471;
    }

    // Check if the chest location is one of the designated RPG Loot Chest locations
    public static boolean isRPG(Location location) {
        //MinX, MaxX, MinZ, MaxZ
        return location.getX() > -969 && location.getX() < -838 && location.getZ() > -263 && location.getZ() < 469;
    }
    public static boolean isSafezone(Location location) {
        // ===================Spawn==================
        if (location.getX() > -259 && location.getX() < 220 && location.getZ() >-419 && location.getZ() < 502) {
            return true;
        }
        // =================Tutorial================
        if (location.getX() > -766 && location.getX() < -597 && location.getZ() > 30 && location.getZ() < 101) {
            return true;
        }
        // ===================AFK====================
        if (location.getX() > -12 && location.getX() < -3 && location.getZ() > 326 && location.getZ() < 335) {
            return true;
        }
        return false;
    }

}
