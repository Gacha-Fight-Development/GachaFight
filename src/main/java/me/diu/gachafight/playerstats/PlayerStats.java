package me.diu.gachafight.playerstats;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class PlayerStats {
    public static final Map<UUID, PlayerStats> playerStatsMap = new HashMap<>();
    private UUID playerUUID;
    private int level;
    private double exp;
    private double damage;
    private double armor;
    private double maxhp;
    private double hp;
    private double crit;
    private double speed;
    private int luck;
    private double money;
    private int gem;

    private GearStats gearStats;
    private WeaponStats weaponStats;
    public PlayerStats(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.level = 1;
        this.exp = 0.0;
        this.damage = 1;
        this.maxhp = 20;
        this.hp = this.getMaxhp();
        this.armor = 0.2;
        this.crit = 1;
//        this.intelligence = 10;
//        this.wisdom = 10;
        this.luck = 5;
        this.money = 0;
        this.gem = 0;
        this.speed = 1;
        this.gearStats = new GearStats();
        this.weaponStats = new WeaponStats();
    }

    public static PlayerStats getPlayerStats(Player player) {
        return playerStatsMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats(player.getUniqueId()));
    }

    public void addStat(String stat, Player player) {
        if (player.isOnline()) {
            if (stat.equalsIgnoreCase("damage")) {
                this.damage++;
            } else if (stat.equalsIgnoreCase("hp")) {
                this.maxhp++;
            } else if (stat.equalsIgnoreCase("armor")) {
                this.armor++;
            } else if (stat.equalsIgnoreCase("level")) {
                this.level++;
            } else if (stat.equalsIgnoreCase("luck")) {
                this.luck++;
//            } else if (stat.equalsIgnoreCase("wisdom")) {
//                this.wisdom++;
            }
        }
    }

    public String showStats(Player player) {
        return ColorChat.chat("&aStats:\n" +
                "&eLevel: " + getPlayerStats(player).getLevel() + "\n" +
                "&eExp: " + String.format("%.2f", getPlayerStats(player).getExp()) + "/" + getPlayerStats(player).getRequiredExp() + "\n" +
                "&eStrength: " + String.format("%.1f", getPlayerStats(player).getDamage()) + " (+" + String.format("%.1f", getPlayerStats(player).getWeaponStats().getDamage()) + ")\n" +
                "&eArmor: " + String.format("%.1f", getPlayerStats(player).getArmor()) + " (+" + String.format("%.1f", getPlayerStats(player).getGearStats().getTotalArmor()) + ")\n" +
                "&eHP: " + getPlayerStats(player).getMaxhp() + " (+" + String.format("%.1f", getPlayerStats(player).getGearStats().getTotalMaxHp()) + ")\n" +
                "&eOffhand Stats: " + getPlayerStats(player).getGearStats().getOffhandStats().getDamage() + " damage, " +
                getPlayerStats(player).getGearStats().getOffhandStats().getArmor() + " armor\n" +
                "&eSpeed: " + getPlayerStats(player).getSpeed() + "\n" +
                "&eLuck: " + getPlayerStats(player).getLuck() + "\n");
    }

    public void addExp(double amount, Player player) {
        this.exp += amount;
        syncPlayerLevelWithMinecraft(player);
        syncExpBarWithMinecraft(player);  // Sync experience bar progress

        if (this.exp >= getRequiredExp()) {
            this.exp -= getRequiredExp();
            this.level++;
            syncPlayerLevelWithMinecraft(player);  // Sync Minecraft level
        }
    }

    public int getRequiredExp() {
        return (int) (Math.pow(this.level, 2.88) + 25 * this.level);
    }

    // Added a new method to sync player's health with their hearts
    public void syncHealthWithHearts(Player player) {
        double currentHp = Math.min(this.hp + this.getGearStats().getTotalMaxHp(), this.maxhp+ this.getGearStats().getTotalMaxHp());
        if (currentHp < 0) {
        } else {
            player.setHealth(currentHp / (this.maxhp+this.getGearStats().getTotalMaxHp()) * 20); // Scale to Minecraft's health system (20 half hearts)
        }
    }


    // Updated method to update the player's actionbar with their current health status
    public void updateActionbar(Player player) {
        String actionbarText = ColorChat.chat("&cHP: " + (int) (this.hp + this.getGearStats().getTotalMaxHp())  + "/" + (int) (this.maxhp + this.getGearStats().getTotalMaxHp()));
        player.sendActionBar(actionbarText);
    }

    // Sync player's Minecraft level with custom level
    public void syncPlayerLevelWithMinecraft(Player player) {
        player.setLevel(this.level);  // Set Minecraft level to match PlayerStats level
    }

    // Sync player's experience bar with custom exp/expreq progress
    public void syncExpBarWithMinecraft(Player player) {
        if (this.exp >= getRequiredExp()) {
            this.exp -= getRequiredExp();
            this.level++;
            syncPlayerLevelWithMinecraft(player);  // Sync Minecraft level
        }
        float expProgress = (float) (this.exp / getRequiredExp());  // Calculate progress as a float between 0 and 1
        player.setExp(expProgress);  // Set Minecraft experience bar
    }

}
