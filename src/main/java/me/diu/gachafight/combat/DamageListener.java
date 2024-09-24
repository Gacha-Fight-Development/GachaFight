package me.diu.gachafight.combat;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitEntity;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.combat.mobdrops.GoblinDeathReward;
import me.diu.gachafight.combat.mobdrops.RPGDeathReward;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.quest.listeners.QuestKillListener;
import me.diu.gachafight.utils.ColorChat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;


public class DamageListener implements Listener {

    public DamageListener(GachaFight plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // when arrow hits player

        if (isSafezone(event.getEntity().getLocation())) {
            event.setCancelled(true);
            event.getDamager().sendMessage(ColorChat.chat("&aSafezone. &cPvP only in Dungeon."));
            return;
        }

        if (event.getDamager() instanceof Arrow && event.getEntity() instanceof Player) {
            PlayerStats stats = PlayerStats.getPlayerStats(((Player) event.getEntity()));
            event.setDamage(0);
            stats.syncHealthWithHearts((Player) event.getEntity());
        }

        // Handle PvE - Player attacks a mob
        if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity && event.getEntity().getType() != EntityType.PLAYER && event.getEntity().getType() != EntityType.ARMOR_STAND) {
            handlePlayerVsEntity(event, (Player) event.getDamager(), (LivingEntity) event.getEntity());
        }

