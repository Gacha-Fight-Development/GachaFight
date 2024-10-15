package me.diu.gachafight.combat.mobdrops;

import me.diu.gachafight.hooks.VaultHook;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.skills.managers.MobDropSelector;
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
                GiveItemUtils.giveCommonKey(player, 1);
            }
            if (Math.random() < (0.09 + (stats.getLuck() * 0.001))) {
                GiveItemUtils.giveUncommonKey(player, 1);
            }
        }
        if (mobName.contains("Goblin Shaman")) {
            if (Math.random() < (0.30 + (stats.getLuck() * 0.001))) {
                GiveItemUtils.giveCommonKey(player, 1);
            }
            if (Math.random() < (0.12 + (stats.getLuck() * 0.001))) {
                GiveItemUtils.giveUncommonKey(player, 1);
            }
        }
        if (mobName.contains("Goblin King")) {
            if (Math.random() < (1 + (stats.getLuck() * 0.001))) {
                GiveItemUtils.giveUncommonKey(player, 5);
            }
            if (Math.random() < (0.15 + (stats.getLuck() * 0.001))) {
                GiveItemUtils.giveRareKey(player, 1);
            }
            player.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>Boss Bonus:"));
            player.sendMessage(MiniMessage.miniMessage().deserialize(" <green>+ <yellow>$250"));
            player.sendMessage(MiniMessage.miniMessage().deserialize(" <green>+ <aqua>75 EXP"));
            VaultHook.addMoney(player, 250); stats.setExp(stats.getExp() + 75);
        }
    }
}
