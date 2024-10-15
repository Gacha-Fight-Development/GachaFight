package me.diu.gachafight.skills.rarity.common;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.managers.SkillCooldownManager;
import me.diu.gachafight.skills.utils.Skill;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class SwordChargeSkill implements Skill {

    private final GachaFight plugin;
    public static int cooldownDuration;
    public static int skillDuration;
    public static double damage;
    public static final HashMap<UUID, Boolean> swordChargeActive = new HashMap<>();

    public SwordChargeSkill(GachaFight plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "Skills/common.yml");
        if (!configFile.exists()) {
            plugin.saveResource("Skills/common.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        damage = config.getDouble("sword charge.damage");
        cooldownDuration = config.getInt("sword charge.cooldown");
        skillDuration = config.getInt("sword charge.duration");
    }

    @Override
    public void useSkill(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();

        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ColorChat.chat("&cSkill on cooldown! " + remainingTime + " seconds remaining."));
            return;
        }

        swordChargeActive.put(playerUUID, true);
        player.sendMessage(ColorChat.chat("&dSword Charge ready! Your next attack will be empowered."));

        SkillCooldownManager.setCooldown(playerUUID, slot, cooldownDuration);
        spawnActivationParticles(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                deactivateSkill(player);
            }
        }.runTaskLater(plugin, skillDuration * 20L);
    }

    @Override
    public double applySkillEffect(Player player, LivingEntity target) {
        UUID playerUUID = player.getUniqueId();
        System.out.println(swordChargeActive.get(playerUUID));
        if (swordChargeActive.getOrDefault(playerUUID, false)) {
            player.sendMessage(ChatColor.GOLD + "You used Sword Charge! Dealt " + damage*100 + "% damage.");
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0),
                    20, 0.3, 0.3, 0.3, 0.05);
            swordChargeActive.remove(playerUUID);
            return damage;
        }
        return 1.0; // Default multiplier if skill is not active
    }

    @Override
    public boolean isSkillActive(Player player) {
        return swordChargeActive.getOrDefault(player.getUniqueId(), false);
    }
    @Override
    public boolean hasActiveState() {
        return true;
    }

    @Override
    public void deactivateSkill(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (swordChargeActive.remove(playerUUID) != null) {
            player.sendMessage(ColorChat.chat("&7Sword Charge effect has worn off."));
            // You might want to add some deactivation particles here
        }
    }

    private void spawnActivationParticles(Player player) {
        player.getWorld().spawnParticle(
                Particle.END_ROD,
                player.getLocation().add(0, 1, 0),
                30,
                0.5, 1, 0.5,
                0.1
        );
    }
}