        // Handle PvP - Player attacks another player
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            handlePlayerVsPlayer(event, (Player) event.getDamager(), (Player) event.getEntity());
        }

        // Handle Gacha Chest
        if (event.getDamager() instanceof Player && event.getEntity().getName().contains("Gacha Chest")) {

        }
    }

    // Handle Player vs Entity (PvE)
    private void handlePlayerVsEntity(EntityDamageByEntityEvent event, Player player, LivingEntity entity) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);

        // Retrieve player's damage stat
        double playerDamage = stats.getDamage() + stats.getWeaponStats().getDamage() + stats.getGearStats().getTotalDamage();

        // Get the mob's armor using MythicMobs API
        double mobArmor = getMobArmor(entity);

        // Calculate the custom total damage
        double totalDamage = playerDamage - mobArmor; // Calc PvE
        if (totalDamage < 0.5) {
            totalDamage = 0.5;
        }

        if (isCritical(player)) {
            totalDamage = totalDamage*1.2;
        }

        // Check if the mob will die from this hit
        if (entity.getHealth() - totalDamage <= 0) {
            handleMobDeath(player, entity);  // Handle mob death, rewards, etc.
            QuestKillListener.questKill(player, entity); //handle quest
        }

        if (player.hasPermission("gachafight.toggledamage")) {
            player.sendMessage(ColorChat.chat("&cFinal Damage: &f" + String.format("%.1f",totalDamage)));
        }

        // Cancel the default damage and apply custom damage
        event.setDamage(0);
        double newHealth = entity.getHealth() - totalDamage;
        if (newHealth <= 0) {
            entity.setHealth(0);
        } else {
            entity.setHealth(newHealth);
        }
    }

    // Handle Player vs Player (PvP)
    private void handlePlayerVsPlayer(EntityDamageByEntityEvent event, Player attacker, Player target) {
        // Get attacker and target stats
        PlayerStats attackerStats = PlayerStats.getPlayerStats(attacker);
        PlayerStats targetStats = PlayerStats.getPlayerStats(target);

        // Calculate the attacker's total damage
        double attackerDamage = attackerStats.getDamage() + attackerStats.getWeaponStats().getDamage() + attackerStats.getGearStats().getTotalDamage();

        // Calculate the target's total armor
        double targetArmor = targetStats.getArmor() + targetStats.getGearStats().getTotalArmor();

        // Calculate the custom damage for PvP
        double totalDamage = attackerDamage - (targetArmor); // Adjust the armor effect as needed
        if (attackerStats.getLevel() > targetStats.getLevel()) {
            totalDamage = totalDamage*(1-((attackerStats.getLevel()-targetStats.getLevel())*0.1));
        }
        if (totalDamage < 0.5) {
            totalDamage = 0.5;
        }

        // Apply custom damage to the target
        event.setDamage(0); // Cancel the default damage
        if (attacker.hasPermission("gachafight.toggledamage")) {
            attacker.sendMessage(ColorChat.chat("&cFinal Damage: &f" + totalDamage));
        }
        targetStats.setHp(targetStats.getHp() - totalDamage);
        targetStats.syncHealthWithHearts(target);

        // Check if the target will die from this hit
        if (targetStats.getHp() <= 0) {
            target.setHealth(0); // Trigger death event
        }
    }


    // New method to handle mob death and give rewards
    private void handleMobDeath(Player player, Entity entity) {
        if (entity instanceof LivingEntity) {
            double mobHp = ((LivingEntity) entity).getMaxHealth();
            double expGained = mobHp / 7.5;
            double moneyGained = mobHp / 20;

            // Add experience to the player
            PlayerStats playerStats = PlayerStats.getPlayerStats(player);
            playerStats.addExp((int) expGained, player);

            // Add money to the player
            playerStats.setMoney(playerStats.getMoney() + moneyGained);
            if (entity.getName().contains("Goblin")) {
                GoblinDeathReward.MobDeath(entity.getName(), player);
            } else if (entity.getName().contains("rpg")) {
                RPGDeathReward.MobDeath(entity.getName(), player);
            }

            // Notify the player of the exp and money gained
            player.sendActionBar(MiniMessage.miniMessage().deserialize("<green>+ <dark_aqua>Exp: <aqua>" + String.format("%.2f", expGained) + "<black> | <gold> Money: <yellow>" + String.format("%.2f", moneyGained) + "</green>"));
        }
    }


    @EventHandler
    public void onMobDamage(MythicDamageEvent event) { //damage by mob
        // Handle when MythicMob attacks a player
        if (event.getCaster() instanceof ActiveMob && event.getTarget() instanceof BukkitEntity) {
            if (event.getTarget().getBukkitEntity() instanceof Player) {
                Player player = ((BukkitEntity) event.getTarget()).getEntityAsPlayer();
                // Check if the damager is a MythicMob
                ActiveMob mythicMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(event.getCaster().getEntity());
                if (mythicMob != null) {
                    // Get the MythicMob's damage
                    double mobDamage = event.getDamage();

                    // Get the player's armor stat
                    PlayerStats stats = PlayerStats.getPlayerStats(player);
                    double playerArmor = stats.getArmor() + stats.getGearStats().getTotalArmor();

                    // Calculate the total damage received by the player
                    double totalDamage = mobDamage - (playerArmor * 0.5); // Calc EvP

                    if (totalDamage < 0) {
                        totalDamage = 0.05;
                    }

                    // Cancel the default damage and apply custom damage
                    event.setDamage(0);
                    stats.setHp(stats.getHp() - totalDamage);
                    stats.syncHealthWithHearts(player);
                    if (stats.getHp() <= 0) {
                        player.setHealth(0); // This will trigger the death event
                        stats.syncHealthWithHearts(player);
                        stats.updateActionbar(player);
                    } else {
                        // Sync the player's hearts with the current HP
                        stats.syncHealthWithHearts(player);
                        stats.updateActionbar(player);
                    }
                }
            }
        } else {
            event.setDamage(0);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        stats.setHp(stats.getMaxhp());

        // Create a list to store items that should be removed from the inventory
        List<ItemStack> itemsToRemove = new ArrayList<>();
        List<String> itemNamesToRemove = new ArrayList<>();

        // Iterate through the player's inventory
        if (stats.getLevel() > 2) {
            for (ItemStack item : player.getInventory()) {
                if (item != null && item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.hasLore()) {
                        for (String lore : meta.getLore()) {
                            if (lore.contains("Drop On Death")) {
                                // Drop the item on the ground without adding it to the event drops
                                player.getWorld().dropItemNaturally(player.getLocation(), item);
                                itemsToRemove.add(item); // Mark the item for removal from inventory
                                itemNamesToRemove.add("&e"+item.getAmount() + "x " + item.getItemMeta().getDisplayName() + "&r");
                                break;
                            }
                        }
                    }
                }
            }
            player.sendMessage(ColorChat.chat("&cItems you lost when killed... &r" + itemNamesToRemove));
        } else {
            player.sendMessage(ColorChat.chat("&cItems earned from Dungeon are dropped on death for player's Level above 2!"));
        }

        // Remove the marked items from the player's inventory
        for (ItemStack itemToRemove : itemsToRemove) {
            player.getInventory().remove(itemToRemove);
        }
        event.setCancelled(true);
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "spawn " + player.getName());
        player.sendMessage(ColorChat.chat("&4You have Died"));
    }

    private double getMobArmor(Entity entity) {
        ActiveMob mythicMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(entity);
        if (mythicMob == null) {
            return 1.0;
        }
        if (mythicMob.getArmor() > 1) {
            return mythicMob.getArmor();
        } else {
            return 1.0; // Default armor value if not using MythicMobs or no armor stat found
        }
    }
    private boolean isCritical(Player player)
    {
        return
                player.getFallDistance() > 0.0F &&
                        !player.isOnGround() &&
                        !player.isInsideVehicle() &&
                        !player.hasPotionEffect(PotionEffectType.BLINDNESS) &&
                        player.getLocation().getBlock().getType() != Material.LADDER &&
                        player.getLocation().getBlock().getType() != Material.VINE;
    }

    private boolean isSafezone(Location location) {
        if (location.getX() > -259 && location.getX() < 220 && location.getZ() >-419 && location.getZ() < 502) {
            return true;
        }
        return false;
    }
}
