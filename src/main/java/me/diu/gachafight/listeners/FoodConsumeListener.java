package me.diu.gachafight.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.utils.ExtractLore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class FoodConsumeListener implements Listener {

    public FoodConsumeListener(GachaFight plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack consumedItem = event.getItem();

        // Check if the item has lore
        if (consumedItem.hasItemMeta()) {
            ItemMeta meta = consumedItem.getItemMeta();

            if (meta.hasLore()) {
                List<String> loreList = meta.getLore();
                if (loreList != null && !loreList.isEmpty()) {
                    // Assuming the relevant lore is on the first line
                    String lore = loreList.get(0);

                    // Extract healing amount and duration
                    String[] healingData = ExtractLore.extractHealingAndDuration(lore);
                    if (healingData[0].isEmpty() || healingData[1].isEmpty()) {
                        return; // No valid lore found
                    }

                    double healingPerSecond = Double.parseDouble(healingData[0]);
                    int durationSeconds = Integer.parseInt(healingData[1]);

                    // Start healing the player
                    startHealing(player, healingPerSecond, durationSeconds);
                }
            }
        }
    }
    private void startHealing(Player player, double healingPerSecond, int durationSeconds) {
        new BukkitRunnable() {
            int ticksElapsed = 0;

            @Override
            public void run() {
                // Heal the player every second
                if (ticksElapsed >= durationSeconds * 20) {
                    this.cancel(); // Stop when duration is complete
                    return;
                }
                PlayerStats stats = PlayerStats.getPlayerStats(player);
                // Heal the player
                if (stats.getHp() > stats.getGearStats().getTotalMaxHp()) {
                    if (stats.getHp() + stats.getGearStats().getTotalMaxHp() + healingPerSecond >= stats.getMaxhp() + stats.getGearStats().getTotalMaxHp()) {
                        //stop healing when hp is max
                        stats.setHp(stats.getMaxhp());
                    } else {
                        //heals when its not max
                        stats.setHp(stats.getHp() + healingPerSecond);
                    }
                } else {
                    stats.setHp(stats.getHp() + healingPerSecond);
                }

                // Update elapsed ticks
                ticksElapsed += 20;
            }
        }.runTaskTimer(GachaFight.getInstance(), 0L, 20L); // Schedule every second (20 ticks)
    }
}
