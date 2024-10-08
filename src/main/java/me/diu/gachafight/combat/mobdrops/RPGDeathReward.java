package me.diu.gachafight.combat.mobdrops;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.commands.GuideCommand;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.utils.TutorialBossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RPGDeathReward {
    public static void MobDeath(String mobName, Player player) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        if (mobName.contains("rpg_slime_cube") || mobName.contains("rpg_mushroom") || mobName.contains("rpg_mushroom_red") || mobName.contains("rpg_rat") || mobName.contains("rpg_bat") || mobName.contains("rpg_zombie_head")) {
            //tier 1 loot
            if (Math.random() < (0.15 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 991 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <white>Common Gacha Key"));
            }
            if (Math.random() < (0.01 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 992 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>Uncommon Gacha Key"));
            }
            if (player.hasPermission("gacha.tutorial.1")) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission unset gacha.tutorial.1");
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set gacha.tutorial.2");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1,1);
                TutorialBossBar.showDungeonExitBossBar(player);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        GachaFight.getInstance().getGuideSystem().guidePlayerToLocation(player, GuideCommand.preSetLocations.get("tutorialexit"));
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    }
                }.runTaskLater(GachaFight.getInstance(), 85L);
            }
        }
        if (mobName.contains("rpg_zombie") || mobName.contains("rpg_rat_undead") || mobName.contains("rpg_poison_slime_cube")) {
            if (Math.random() < (0.17 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 991 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <white>Common Gacha Key"));
            }
            if (Math.random() < (0.02 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 992 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>Uncommon Gacha Key"));
            }
        }
        if (mobName.contains("rpg_skeleton") || mobName.contains("rpg_skeleton_crossbow")) {
            if (Math.random() < (0.20 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 991 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <white>Common Gacha Key"));
            }
            if (Math.random() < (0.05 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 992 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>Uncommon Gacha Key"));
            }
        }
        if (mobName.contains("rpg_sand_golem") || mobName.contains("rpg_stone_golem")) {
            if (Math.random() < (1 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 991 5 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <white>5x Common Gacha Key"));
            }
            if (Math.random() < (1 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 992 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>Uncommon Gacha Key"));
            }
            if (Math.random() < (0.2 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 992 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>Uncommon Gacha Key"));
            }
            if (Math.random() < (0.02 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 993 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <green>Rare Gacha Key"));
            }
            player.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>Boss Bonus:"));
            player.sendMessage(MiniMessage.miniMessage().deserialize(" <green>+ <yellow>$100"));
            player.sendMessage(MiniMessage.miniMessage().deserialize(" <green>+ <aqua>25 EXP"));
            stats.setMoney(stats.getMoney() + 100); stats.setExp(stats.getExp() + 25);
        }
    }
}
