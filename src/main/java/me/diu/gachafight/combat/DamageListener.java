package me.diu.gachafight.combat;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitEntity;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.combat.mobdrops.BulbDeathReward;
import me.diu.gachafight.combat.mobdrops.GoblinDeathReward;
import me.diu.gachafight.combat.mobdrops.RPGDeathReward;
import me.diu.gachafight.dungeon.utils.DungeonUtils;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.quest.listeners.QuestKillListener;
import me.diu.gachafight.skills.managers.MobDropSelector;
import me.diu.gachafight.skills.managers.SkillManager;
import me.diu.gachafight.skills.utils.RandomSkillUtils;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.TextDisplayUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;


public class DamageListener implements Listener {

    public DamageListener(GachaFight plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (isSafezone(event.getEntity().getLocation())) {
            if (event.getEntity().getType() == EntityType.PLAYER || event.getEntity().getType() == EntityType.PIG) {
                event.setCancelled(true);
                event.getDamager().sendMessage(ColorChat.chat("&aSafezone. &cPvP only in Dungeon."));
                return;
            }
        }
        // when arrow hits player
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

    }

    // Handle Player vs Entity (PvE)
    private void handlePlayerVsEntity(EntityDamageByEntityEvent event, Player player, LivingEntity entity) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        // Retrieve player's damage stat
        double playerDamage = stats.getDamage() + stats.getWeaponStats().getDamage() + stats.getGearStats().getTotalDamage();
        if (DungeonUtils.isRPG(entity.getLocation())) {
            if (playerDamage > 15) {
                playerDamage = 15;
            }
        }
        // Checks for SKILL Damage
        if (event.getDamageSource().getDamageType().equals(DamageType.CACTUS)) {
            playerDamage *= event.getDamage();
        } else {
            playerDamage *= SkillManager.applyActiveSkills(player, entity);
        }
        double mobArmor = getMobArmor(entity);
        // ==============Calc PvE================
        double totalDamage = playerDamage - mobArmor; // armor
        if (totalDamage < 0.5) {
            totalDamage = 0.5;
        }
        double random = Math.random();
        if (random < stats.getCritChance()) {
            totalDamage *= stats.getCritDmg();
        }
        if (isCritical(player)) {
            totalDamage = totalDamage*1.2;
        }
        boolean isCrit = random < stats.getCritChance();
        TextDisplayUtils.summonDamageTextDisplay(entity, totalDamage, isCrit);
        // Check if the mob will die from this hit
        if (entity.getHealth() - totalDamage <= 0) {
            handleMobDeath(player, entity);  // Handle mob death, rewards, etc.
            QuestKillListener.questKillMob(player, entity); //handle quest
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
        if (DungeonUtils.isRPG(target.getLocation())) {
            if (attackerDamage > 15) {
                attackerDamage = 15;
            }
        }
        // Calculate the target's total armor
        double targetArmor = targetStats.getArmor() + targetStats.getGearStats().getTotalArmor();

        // Calculate the custom damage for PvP
        double totalDamage = attackerDamage - (targetArmor/4); // Adjust the armor effect as needed

        //Check for if player dodged
        if (Math.random() > targetStats.getDodge()) { //below triggers if not dodged
            //reduce damage for target if attacker level is above target's level
            if (attackerStats.getLevel() > targetStats.getLevel()) {
                double levelDiff = attackerStats.getLevel() - targetStats.getLevel();
                //sets a minimum of 10% damage
                if (levelDiff >= 10) {
                    levelDiff = 0.5;
                }
                totalDamage = totalDamage*(1-(levelDiff*0.07));
            }
            //sets minimum damage of 0.5
            if (totalDamage < 0.5) {
                totalDamage = 0.5;
            }
            double random = Math.random();
            if (random < attackerStats.getCritChance()) {
                totalDamage *= attackerStats.getCritDmg();
            }
            if (isCritical(attacker)) {
                totalDamage = totalDamage*1.2;
            }
            boolean isCrit = random < attackerStats.getCritChance();
            TextDisplayUtils.summonDamageTextDisplay(target, totalDamage, isCrit);
        } else { //below triggers when attack dodged
            totalDamage = 0;
            target.sendMessage(ColorChat.chat("&aDodged!"));
        }
        // Apply custom damage to the target
        event.setDamage(0); // Cancel the default damage
        if (attacker.hasPermission("gachafight.toggledamage")) {
            attacker.sendMessage(ColorChat.chat("&cFinal Damage: &f" + totalDamage));
        }
        targetStats.setHp(targetStats.getHp() - totalDamage);
        targetStats.syncHealthWithHearts(target);

        // Check if the target will die from this hit
        if (targetStats.getHp()+targetStats.getGearStats().getTotalMaxHp() <= 0) {
            target.setHealth(0); // Trigger death event
            target.sendMessage(ColorChat.chat("&4You have Died to " + attacker.getName()));
        }
    }

