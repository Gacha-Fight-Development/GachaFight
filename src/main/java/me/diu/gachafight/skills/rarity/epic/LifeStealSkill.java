package me.diu.gachafight.skills.rarity.epic;

import io.lumine.mythic.bukkit.utils.particles.Particle;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.managers.SkillCooldownManager;
import me.diu.gachafight.skills.utils.Skill;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class LifeStealSkill implements Skill {

    private final GachaFight plugin;
    private static FileConfiguration config;
    private static int cooldownDuration;
    private static int skillDuration;
    private static double healPercentage;
    public static final HashMap<UUID, Boolean> lifeStealActive = new HashMap<>();

    public LifeStealSkill(GachaFight plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "Skills/epic.yml");
        if (!configFile.exists()) {
            plugin.saveResource("Skills/epic.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        cooldownDuration = config.getInt("life steal.cooldown", 10);
        skillDuration = config.getInt("life steal.duration", 3);
        healPercentage = config.getDouble("life steal.heal_percentage", 0.5);
    }

    @Override
    public double applySkillEffect(Player player, LivingEntity target) {
        UUID playerUUID = player.getUniqueId();
        if (lifeStealActive.getOrDefault(playerUUID, false)) {
            double damage = target.getLastDamage();
            double healAmount = damage * healPercentage;
            player.setHealth(Math.min(player.getHealth() + healAmount, player.getMaxHealth()));
            player.sendMessage(ColorChat.chat("&4You stole " + String.format("%.1f", healAmount) + " health from your target!"));

            // Spawn particles for the life steal effect
            player.getWorld().spawnParticle(
                    Particle.SPELL_WITCH.toBukkitParticle(),
                    target.getLocation().add(0, 1, 0),
                    20,
                    0.5, 0.5, 0.5,
                    0.1
            );
            lifeStealActive.remove(playerUUID);

            return 1.0; // No damage multiplier for Life Steal
        }
        return 1.0; // Default multiplier if skill is not active
    }

    @Override
    public boolean isSkillActive(Player player) {
        return lifeStealActive.getOrDefault(player.getUniqueId(), false);
    }

    @Override
    public void deactivateSkill(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (lifeStealActive.remove(playerUUID) != null) {
            player.sendMessage(ColorChat.chat("&7Life Steal effect has worn off."));
        }
    }

    @Override
    public void useSkill(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();

        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ColorChat.chat("&cSkill on cooldown! " + remainingTime + " seconds remaining."));
            return;
        }

        lifeStealActive.put(playerUUID, true);
        player.sendMessage(ColorChat.chat("&4Life Steal activated! Your next attack will drain health from your target."));

        SkillCooldownManager.setCooldown(playerUUID, slot, cooldownDuration);
        spawnActivationParticles(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                deactivateSkill(player);
            }
        }.runTaskLater(plugin, skillDuration * 20L);
    }

    private void spawnActivationParticles(Player player) {
        player.getWorld().spawnParticle(
                Particle.SPELL_WITCH.toBukkitParticle(),
                player.getLocation().add(0, 1, 0),
                40,
                0.7, 1, 0.7,
                0.1
        );
    }
    @Override
    public boolean hasActiveState() {
        return true;
    }
}
