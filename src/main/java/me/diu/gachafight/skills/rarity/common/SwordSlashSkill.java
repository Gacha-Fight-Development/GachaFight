package me.diu.gachafight.skills.rarity.common;

import me.diu.gachafight.GachaFight;

import me.diu.gachafight.skills.managers.SkillCooldownManager;
import me.diu.gachafight.skills.managers.SkillDamageSource;
import me.diu.gachafight.skills.utils.Skill;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.UUID;

public class SwordSlashSkill implements Skill {

    private final GachaFight plugin;
    private double damage;
    private int cooldown;

    public SwordSlashSkill(GachaFight plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "Skills/common.yml");
        if (!configFile.exists()) {
            plugin.saveResource("Skills/common.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        damage = config.getDouble("sword slash.damage", 0.75);
        cooldown = config.getInt("sword slash.cooldown", 5);
    }

    @Override
    public void useSkill(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();

        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ChatColor.RED + "Skill on cooldown! " + remainingTime + " seconds remaining.");
            return;
        }

        player.sendMessage(ChatColor.LIGHT_PURPLE + "You launched a Sword Slash!");

        Location startLocation = player.getEyeLocation().add(player.getLocation().getDirection().normalize());
        Vector direction = startLocation.getDirection().normalize();

        new BukkitRunnable() {
            double distanceTravelled = 0;
            Location projectileLocation = startLocation.clone();

            @Override
            public void run() {
                projectileLocation.add(direction.clone().multiply(0.5));
                player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, projectileLocation, 1, 0, 0, 0, 0);

                for (Entity entity : projectileLocation.getWorld().getNearbyEntities(projectileLocation, 0.5, 0.5, 0.5)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        LivingEntity target = (LivingEntity) entity;
                        applySkillEffect(player, target);
                        this.cancel();
                        return;
                    }
                }

                distanceTravelled += 0.5;
                if (distanceTravelled >= 5) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);

        SkillCooldownManager.setCooldown(playerUUID, slot, cooldown);
    }

    @Override
    public double applySkillEffect(Player player, LivingEntity target) {
        target.damage(damage, SkillDamageSource.damageSource(player));
        player.sendMessage(ChatColor.GOLD + "Sword Slash hit " + target.getName() + " for " +
                String.format("%.1f", damage * 100) + "% damage!");
        return 1.0; // This skill doesn't modify damage, it applies its own damage
    }

    @Override
    public boolean hasActiveState() {
        return false; // This skill doesn't have an active state
    }
}
