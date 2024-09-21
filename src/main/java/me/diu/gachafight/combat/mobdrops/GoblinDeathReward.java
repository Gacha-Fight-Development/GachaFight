package me.diu.gachafight.combat.mobdrops;

import me.diu.gachafight.playerstats.PlayerStats;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GoblinDeathReward {
    public static void MobDeath(String mobName, Player player) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        if (mobName.equals("Goblin Warrior")) {
            if (Math.random() < (0.15 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 991 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>Uncommon Gacha Key"));
            }
            if (Math.random() < (0.01 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 992 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>Uncommon Gacha Key"));
            }
        }
        if (mobName.equals("Goblin Knife")) {
            if (Math.random() < (0.20 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 991 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <white>Common Gacha Key"));
            }
            if (Math.random() < (0.05 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 992 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>Uncommon Gacha Key"));
            }
        }
        if (mobName.equals("Goblin Shaman")) {
            if (Math.random() < (0.22 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 991 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <white>Common Gacha Key"));
            }
            if (Math.random() < (0.07 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 992 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>Uncommon Gacha Key"));
            }
        }
        if (mobName.equals("Goblin King")) {
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
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>Uncommon Gacha Key"));
            }
        }
    }
}
