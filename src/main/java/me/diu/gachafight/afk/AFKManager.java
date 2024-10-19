package me.diu.gachafight.afk;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.hooks.PlaceholderAPIHook;
import me.diu.gachafight.hooks.VaultHook;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.skills.rarity.epic.GhostSwordSkill;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.GiveItemUtils;
import me.diu.gachafight.utils.TextDisplayUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AFKManager {
    public static final HashMap<UUID, BukkitRunnable> afkTasks = new HashMap<>();
    public static final HashMap<UUID, ItemDisplay> afkSwords = new HashMap<>();

    public static void startAFKSession(Player player) {
        if (isInAFKZone(player.getLocation())) {
            summonAFKSword(player);
        }
    }

    public static void stopAFKSession(OfflinePlayer player) {
        if (afkTasks.containsKey(player.getUniqueId()) || afkSwords.containsKey(player.getUniqueId())) {
            UUID playerUUID = player.getUniqueId();
            if (afkSwords.containsKey(playerUUID)) {
                ItemDisplay sword = afkSwords.get(playerUUID);
                if (sword != null && !sword.isDead()) {
                    System.out.println(sword);
                    sword.remove();
                }
                afkSwords.remove(playerUUID);
            }
            if (afkTasks.containsKey(playerUUID)) {
                BukkitRunnable task = afkTasks.get(playerUUID);
                task.cancel();
                afkTasks.remove(playerUUID);
            }

        }
    }

    public static void giveAFKReward(Player player, double damage) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);

        // changing keyChance Calculation Requires you to Change PlaceholderAPIHook.getAFKRewardAsync()
        double keyChance = Math.min(damage / 1000, 0.125);

        String rewardMessage = "";
        double rareKeyChance = 0;
        double uncommonKeyChance = 0;
        double commonKeyChance = 0;

        if (keyChance >= 0.1) {
            rareKeyChance = Math.max(0.001, keyChance - 0.1);
            if (Math.random() < rareKeyChance) {
                GiveItemUtils.giveRareKey(player, 1);
                rewardMessage += ColorChat.chat("&aRare Key: " + String.format("%.2f", rareKeyChance * 100) + "%");
            }
        } else if (keyChance >= 0.05) {
            uncommonKeyChance = Math.max(0.001, keyChance - 0.05);
            if (Math.random() < uncommonKeyChance) {
                GiveItemUtils.giveUncommonKey(player, 1);
                rewardMessage += ColorChat.chat("&aUncommon Key: " + String.format("%.2f", uncommonKeyChance * 100) + "%");
            }
        } else if (keyChance >= 0.001) {
            commonKeyChance = keyChance;
            if (Math.random() < commonKeyChance) {
                GiveItemUtils.giveCommonKey(player, 1);
                rewardMessage += ColorChat.chat("&aCommon Key: " + String.format("%.2f", commonKeyChance * 100) + "%");
            }
        }


        if (keyChance >= 0.1) {
            if (Math.random() < Math.max(0.001, keyChance - 0.1)) {
                GiveItemUtils.giveRareKey(player, 1);
            }
        } else if (keyChance >= 0.05) {
            if (Math.random() < Math.max(0.001, keyChance - 0.05)) {
                GiveItemUtils.giveUncommonKey(player, 1);
            }
        } else if (keyChance >= 0.001) {
            if (Math.random() < keyChance) {
                GiveItemUtils.giveCommonKey(player, 1);
            }
        }

        // changing goldAmount Calculation Requires you to Change PlaceholderAPIHook.getAFKRewardAsync()
        double goldAmount = Math.random() * (damage/18);
        VaultHook.addMoney(player, goldAmount);

        // changing expAmount Calculation Requires you to Change PlaceholderAPIHook.getAFKRewardAsync()
        double expAmount = stats.getLevel() * 0.05;
        stats.setExp(stats.getExp() + expAmount);
        // Inform the player about the rewards
        player.sendActionBar(MiniMessage.miniMessage().deserialize("<green>+ <dark_aqua>Exp: <aqua>" + String.format("%.2f", expAmount) + "<black> | <gold> Gold: <yellow>" + String.format("%.2f", goldAmount) + "</green>"));

    }

    public static boolean isInAFKZone(Location loc) {
        return loc.getBlockX() >= -772 && loc.getBlockX() <= -680 &&
                loc.getBlockY() >= 0 && loc.getBlockY() <= 46 &&
                loc.getBlockZ() >= -104 && loc.getBlockZ() <= -24;
    }

    public static DamageSource damageSource(Player player) {
        return DamageSource.builder(DamageType.SWEET_BERRY_BUSH)
                .withDirectEntity(player)
                .build();
    }

    public static void summonAFKSword(Player player) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        final double ATTACK_RANGE = 35.0;
        final double MOVEMENT_SPEED = 0.5;

        Location loc = player.getLocation().add(0, 1, 0);
        ItemDisplay afkSword = (ItemDisplay) player.getWorld().spawnEntity(loc, EntityType.ITEM_DISPLAY);

        ItemStack swordItem = player.getInventory().getItemInMainHand();
        afkSword.setItemStack(swordItem);

        float baseScale = 0.2f;
        afkSword.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(0, 0, 0, 1),
                new Vector3f(baseScale, baseScale, baseScale),
                new Quaternionf(0, 0, 0, 1)
        ));

        player.sendMessage(ColorChat.chat("&bAFK Sword summoned! It will attack nearby enemies for you."));

        BukkitRunnable afkSwordTask = new BukkitRunnable() {
            int ticks = 0;
            float angle = 0;
            boolean isSwinging = false;
            int attackCooldown = 0; //do not change
            final int ATTACK_COOLDOWN_MAX = 80;
            final double ATTACK_DISTANCE = 1.5;
            final float SWING_SPEED = 0.3f;
            final double IDLE_DISTANCE = 2.0;

            @Override
            public void run() {
                if (!player.isOnline() || !AFKManager.isInAFKZone(player.getLocation())) {
                    stopAFKSession(player);
                    this.cancel();
                    return;
                }

                LivingEntity target = AFKManager.findNearestEnemy(player, ATTACK_RANGE);
                if (target != null) {
                    Vector direction = target.getLocation().add(0, 1, 0).subtract(afkSword.getLocation()).toVector();
                    double distanceToTarget = direction.length();
                    direction.normalize();

                    if (distanceToTarget > ATTACK_DISTANCE) {
                        GhostSwordSkill.smoothTeleport(afkSword, afkSword.getLocation().add(direction.multiply(MOVEMENT_SPEED)));
                    }

                    if (attackCooldown == 0) {
                        if (!isSwinging) {
                            isSwinging = true;
                            angle = 0;
                            GhostSwordSkill.smoothUpdateSwordScale(afkSword, baseScale * 1.2f);
                        }
                        angle += SWING_SPEED;
                        if (angle > Math.PI) {
                            angle = 0;
                            isSwinging = false;
                            handleAFKDummyDamage(player, afkSword, stats);
                            GhostSwordSkill.smoothUpdateSwordScale(afkSword, baseScale);
                            attackCooldown = ATTACK_COOLDOWN_MAX;
                        }
                        GhostSwordSkill.updateSwordRotation(afkSword, angle, direction);
                    } else {
                        float idleAngle = (float) (Math.sin(ticks * 0.05) * 0.2);
                        GhostSwordSkill.smoothUpdateSwordRotation(afkSword, idleAngle, direction);
                        GhostSwordSkill.smoothUpdateSwordScale(afkSword, baseScale);
                    }
                } else {
                    Location playerLoc = player.getLocation();
                    Vector playerDir = playerLoc.getDirection();
                    Vector rightSide = playerDir.getCrossProduct(new Vector(0, 1, 0)).normalize();
                    Location idleLocation = playerLoc.clone().add(rightSide.multiply(IDLE_DISTANCE)).add(0, 1.5, 0);

                    Vector toIdle = idleLocation.toVector().subtract(afkSword.getLocation().toVector());
                    double distanceToIdle = toIdle.length();

                    if (distanceToIdle > 0.1) {
                        toIdle.normalize().multiply(Math.min(distanceToIdle, MOVEMENT_SPEED));
                        GhostSwordSkill.smoothTeleport(afkSword, afkSword.getLocation().add(toIdle));
                    }

                    float idleAngle = (float) (Math.sin(ticks * 0.05) * 0.2);
                    GhostSwordSkill.smoothUpdateSwordRotation(afkSword, idleAngle, playerDir);
                    GhostSwordSkill.smoothUpdateSwordScale(afkSword, baseScale);
                }

                if (attackCooldown > 0) {
                    attackCooldown--;
                }

                ticks++;
            }
        };
        AFKManager.afkTasks.put(player.getUniqueId(), afkSwordTask);
        afkSwords.put(player.getUniqueId(), afkSword);
        afkSwordTask.runTaskTimer(GachaFight.getInstance(), 0L, 1L);
    }

    public static LivingEntity findNearestEnemy(Player player, double range) {
        LivingEntity nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (LivingEntity entity : player.getWorld().getLivingEntities()) {
            if (!(entity instanceof Player) && entity != player && entity.getLocation().distanceSquared(player.getLocation()) <= range * range) {
                double distanceSquared = entity.getLocation().distanceSquared(player.getLocation());
                if (distanceSquared < nearestDistanceSquared) {
                    nearest = entity;
                    nearestDistanceSquared = distanceSquared;
                }
            }
        }
        return nearest;
    }

    public static void handleAFKDummyDamage(Player player, Entity entity, PlayerStats stats) {
        // Calculate player's damage
        double playerDamage = stats.getDamage() + stats.getWeaponStats().getDamage() + stats.getGearStats().getTotalDamage();

        // Calculate total damage
        double totalDamage = playerDamage;

        // Check for critical hit
        boolean isCrit = Math.random() < stats.getCritChance();
        if (isCrit) {
            totalDamage *= stats.getCritDmg();
        }
        // Give reward to player (assuming you have a method to add currency)
        giveAFKReward(player, totalDamage);

        // Display damage and reward information
        TextDisplayUtils.summonDamageTextDisplay(entity, totalDamage, isCrit);

        // Play sound effect (optional)
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    public static void cancelAllAFKTasks() {
        for (BukkitRunnable task : afkTasks.values()) {
            task.cancel();
        }
        afkTasks.clear();
    }
    public static void stopAllAFKSessions() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            // Stop the AFK session for this player
            System.out.println(afkTasks);
            System.out.println(afkSwords);
            System.out.println(player.getUniqueId());
            stopAFKSession(player);
        }

        Bukkit.getLogger().info("All AFK sessions have been stopped.");
    }

    public static void updateAFKSwordItem(Player player, ItemStack item) {
        ItemDisplay afkSword = afkSwords.get(player.getUniqueId());
        if (afkSword != null && !afkSword.isDead()) {
            if (item == null || item.getType().isAir()) {
                // If the player's hand is empty, use a default item (e.g., a stick)
                item = new ItemStack(Material.STICK);
            }
            afkSword.setItemStack(item);
        }
    }

}
