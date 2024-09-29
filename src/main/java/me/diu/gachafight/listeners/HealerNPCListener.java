package me.diu.gachafight.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class HealerNPCListener implements Listener {

    private final GachaFight plugin;

    public HealerNPCListener(GachaFight plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        // Check if the entity is the healer NPC (assuming the healer's name is "Healer")
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // Ignore offhand interactions
        }
        if (entity.getName().equalsIgnoreCase("healer")) {
            PlayerStats stats = PlayerStats.getPlayerStats(player);

            double currentHp = stats.getHp();
            double maxHp = stats.getMaxhp();
            double playerMoney = stats.getMoney();
            double healCost = (stats.getMaxhp()*0.1) - 0.8;

            // Check if the player is already at max HP
            if (currentHp >= maxHp) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You are already at full HP!"));
                return;
            }

            if (playerMoney >= healCost) {
                // Deduct the cost and heal the player
                stats.setMoney(playerMoney - healCost);
                stats.setHp(maxHp);
                stats.syncHealthWithHearts(player);
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Your HP has been fully restored, and $0.1 has been deducted from your balance."));
            } else {
                // Not enough money, send a message to the player
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You need at least $0.1 to heal yourself!"));
            }
        }
    }
}
