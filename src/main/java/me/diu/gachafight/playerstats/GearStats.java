package me.diu.gachafight.playerstats;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GearStats {

    // Separate stats for each gear piece
    private ArmorStats helmetStats;
    private ArmorStats chestplateStats;
    private ArmorStats leggingsStats;
    private ArmorStats bootsStats;
    private ArmorStats offhandStats; // New Offhand Stats

    public GearStats() {
        this.helmetStats = new ArmorStats();
        this.chestplateStats = new ArmorStats();
        this.leggingsStats = new ArmorStats();
        this.bootsStats = new ArmorStats();
        this.offhandStats = new ArmorStats(); // Initialize Offhand Stats
    }

    // Reset all gear stats
    public void resetStats() {
        this.helmetStats.resetStats();
        this.chestplateStats.resetStats();
        this.leggingsStats.resetStats();
        this.bootsStats.resetStats();
        this.offhandStats.resetStats(); // Reset Offhand Stats
    }

    // Total damage from all gear
    public double getTotalDamage() {
        return helmetStats.getDamage() + chestplateStats.getDamage() + leggingsStats.getDamage() +
                bootsStats.getDamage() + offhandStats.getDamage(); // Include Offhand Stats
    }

    // Total armor from all gear
    public double getTotalArmor() {
        return helmetStats.getArmor() + chestplateStats.getArmor() + leggingsStats.getArmor() +
                bootsStats.getArmor() + offhandStats.getArmor(); // Include Offhand Stats
    }

    // Total crit from all gear
    public double getTotalCrit() {
        return helmetStats.getCrit() + chestplateStats.getCrit() + leggingsStats.getCrit() +
                bootsStats.getCrit() + offhandStats.getCrit(); // Include Offhand Stats
    }

    // Total max HP from all gear
    public double getTotalMaxHp() {
        return helmetStats.getMaxHp() + chestplateStats.getMaxHp() + leggingsStats.getMaxHp() +
                bootsStats.getMaxHp() + offhandStats.getMaxHp(); // Include Offhand Stats
    }

    public double getTotalLuck() {
        return helmetStats.getLuck() + chestplateStats.getLuck() + leggingsStats.getLuck() +
                bootsStats.getLuck() + offhandStats.getLuck(); // Include Offhand Stats
    }
}
