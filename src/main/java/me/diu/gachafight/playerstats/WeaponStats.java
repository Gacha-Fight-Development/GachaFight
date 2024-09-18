package me.diu.gachafight.playerstats;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeaponStats {
    private double damage;
    private double armor;
    private double crit;
    private double maxHp;
    private int luck;

    public WeaponStats() {
        this.damage = 0;
        this.armor = 0;
        this.crit = 0;
        this.maxHp = 0;
        this.luck = 0;
    }

    public void resetStats() {
        this.damage = 0;
        this.armor = 0;
        this.crit = 0;
        this.maxHp = 0;
        this.luck = 0;
    }
}
