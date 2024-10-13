package me.diu.gachafight.skills.rarity.common;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.skills.managers.SkillCooldownManager;
import me.diu.gachafight.skills.managers.SkillDamageSource;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

public class SwordSlashSkill {

    private final GachaFight plugin;

    public SwordSlashSkill(GachaFight plugin) {
        this.plugin = plugin;
    }

    public static void useSwordSlash(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();
        double damage = 0.75;
        int cooldown = 5;

        // Check if the player is on cooldown
        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ChatColor.RED + "Skill on cooldown! " + remainingTime + " seconds remaining.");
            return;
        }

        // Activate the Sword Slash ability
        player.sendMessage(ChatColor.LIGHT_PURPLE + "You launched a Sword Slash!");

        Location startLocation = player.getEyeLocation().add(player.getLocation().getDirection().normalize()); // Get the player's view location
        Vector direction = startLocation.getDirection().normalize(); // Get the direction the player is looking

        new BukkitRunnable() {
            double distanceTravelled = 0;
            Location projectileLocation = startLocation.clone(); // Create a clone of the start location

            @Override
            public void run() {
                // Move the projectile forward in the direction the player is facing
                projectileLocation.add(direction.clone().multiply(0.5)); // Move by 0.5 block increments

                // Display particle effects at the projectile's location
                player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, projectileLocation, 1, 0, 0, 0, 0);

                // Check for nearby entities to apply damage
                for (Entity entity : projectileLocation.getWorld().getNearbyEntities(projectileLocation, 0.5, 0.5, 0.5)) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity target = (LivingEntity) entity;
                        target.damage(damage, SkillDamageSource.damageSource(player)); //Damage
                        player.sendMessage(ChatColor.GOLD + "Sword Slash hit " + target.getName() + " for 75% damage!");

                        // After the attack, stop the projectile
                        this.cancel(); // Stop the projectile when it hits an entity
                        return;
                    }
                }

                // Stop the projectile after 5 blocks
                distanceTravelled += 0.5;
                if (distanceTravelled >= 5) {
                    this.cancel();
                }
            }
        }.runTaskTimer(GachaFight.getInstance(), 0, 1); // Runs every tick (20 times per second)

        SkillCooldownManager.setCooldown(playerUUID, slot, cooldown);
    }

}
