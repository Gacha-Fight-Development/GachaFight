package me.diu.gachafight.skills.rarity.rare;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.managers.SkillCooldownManager;
import me.diu.gachafight.skills.utils.Skill;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.UUID;

public class ThunderStrikeSkill implements Skill {

    private final GachaFight plugin;
    private double damage;
    private double cooldown;
    private int stunDuration;
    private double radius;

    public ThunderStrikeSkill(GachaFight plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "Skills/rare.yml");
        if (!configFile.exists()) {
            plugin.saveResource("Skills/rare.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        damage = config.getDouble("thunder strike.damage", 1.5);
        cooldown = config.getDouble("thunder strike.cooldown", 7);
        stunDuration = config.getInt("thunder strike.stunDuration", 2);
        radius = config.getDouble("thunder strike.radius", 3);
    }

    @Override
    public void useSkill(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();

        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ChatColor.RED + "Skill on cooldown! " + remainingTime + " seconds remaining.");
            return;
        }

        player.sendMessage(ChatColor.LIGHT_PURPLE + "You summoned a Thunder Strike!");
        applySkillEffect(player, null);
        SkillCooldownManager.setCooldown(playerUUID, slot, cooldown);
    }

    @Override
    public double applySkillEffect(Player player, LivingEntity ignoredTarget) {
        Location targetLocation = player.getTargetBlock(null, 10).getLocation().add(0, 1, 0);

        player.getWorld().strikeLightningEffect(targetLocation);


        for (Entity entity : targetLocation.getWorld().getNearbyEntities(targetLocation, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;

                target.damage(damage * target.getMaxHealth(), player);
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, stunDuration * 20, 5));
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, stunDuration * 20, 5));

                player.sendMessage(ChatColor.GOLD + "Thunder Strike hit " + target.getName() + " for " +
                        String.format("%.1f", damage * 100) + "% damage and stunned them for " +
                        stunDuration + " seconds!");
            }
        }

        return 1.0; // This skill doesn't modify damage, it applies its own damage
    }

    @Override
    public boolean hasActiveState() {
        return false; // This skill doesn't have an active state
    }
}
