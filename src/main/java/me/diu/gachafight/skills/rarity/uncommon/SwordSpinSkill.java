package me.diu.gachafight.skills.rarity.uncommon;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.managers.SkillCooldownManager;
import me.diu.gachafight.skills.managers.SkillDamageSource;
import me.diu.gachafight.skills.utils.Skill;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

public class SwordSpinSkill implements Skill {

    private final GachaFight plugin;
    private double damage;
    private int cooldown;
    private double radius;

    public SwordSpinSkill(GachaFight plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "Skills/uncommon.yml");
        if (!configFile.exists()) {
            plugin.saveResource("Skills/uncommon.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        damage = config.getDouble("sword spin.damage", 0.75);
        cooldown = config.getInt("sword spin.cooldown", 5);
        radius = config.getDouble("sword spin.radius", 3.0);
    }

    @Override
    public void useSkill(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();

        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ChatColor.RED + "Skill on cooldown! " + remainingTime + " seconds remaining.");
            return;
        }

        player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius).forEach(entity -> {
            if (entity instanceof LivingEntity && entity != player) {
                applySkillEffect(player, (LivingEntity) entity);
            }
        });

        playParticleEffects(player);

        SkillCooldownManager.setCooldown(playerUUID, slot, cooldown);
    }

    @Override
    public double applySkillEffect(Player player, LivingEntity target) {
        target.damage(damage, SkillDamageSource.damageSource(player));
        player.sendMessage(ChatColor.GOLD + "Sword Spin hit " + target.getName() + " for " +
                String.format("%.1f", damage * 100) + "% damage!");
        return 1.0; // This skill doesn't modify damage, it applies its own damage
    }

    private void playParticleEffects(Player player) {
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation(), 20, 1, 1, 1, 0.1);

    }

    @Override
    public boolean hasActiveState() {
        return false; // This skill doesn't have an active state
    }
}
