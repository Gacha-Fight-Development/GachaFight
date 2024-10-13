package me.diu.gachafight.skills.rarity.common;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.managers.SkillCooldownManager;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.UUID;

public class SwordChargeSkill {

    private final GachaFight plugin;
    public static final HashMap<UUID, Boolean> swordChargeActive = new HashMap<>(); // Tracks if the next attack is empowered

    public SwordChargeSkill(GachaFight plugin) {
        this.plugin = plugin;
    }

    // Triggered when the player uses the skill (via right-click with a sword)
    public static void useSwordCharge(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();

        // Check if the player is on cooldown
        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ChatColor.RED + "Skill on cooldown! " + remainingTime + " seconds remaining.");
            return;
        }

        // Activate the next attack as a Sword Slash
        swordChargeActive.put(playerUUID, true); // Set the next attack as empowered
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Sword Charge ready! Your next attack will be empowered.");

        // Set a cooldown of 5 seconds
        SkillCooldownManager.setCooldown(playerUUID, slot, 5);
        spawnActivationParticles(player);
    }
    private static void spawnActivationParticles(Player player) {
        player.getWorld().spawnParticle(
                Particle.END_ROD, // The type of particle
                player.getLocation().add(0, 1, 0), // Spawn location, slightly above the player
                30, // Number of particles
                0.5, 1, 0.5, // X, Y, Z offsets for randomization
                0.1 // Speed of the particles
        );
    }
}