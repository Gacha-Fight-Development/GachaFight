package me.diu.gachafight.utils;


public class Calculations {

    public static double playerLevelMultiplier(int level) {
        return 1 + (level * 0.05) - 0.05;
    }
    public static double healerCost(double hp) { return (hp*0.1) - 0.8; }
    public static double overseerHPCost(double hp) { return 10 + Math.pow(hp*0.6, 1+0.8); }
    public static double overseerDamageCost(double dmg) { return 50 + Math.pow(dmg*3.5, 1+1.8); }
    public static double overseerArmorCost(double armor) { return 50 + Math.pow(armor*2, 1+1.8); }
    public static double overseerCritChanceCost(double critChance) { return 10 + Math.pow(critChance*500, 1+1.05); }
    public static double overseerCritDmgCost(double critDmg) { return 10 + Math.pow(critDmg*2, 1+1.05); }
    public static double overseerSpeedCost(double speed) { return (1000*((speed-1)*10)) + Math.pow(speed*5, 1+2.5); }
    public static double overseerDodgeCost(double dodge) { return 50 + Math.pow(dodge*2.5, 1+1.05); }
}
