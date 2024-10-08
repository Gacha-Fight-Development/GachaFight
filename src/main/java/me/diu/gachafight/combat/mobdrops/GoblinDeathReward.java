package me.diu.gachafight.combat.mobdrops;

import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.utils.GiveItemUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GoblinDeathReward {
    public static void MobDeath(String mobName, Player player) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        if (mobName.contains("Goblin Warrior")) {

            if (Math.random() < (0.20 + (stats.getLuck() * 0.001))) {
                GiveItemUtils.giveCommonKey(player, 1);
            }
            if (Math.random() < (0.05 + (stats.getLuck() * 0.001))) {
                GiveItemUtils.giveUncommonKey(player, 1);
            }
        }
        if (mobName.contains("Goblin Knife")) {
            if (Math.random() < (0.25 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 991 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <white>Common Gacha Key"));
            }
            if (Math.random() < (0.09 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 992 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>Uncommon Gacha Key"));
            }
        }
        if (mobName.contains("Goblin Shaman")) {
            if (Math.random() < (0.30 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 991 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <white>Common Gacha Key"));
            }
            if (Math.random() < (0.12 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 992 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>Uncommon Gacha Key"));
            }
        }
        if (mobName.contains("Goblin King")) {
            if (Math.random() < (1 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 992 5 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>5x Uncommon Gacha Key"));
            }
            if (Math.random() < (0.08 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 993 1 " + player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <green>Rare Gacha Key"));
            }
            player.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>Boss Bonus:"));
            player.sendMessage(MiniMessage.miniMessage().deserialize(" <green>+ <yellow>$250"));
            player.sendMessage(MiniMessage.miniMessage().deserialize(" <green>+ <aqua>75 EXP"));
            stats.setMoney(stats.getMoney() + 250); stats.setExp(stats.getExp() + 75);
        }
    }
}
