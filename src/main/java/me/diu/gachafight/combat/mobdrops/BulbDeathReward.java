package me.diu.gachafight.combat.mobdrops;

import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.GiveItemUtils;
import org.bukkit.entity.Player;

public class BulbDeathReward {
    public static void MobDeath(String mobName, Player player) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        if (mobName.contains("Lilypad Bulb")) {
            //tier 1 loot
            stats.setLuck(stats.getLuck()+0.1);
            player.sendMessage(ColorChat.chat("&a+ &60.1 &2Luck"));
        }
    }
}
