package me.diu.gachafight.skills.rarity.rare;

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

public class FireStrikeSkill implements Skill {

    private final GachaFight plugin;
    private double damage;
    private double cooldown;
    private int burnDuration;

    public FireStrikeSkill(GachaFight plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "Skills/rare.yml");
        if (!configFile.exists()) {
            plugin.saveResource("Skills/rare.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        damage = config.getDouble("fire strike.damage", 1.25);
        cooldown = config.getDouble("fire strike.cooldown", 6);
        burnDuration = config.getInt("fire strike.burnDuration", 3);
    }

    @Override
    public void useSkill(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();

        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ChatColor.RED + "Skill on cooldown! " + remainingTime + " seconds remaining.");
            return;
        }

        player.sendMessage(ChatColor.LIGHT_PURPLE + "You launched a Fire Strike!");
        Location startLocation = player.getEyeLocation().add(player.getLocation().getDirection().normalize());
        Vector direction = startLocation.getDirection().normalize();

        new BukkitRunnable() {
            double distanceTravelled = 0;
            Location projectileLocation = startLocation.clone();

            @Override
            public void run() {
                projectileLocation.add(direction.clone().multiply(0.7));
                player.getWorld().spawnParticle(Particle.FLAME, projectileLocation, 10, 0.2, 0.2, 0.2, 0.01);

                for (Entity entity : projectileLocation.getWorld().getNearbyEntities(projectileLocation, 0.5, 0.5, 0.5)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        applySkillEffect(player, (LivingEntity) entity);
                        this.cancel();
                        return;
                    }
                }

                distanceTravelled += 0.7;
                if (distanceTravelled >= 10) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);

        SkillCooldownManager.setCooldown(playerUUID, slot, cooldown);
    }

    @Override
    public double applySkillEffect(Player player, LivingEntity target) {
        target.damage(damage, SkillDamageSource.damageSource(player));
        target.setFireTicks(burnDuration * 20);
        player.sendMessage(ChatColor.GOLD + "Fire Strike hit " + target.getName() + " for " +
                String.format("%.1f", damage * 100) + "% damage and set them ablaze!");
        return 1.0; // This skill doesn't modify damage, it applies its own damage
    }

    @Override
    public boolean hasActiveState() {
        return false; // This skill doesn't have an active state
    }
}
