package me.diu.gachafight.skills.rarity.uncommon;

import io.lumine.mythic.bukkit.utils.particles.Particle;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.skills.managers.SkillCooldownManager;
import me.diu.gachafight.skills.managers.SkillDamageSource;
import org.bukkit.ChatColor;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SwordSpinSkill {

    private final GachaFight plugin;

    public SwordSpinSkill(GachaFight plugin) {
        this.plugin = plugin;
    }

    public static void useSwordSpin(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        double damage = 0.75;
        int cooldown = 5;

        // Check cooldown
        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ChatColor.RED + "Skill on cooldown! " + remainingTime + " seconds remaining.");
            return;
        }

        // Apply damage to nearby entities in a 3-block radius
        player.getWorld().getNearbyEntities(player.getLocation(), 3, 3, 3).forEach(entity -> {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;

                target.damage(damage, SkillDamageSource.damageSource(player)); //Damage

                player.sendMessage(ChatColor.GOLD + "Sword Spin hit " + target.getName() + " for " + damage + " damage!");
            }
        });

        // Play particle effects (sweeping + critical particles)
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK.toBukkitParticle(), player.getLocation(), 20, 1, 1, 1, 0.1);
        player.getWorld().spawnParticle(Particle.CRIT_MAGIC.toBukkitParticle(), player.getLocation(), 30, 1, 1, 1, 0.1);

        // Set skill cooldown (5 seconds)
        SkillCooldownManager.setCooldown(playerUUID, slot, cooldown);
    }
}

