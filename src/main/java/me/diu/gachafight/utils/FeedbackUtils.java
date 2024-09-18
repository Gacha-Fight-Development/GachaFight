package me.diu.gachafight.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.Particle;

public class FeedbackUtils {

    public static void playCompletionSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }

    public static void displayCompletionTitle(Player player) {
        player.sendTitle("§aQuest Completed!", "§eYou have earned rewards.", 10, 70, 20);
    }

    public static void showCompletionParticles(Player player) {
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 20, 1, 1, 1);
    }
}