    //Handles EvP Interactions | When Entity Hits Player. (Only Applies to MythicMobs)
    @EventHandler
    public void onMobDamage(MythicDamageEvent event) { //damage by mob
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
                    double totalDamage = mobDamage - (playerArmor * 0.4); // Calc EvP

                    //Check for if player dodged
                    if (Math.random() > stats.getDodge()) { //below triggers if not dodged
                        //sets minimum damage of 0.5
                        if (totalDamage < 0.5) {
                            totalDamage = 0.5;
                        }
                        double random = Math.random();
                        if (random < 0.15) {
                            totalDamage *= 1.5;
                        }
                    } else { //below triggers when attack dodged
                        totalDamage = 0;
                        player.sendMessage(ColorChat.chat("&aDodged!"));
                    }


                    // Cancel the default damage and apply custom damage
                    event.setDamage(0);
                    stats.setHp(stats.getHp() - totalDamage);
                    stats.syncHealthWithHearts(player);
                    if (stats.getHp()+stats.getGearStats().getTotalMaxHp() <= 0) {
                        player.setHealth(0); // This will trigger the death event
                        stats.syncHealthWithHearts(player);
                        stats.updateActionbar(player);
                        player.sendMessage(ColorChat.chat("&4You have Died to " + mythicMob.getName()));
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

    // New method to handle mob death and give rewards
    private void handleMobDeath(Player player, Entity entity) {
        if (entity instanceof LivingEntity) {
            double mobHp = ((LivingEntity) entity).getMaxHealth();
            double expGained = mobHp / 7.5;
            double moneyGained = mobHp / 20;
            double rankMulti = 1;
            // Add EXP & $ to the player
            PlayerStats playerStats = PlayerStats.getPlayerStats(player);
            if (player.hasPermission("gacha.vip")) {
                rankMulti = 1.2;
            }
            playerStats.addExp( expGained * rankMulti, player);
            playerStats.setMoney(playerStats.getMoney() + (moneyGained*rankMulti));


            if (entity.getName().contains("Goblin")) {
                GoblinDeathReward.MobDeath(entity.getName(), player);
            } else if (entity.getName().contains("rpg")) {
                RPGDeathReward.MobDeath(entity.getName(), player);
            } else if (entity.getName().contains("Bulb")) {
                BulbDeathReward.MobDeath(entity.getName(), player);
            }
            if (entity.getName().equalsIgnoreCase(MobDropSelector.getMob())) {
                if (Math.random() < 0.0002) {
                    player.getInventory().addItem(MobDropSelector.getDrop(player));
                }
            }
            if (Math.random() < ((double) 1 /258)) {
                player.getInventory().addItem(RandomSkillUtils.getRandomCommonSkill());
                player.sendMessage(ColorChat.chat("&a&l Received &f&lCommon &a&lSkill!"));
            }
            if (Math.random() < ((double) 1/512)) {
                player.getInventory().addItem(RandomSkillUtils.getRandomUncommonSkill());
                player.sendMessage(ColorChat.chat("&a&l Received &7&lUncommon &a&lSkill!"));
            }
            if (Math.random() < 0.001) {
                MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob("bulb_lilypad_pet").orElse(null);
                ActiveMob lilypad = mob.spawn(BukkitAdapter.adapt(entity.getLocation()),1);
                player.sendMessage(ColorChat.chat("&a&lLilypad Bulb Spawned!"));
                player.sendMessage(ColorChat.chat("&aKill it before it despawn! (60s)"));
                new BukkitRunnable() {
                    public void run() {
                        lilypad.despawn();
                    }
                }.runTaskLater(GachaFight.getInstance(), 1200);
            }

            // Notify the player of the exp and money gained
            player.sendActionBar(MiniMessage.miniMessage().deserialize("<green>+ <dark_aqua>Exp: <aqua>" + String.format("%.2f", expGained) + "<black> | <gold> Money: <yellow>" + String.format("%.2f", moneyGained) + "</green>"));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        stats.setHp(stats.getMaxhp());
        //clear Damage Indicator
        TextDisplay display = TextDisplayUtils.activeDisplays.get(player.getUniqueId());
        if (display != null) {
            display.remove();
        }
        // Create a list to store items that should be removed from the inventory
        List<ItemStack> itemsToRemove = new ArrayList<>();
        List<String> itemNamesToRemove = new ArrayList<>();
        if (stats.getLevel() > 2) {
            for (ItemStack item : player.getInventory()) {
                if (item != null && item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.hasLore()) {
                        for (String lore : meta.getLore()) {
                            if (lore.contains("Drop On Death")) {
                                player.getWorld().dropItemNaturally(player.getLocation(), item);
                                itemsToRemove.add(item);
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
    }
    // ===============Grabs Mythic Mobs Armor Value================
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
    // ===============Check For Vanilla Crit===============
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

    public static boolean isSafezone(Location location) {
        // ===================Spawn==================
        if (location.getX() > -259 && location.getX() < 220 && location.getZ() >-419 && location.getZ() < 502) {
            return true;
        }
        // =================Tutorial================
        if (location.getX() > -766 && location.getX() < -597 && location.getZ() > 30 && location.getZ() < 101) {
            return true;
        }
        // ===================AFK====================
        if (location.getX() > -12 && location.getX() < -3 && location.getZ() > 326 && location.getZ() < 335) {
            return true;
        }
        return false;
    }

    public static void handleFireTicks() {
        new BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (player.isVisualFire()) {
                        player.sendMessage("a");
                        PlayerStats stats = PlayerStats.getPlayerStats(player);
                        player.damage(0);
                        double damage = stats.getMaxhp()/40;
                        stats.setHp(stats.getHp()-damage);
                    }
                }
            }
        }.runTaskTimer(GachaFight.getInstance(), 20, 60);
    }
}
