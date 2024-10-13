package me.diu.gachafight.skills.rarity.rare;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.managers.SkillCooldownManager;
import me.diu.gachafight.skills.managers.SkillDamageSource;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

public class FireStrikeSkill {

    private final GachaFight plugin;

    public FireStrikeSkill(GachaFight plugin) {
        this.plugin = plugin;
    }

    public static void useFireStrike(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();
        double damage = 1.25; // 125% damage
        int cooldown = 6; // 6-second cooldown
        int burnDuration = 60; // Burn for 3 seconds (60 ticks)

        // Check if the player is on cooldown
        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ChatColor.RED + "Skill on cooldown! " + remainingTime + " seconds remaining.");
            return;
        }

        // Activate the Fire Strike ability
        player.sendMessage(ChatColor.LIGHT_PURPLE + "You launched a Fire Strike!");

        Location startLocation = player.getEyeLocation().add(player.getLocation().getDirection().normalize()); // Get the player's view location
        Vector direction = startLocation.getDirection().normalize(); // Get the direction the player is looking

        new BukkitRunnable() {
            double distanceTravelled = 0;
            Location projectileLocation = startLocation.clone(); // Create a clone of the start location

            @Override
            public void run() {
                // Move the projectile forward in the direction the player is facing
                projectileLocation.add(direction.clone().multiply(0.7)); // Move by 0.7 block increments

                // Display fire particle effects at the projectile's location
                player.getWorld().spawnParticle(Particle.FLAME, projectileLocation, 10, 0.2, 0.2, 0.2, 0.01);

                // Check for nearby entities to apply damage and fire effect
                for (Entity entity : projectileLocation.getWorld().getNearbyEntities(projectileLocation, 0.5, 0.5, 0.5)) {
                    if (entity instanceof LivingEntity) {
                        LivingEntity target = (LivingEntity) entity;
                        target.damage(damage, SkillDamageSource.damageSource(player)); // Apply 125% damage
                        target.setFireTicks(burnDuration); // Set the target on fire for 3 seconds
                        player.sendMessage(ChatColor.GOLD + "Fire Strike hit " + target.getName() + " for 125% damage and set them ablaze!");

                        // After hitting the target, stop the projectile
                        this.cancel();
                        return;
                    }
                }

                // Stop the projectile after it travels 10 blocks
                distanceTravelled += 0.7;
                if (distanceTravelled >= 10) {
                    this.cancel();
                }
            }
        }.runTaskTimer(GachaFight.getInstance(), 0, 1); // Runs every tick (20 times per second)
        SkillCooldownManager.setCooldown(playerUUID, slot, cooldown);
    }

}
