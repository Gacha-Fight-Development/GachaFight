package me.diu.gachafight.skills.rarity.rare;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.managers.SkillCooldownManager;
import me.diu.gachafight.skills.managers.SkillDamageSource;
import me.diu.gachafight.skills.utils.Skill;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Color;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DushSkill implements Skill {

    private final GachaFight plugin;
    private double damage;
    private double leapDistance;
    private int cooldown;

    public DushSkill(GachaFight plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "Skills/rare.yml");
        if (!configFile.exists()) {
            plugin.saveResource("Skills/rare.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        leapDistance = config.getDouble("dush.leapDistance", 4.0);
        damage = config.getDouble("dush.damage", 1.0);
        cooldown = config.getInt("dush.cooldown", 8);
    }

    @Override
    public void useSkill(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();

        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ColorChat.chat("&cSkill on cooldown! " + remainingTime + " seconds remaining."));
            return;
        }

        player.sendMessage(ColorChat.chat("&bYou leaped forward!"));

        Vector direction = player.getLocation().getDirection().normalize();
        direction.setY(0.2);
        Set<LivingEntity> hitEntities = new HashSet<>();
        player.setVelocity(direction.multiply(leapDistance * 0.275));

        new BukkitRunnable() {
            double distanceTravelled = 0;
            int ticksElapsed = 0;

            @Override
            public void run() {
                if (distanceTravelled >= leapDistance || ticksElapsed >= 20) {
                    this.cancel();
                    return;
                }

                Location particleLocation = player.getLocation().add(0, 1, 0);
                player.getWorld().spawnParticle(Particle.DUST, particleLocation, 10, 0.5, 0.5, 0.5, 0, new Particle.DustOptions(Color.AQUA, 1));

                for (Entity entity : player.getNearbyEntities(0.5, 0.5, 0.5)) {
                    if (entity instanceof LivingEntity && !entity.equals(player)) {
                        LivingEntity target = (LivingEntity) entity;
                        if (!hitEntities.contains(target)) {
                            hitEntities.add(target);
                            applySkillEffect(player, target);
                        }
                    }
                }

                distanceTravelled += 0.5;
                ticksElapsed++;
            }
        }.runTaskTimer(plugin, 0, 1);

        SkillCooldownManager.setCooldown(playerUUID, slot, cooldown);
    }

    @Override
    public double applySkillEffect(Player player, LivingEntity target) {
        target.damage(damage, SkillDamageSource.damageSource(player));
        player.sendMessage(ColorChat.chat("&6Leaped through " + target.getName() + " dealing " +
                String.format("%.1f", damage * 100) + "% damage!"));
        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        return 1.0; // This skill doesn't modify damage, it applies its own damage
    }

    @Override
    public boolean hasActiveState() {
        return false; // This skill doesn't have an active state
    }
}
