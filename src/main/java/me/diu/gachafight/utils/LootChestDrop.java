package me.diu.gachafight.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LootChestDrop {
    public static void dropCommonKey(Location location, double chance, int amount) {
        if (Math.random() < chance) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 991 " +
                    (int) (Math.floor(Math.random() * amount) + 1) + " Spawn " + location.getX() + " " +
                    location.getY() + 2 + " " + location.getZ());
        }
    }
    public static void dropUncommonKey(Location location, double chance, int amount) {
        if (Math.random() < chance) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 992 " +
                    (int) (Math.floor(Math.random() * amount) + 1) + " Spawn " + location.getX() + " " +
                    location.getY() + 2 + " " + location.getZ());
        }
    }
    public static void dropRareKey(Location location, double chance, int amount) {
        if (Math.random() < chance) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 993 " +
                    (int) (Math.floor(Math.random() * amount) + 1) + " Spawn " + location.getX() + " " +
                    location.getY() + 2 + " " + location.getZ());
        }
    }
    public static void dropSmallHpPot(Location location, double chance, int amount) {
        if (Math.random() < chance) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 901 " +
                    (int) (Math.floor(Math.random() * amount) + 1) + " Spawn " + location.getX() + " " +
                    location.getY() + 2 + " " + location.getZ());
        }
    }
    public static void dropGold(Location location, double chance, int amount) {
        if (Math.random() < chance) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si drop 611 " +
                    (int) (Math.floor(Math.random() * amount) + 1) + " Spawn " + location.getX() + " " +
                    location.getY() + 2 + " " + location.getZ());
        }
    }
}
