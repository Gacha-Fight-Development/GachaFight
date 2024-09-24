package me.diu.gachafight.shop.potion.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class PotionUseListener implements Listener {

    public PotionUseListener(GachaFight plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerUsePotion(PlayerInteractEvent event) {
        // Check if the player right-clicked with an item in hand
        if (event.getItem() != null && event.getAction().name().contains("RIGHT_CLICK")) {
            ItemStack item = event.getItem();
            Player player = event.getPlayer();

            // Check if the item is a potion and has the name "Small HP Potion"
            if (item.getType() == Material.LEATHER_HORSE_ARMOR && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName() && meta.getDisplayName().contains("Small HP Potion")) {
                    useHPPotion(player, 6);
                    item.setAmount(item.getAmount() - 1);
                    event.setCancelled(true);
                }
                if (meta.hasDisplayName() && meta.getDisplayName().contains("Small Speed Potion")) {
                    useSpeedPotion(player, 2);
                    item.setAmount(item.getAmount() - 1);
                    event.setCancelled(true);
                }
            }
        }
    }

    public void useHPPotion(Player player, int heal) {
        PlayerStats playerStats = PlayerStats.getPlayerStats(player);
        double newHp = Math.min(playerStats.getHp() + heal, playerStats.getMaxhp());
        playerStats.setHp(newHp);  // Sync health with hearts
        playerStats.updateActionbar(player);  // Update actionbar with current health
        player.sendMessage(ColorChat.chat("&a+ &c"+ heal+"❤"));
    }

    public void useSpeedPotion(Player player, float speed) {
        PlayerStats playerStats = PlayerStats.getPlayerStats(player);
        double originalSpeed = playerStats.getSpeed() * 0.1; // Store the original speed
        double newSpeed = originalSpeed + (speed * 0.01);

        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue((PlayerStats.getPlayerStats(player).getSpeed()*0.1) + (speed * 0.01));
        player.sendMessage(ColorChat.chat("&a+ &b"+speed+" Speed"));

        new BukkitRunnable() {
            @Override
            public void run() {
                // Revert the player's speed after 30 seconds
                player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(originalSpeed);
                player.sendMessage(ColorChat.chat("&cSpeed boost has worn off!"));
            }
        }.runTaskLater(GachaFight.getInstance(), 30 * 20L); // 30 seconds delay (20 ticks = 1 second)
    }
}
