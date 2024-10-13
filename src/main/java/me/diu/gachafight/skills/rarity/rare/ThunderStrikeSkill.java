package me.diu.gachafight.skills.rarity.rare;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.skills.managers.SkillCooldownManager;
import me.diu.gachafight.skills.managers.SkillDamageSource;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class ThunderStrikeSkill {

    private final GachaFight plugin;

    public ThunderStrikeSkill(GachaFight plugin) {
        this.plugin = plugin;
    }

    public static void useThunderStrike(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();
        double damage = 1.5; // 150% damage
        int cooldown = 7; // 7 second cooldown
        int stunDuration = 40; // 2s
        double radius = 2;

        // Check if the player is on cooldown
        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ChatColor.RED + "Skill on cooldown! " + remainingTime + " seconds remaining.");
            return;
        }

        // Activate the Thunder Strike ability
        player.sendMessage(ChatColor.LIGHT_PURPLE + "You summoned a Thunder Strike!");

        Location targetLocation = player.getTargetBlock(null, 10).getLocation().add(0, 1, 0); // Get target location 10 blocks away

        new BukkitRunnable() {
            @Override
            public void run() {
                // Summon a lightning bolt at the target location (visual effect)
                player.getWorld().strikeLightningEffect(targetLocation);

                // Display particle effects
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, targetLocation, 50, 1, 1, 1, 0.1);

                // Apply damage and stun nearby entities
                for (Entity entity : targetLocation.getWorld().getNearbyEntities(targetLocation, radius, 1.5, radius)) { // 3-block radius
                    if (entity instanceof LivingEntity) {
                        LivingEntity target = (LivingEntity) entity;
                        target.damage(damage, SkillDamageSource.damageSource(player)); // Deal 150% damage

                        // Apply stun effect
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, stunDuration, 5));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, stunDuration, 5));

                        player.sendMessage(ChatColor.GOLD + "Thunder Strike hit " + target.getName() + " for 150% damage and stunned them for 2 seconds!");
                    }
                }
            }
        }.runTaskLater(GachaFight.getInstance(), 10); // Delay the task slightly to give time for the lightning effect

        SkillCooldownManager.setCooldown(playerUUID, slot, cooldown);
    }
}
