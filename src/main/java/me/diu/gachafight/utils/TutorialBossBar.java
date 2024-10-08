package me.diu.gachafight.utils;

import me.diu.gachafight.GachaFight;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TutorialBossBar {
    public static void showTutorialBossBar(Player player) {
        // Create a BossBar for the player
        BossBar bossBar = Bukkit.createBossBar("Welcome to GachaFight!", BarColor.GREEN, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0);

        // Message sequence with delays
        new BukkitRunnable() {
            int messageIndex = 0;
            final String[] messages = {
                    ColorChat.chat("&7&lWelcome to &6GachaFight!"),
                    ColorChat.chat("&7Let's start off with the basics."),
                            ColorChat.chat("&7First, use your &6key &7to open a &6Gacha Chest!")
            };

            @Override
            public void run() {
                // Update BossBar message
                if (messageIndex < messages.length) {
                    bossBar.setTitle(messages[messageIndex]);
                    messageIndex++;
                } else {
                    // Remove the BossBar after all messages have been shown
                    bossBar.removeAll();
                    cancel();
                }
            }
        }.runTaskTimer(GachaFight.getInstance(), 0L, 85L);
    }
    public static void showPostGachaChestBossBar(Player player) {
        // Create a BossBar for the player
        BossBar bossBar = Bukkit.createBossBar("Great Job!", BarColor.BLUE, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0);

        // Message sequence with delays
        new BukkitRunnable() {
            int messageIndex = 0;
            final String[] messages = {
                    ColorChat.chat("&7Great Job!"),
                    ColorChat.chat("&7Now Go into the &6Dungeon!"),
                    ColorChat.chat("&7Dungeon is Filled with Dangerous Mobs!"),
                    ColorChat.chat("&7Please &6Kill 1 Mushroom Monster!")
            };

            @Override
            public void run() {
                // Update BossBar message
                if (messageIndex < messages.length) {
                    bossBar.setTitle(messages[messageIndex]);
                    messageIndex++;
                } else {
                    // Remove the BossBar after all messages have been shown
                    bossBar.removeAll();
                    cancel();
                }
            }
        }.runTaskTimer(GachaFight.getInstance(), 0L, 85L);
    }

    public static void showDungeonExitBossBar(Player player) {
        // Create a BossBar for the player
        BossBar bossBar = Bukkit.createBossBar("Wow you're very talented!", BarColor.RED, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0);

        // Message sequence with delays
        new BukkitRunnable() {
            int messageIndex = 0;
            final String[] messages = {
                    ColorChat.chat("&6Wow you're very talented!"),
                    ColorChat.chat("&7Now let's not forget this is a dungeon"),
                    ColorChat.chat("&7Monsters respawn very quickly"),
                    ColorChat.chat("&7Let's get out of here as soon as possible!"),
                    ColorChat.chat("&6Go to the Exit!")
            };

            @Override
            public void run() {
                // Update BossBar message
                if (messageIndex < messages.length) {
                    bossBar.setTitle(messages[messageIndex]);
                    messageIndex++;
                } else {
                    // Remove the BossBar after all messages have been shown
                    bossBar.removeAll();
                    cancel();
                }
            }
        }.runTaskTimer(GachaFight.getInstance(), 0L, 85L); // Change message every 5 seconds (100 ticks)
    }
    /**
     * Show BossBar messages after the player completes the tutorial, guiding them one last time.
     */
    public static void showFinalTutorialBossBar(Player player) {
        // Create a BossBar for the player
        BossBar bossBar = Bukkit.createBossBar("Great Job Completing your Tutorial!", BarColor.BLUE, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0);

        // Message sequence with delays
        new BukkitRunnable() {
            int messageIndex = 0;
            final String[] messages = {
                    ColorChat.chat("&6Great Job Completing your Tutorial!"),
                    ColorChat.chat("&7I'll guide you one last time"),
                    ColorChat.chat("&6Dungeon Master &7is how you get back into dungeon"),
                    ColorChat.chat("&6Gain Keys/Gold by killing monsters!"),
                    ColorChat.chat("&7Now Be Safe And Grow Strong Adventurer!"),
                    ColorChat.chat("&7Oh one last thing!"),
                    ColorChat.chat("&7If you need more guide do &6/guide!"),
                    ColorChat.chat("&6&lBest of Luck Adventurer!!")
            };

            @Override
            public void run() {
                // Update BossBar message
                if (messageIndex < messages.length) {
                    bossBar.setTitle(messages[messageIndex]);
                    messageIndex++;
                } else {
                    // Remove the BossBar after all messages have been shown
                    bossBar.removeAll();
                    cancel();
                }
            }
        }.runTaskTimer(GachaFight.getInstance(), 0L, 85L); // Change message every 5 seconds (100 ticks)
    }

}
