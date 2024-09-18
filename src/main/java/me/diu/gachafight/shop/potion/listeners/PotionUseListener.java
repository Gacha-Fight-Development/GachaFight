package me.diu.gachafight.shop.potion.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
                    // Get the player's stats
                    PlayerStats playerStats = PlayerStats.getPlayerStats(player);

                    // Heal the player for 5 HP, ensuring it doesn't exceed max HP
                    double newHp = Math.min(playerStats.getHp() + 5, playerStats.getMaxhp());
                    playerStats.setHp(newHp);  // Sync health with hearts
                    playerStats.updateActionbar(player);  // Update actionbar with current health

                    // Send a message to the player confirming the heal
                    player.sendMessage(ColorChat.chat("&aYou used a Small HP Potion and healed 5 HP!"));

                    // Consume the potion
                    item.setAmount(item.getAmount() - 1);

                    // Cancel the event to prevent any further interaction with the item
                    event.setCancelled(true);
                }
            }
        }
    }
}
