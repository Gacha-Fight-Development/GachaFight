package me.diu.gachafight.utils;


public class Calculations {

    public static double playerLevelMultiplier(int level) {
        return 1 + (level * 0.06) - 0.06;
    }
    public static double healerCost(double hp) { return (hp*0.2) - 2; }
    public static double overseerHPCost(double hp) { return 10 + Math.pow(hp*0.6, 1+0.95); }
    public static double overseerDamageCost(double dmg) { return 50 + Math.pow(dmg*3.5, 1+1.1); }
    public static double overseerArmorCost(double armor) { return 50 + Math.pow(armor*2.3, 1+1.1); }
    public static double overseerCritChanceCost(double critChance) { return 20 + (10*(critChance*80)) + Math.pow(critChance*380, 1+1.7); }
    public static double overseerCritDmgCost(double critDmg) { return 20 + (2500*(critDmg-1.5)) +Math.pow(critDmg*12, 1+1.65) -2000; }
    public static double overseerSpeedCost(double speed) { return 100 + Math.pow(speed*15, 1+2.5) -12750; }
    public static double overseerDodgeCost(double dodge) { return 50 + Math.pow(dodge*550, 1+2.2) -100; }
}
