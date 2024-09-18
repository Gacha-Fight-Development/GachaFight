package me.diu.gachafight.combat;

import me.diu.gachafight.playerstats.PlayerStats;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BossDeathReward {
    public static void specificBossDeath(String bossName, Player player) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        double random = Math.random();
        if (bossName.equals("The Goblin King")) { //goblin king extra drops
            if (random < (0.3 + (stats.getLuck() * 0.001))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 992 " + (int) (Math.floor(Math.random() * 2) + 1) +" "+ player.getName() + " true");
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>+ <gray>Uncommon Gacha Key"));
            }
        }
    }
}
