package me.diu.gachafight.skills.rarity.uncommon;

import io.lumine.mythic.bukkit.utils.particles.Particle;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.managers.SkillCooldownManager;
import me.diu.gachafight.skills.managers.SkillDamageSource;
import me.diu.gachafight.skills.utils.Skill;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.UUID;

public class SwordBurstSkill implements Skill {

    private final GachaFight plugin;
    private double damage;
    private int cooldown;
    private double projectileSpeed;
    private double maxDistance;
    private double hitboxSize;

    public SwordBurstSkill(GachaFight plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "Skills/uncommon.yml");
        if (!configFile.exists()) {
            plugin.saveResource("Skills/uncommon.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        damage = config.getDouble("sword burst.damage", 1.5);
        cooldown = config.getInt("sword burst.cooldown", 5);
        projectileSpeed = config.getDouble("sword burst.projectileSpeed", 0.5);
        maxDistance = config.getDouble("sword burst.maxDistance", 2.5);
        hitboxSize = config.getDouble("sword burst.hitboxSize", 0.5);
    }

    @Override
    public void useSkill(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();

        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ChatColor.RED + "Skill on cooldown! " + remainingTime + " seconds remaining.");
            return;
        }

        Location startLocation = player.getEyeLocation().add(player.getLocation().getDirection().normalize());
        Vector direction = startLocation.getDirection().normalize();

        new BukkitRunnable() {
            double distanceTravelled = 0;
            Location projectileLocation = startLocation.clone();

            @Override
            public void run() {
                projectileLocation.add(direction.clone().multiply(projectileSpeed));

                spawnParticles(projectileLocation);

                for (Entity entity : projectileLocation.getWorld().getNearbyEntities(projectileLocation, hitboxSize, hitboxSize, hitboxSize)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        applySkillEffect(player, (LivingEntity) entity);
                        this.cancel();
                        return;
                    }
                }

                distanceTravelled += projectileSpeed;
                if (distanceTravelled >= maxDistance) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);

        SkillCooldownManager.setCooldown(playerUUID, slot, cooldown);
    }

    @Override
    public double applySkillEffect(Player player, LivingEntity target) {
        target.damage(damage, SkillDamageSource.damageSource(player));
        player.sendMessage(ChatColor.GOLD + "Sword Burst hit " + target.getName() + " for " +
                String.format("%.1f", damage * 100) + "% damage!");
        return 1.0; // This skill doesn't modify damage, it applies its own damage
    }

    private void spawnParticles(Location location) {
        location.getWorld().spawnParticle(Particle.CRIT_MAGIC.toBukkitParticle(), location, 5, 0, 0, 0, 0.1);
        location.getWorld().spawnParticle(Particle.SMOKE_NORMAL.toBukkitParticle(), location, 2, 0, 0, 0, 0.02);
    }
    @Override
    public boolean hasActiveState() {
        return false; // This skill doesn't have an active state
    }
}
