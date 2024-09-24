package me.diu.gachafight.utils;

import me.diu.gachafight.playerstats.PlayerStats;

public class Calculations {

    public static double playerLevelMultiplier(int level) {
        return 1 + (level * 0.05) - 0.05;
    }
}
