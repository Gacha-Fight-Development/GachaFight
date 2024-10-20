package me.diu.gachafight.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.hooks.VaultHook;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.utils.Calculations;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
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
            double playerMoney = VaultHook.getBalance(player);
            double healCost = Calculations.healerCost(maxHp);

            // Check if the player is already at max HP
            if (currentHp >= maxHp) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You are already at full HP!"));
                return;
            }

            if (playerMoney >= healCost) {
                // Deduct the cost and heal the player
                VaultHook.withdraw(player, healCost);
                stats.setHp(maxHp);
                stats.syncHealthWithHearts(player);
                String cost = String.format("%.1f", healCost);
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0.1);

                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Your HP has been fully restored, and $" + cost + " has been deducted from your balance."));
            } else {
                // Not enough money, send a message to the player
                String cost = String.format("%.1f", healCost);
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You need at least $" + cost + " to heal yourself!"));
            }
        }
    }
}
