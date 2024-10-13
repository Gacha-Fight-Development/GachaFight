package me.diu.gachafight.skills.rarity;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.rarity.common.SwordChargeSkill;
import me.diu.gachafight.skills.rarity.common.SwordSlashSkill;
import me.diu.gachafight.skills.rarity.rare.FireStrikeSkill;
import me.diu.gachafight.skills.rarity.rare.ThunderStrikeSkill;
import me.diu.gachafight.skills.rarity.uncommon.SwordBurstSkill;
import me.diu.gachafight.skills.rarity.uncommon.SwordSpinSkill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SkillList {
    private final GachaFight plugin;

    public SkillList(GachaFight plugin) {
        this.plugin = plugin;
        new SwordChargeSkill(plugin);
        new SwordBurstSkill(plugin);
        new SwordSlashSkill(plugin);
        new SwordSpinSkill(plugin);
        new ThunderStrikeSkill(plugin);
        new FireStrikeSkill(plugin);
    }

    public static void skillCheck(Player player, String displayName, int slot) {
        if (displayName.contains("Sword Charge")) {
            SwordChargeSkill.useSwordCharge(player, slot);
        }
        if (displayName.contains("Sword Slash")) {
            SwordSlashSkill.useSwordSlash(player, slot);
        }
        if (displayName.contains("Sword Spin")) {
            SwordSpinSkill.useSwordSpin(player, slot);
        }
        if (displayName.contains("Sword Burst")) {
            SwordBurstSkill.useSwordBurst(player, slot);
        }
        if (displayName.contains("Thunder Strike")) {
            ThunderStrikeSkill.useThunderStrike(player, slot);
        }
        if (displayName.contains("Fire Strike")) {
            FireStrikeSkill.useFireStrike(player, slot);
        }
    }

    // Ability ORDER MATTERS. make sure to list from weakest to stronger ability.
    // return: double << double is the MULTIPLIER for the damage!
    public static double getPlayerSkillCharge(Player player) {
        UUID playerUUID = player.getUniqueId();
        // =====================Sword Charge Check========================
        if (SwordChargeSkill.swordChargeActive.getOrDefault(playerUUID, false)) {
            double damage = 1.5;
            player.sendMessage(ChatColor.GOLD + "You used Sword Charge! Dealt 150% damage.");
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), //Type, Location
                    20, 0.3, 0.3, 0.3, 0.05); //amount, offset, offset, offset, speed
            // remove from hashmap after ability used
            SwordChargeSkill.swordChargeActive.remove(playerUUID);
            return damage;
        }
        //
        return 1;
    }
}
