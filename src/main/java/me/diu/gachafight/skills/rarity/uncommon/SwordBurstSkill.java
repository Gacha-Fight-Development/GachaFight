package me.diu.gachafight.skills.rarity.uncommon;

import io.lumine.mythic.bukkit.utils.particles.Particle;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.skills.managers.SkillCooldownManager;
import me.diu.gachafight.skills.managers.SkillDamageSource;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

public class SwordBurstSkill {

    private final GachaFight plugin;

    public SwordBurstSkill(GachaFight plugin) {
        this.plugin = plugin;
    }

    public static void useSwordBurst(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        double damage = 1.5;
        int cooldown = 5;

        // Check cooldown
        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ChatColor.RED + "Skill on cooldown! " + remainingTime + " seconds remaining.");
            return;
        }

        // Launch projectile 2.5 blocks in front of player
        Location startLocation = player.getEyeLocation().add(player.getLocation().getDirection().normalize());
        Vector direction = startLocation.getDirection().normalize();

        new BukkitRunnable() {
            double distanceTravelled = 0;
            Location projectileLocation = startLocation.clone();

            @Override
            public void run() {
                projectileLocation.add(direction.clone().multiply(0.5));

                // Particle effect for the projectile (small burst with magic)
                player.getWorld().spawnParticle(Particle.CRIT_MAGIC.toBukkitParticle(), projectileLocation, 5, 0, 0, 0, 0.1);
                player.getWorld().spawnParticle(Particle.SMOKE_NORMAL.toBukkitParticle(), projectileLocation, 2, 0, 0, 0, 0.02);

                for (Entity entity : projectileLocation.getWorld().getNearbyEntities(projectileLocation, 0.5, 0.5, 0.5)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;

                        // Apply skill damage
                        target.damage(damage, SkillDamageSource.damageSource(player)); //Damage

                        player.sendMessage(ChatColor.GOLD + "Sword Burst hit " + target.getName() + " for " + damage + " damage!");

                        // Cancel the task after hitting the first target
                        this.cancel();
                        return;
                    }
                }

                distanceTravelled += 0.5;
                if (distanceTravelled >= 2.5) {
                    this.cancel(); // End after 2.5 blocks
                }
            }
        }.runTaskTimer(GachaFight.getInstance(), 0, 1);

        // Set skill cooldown (5 seconds)
        SkillCooldownManager.setCooldown(playerUUID, slot, cooldown);
    }
}
