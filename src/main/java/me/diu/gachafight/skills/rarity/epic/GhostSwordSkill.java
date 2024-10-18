package me.diu.gachafight.skills.rarity.epic;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.managers.SkillCooldownManager;
import me.diu.gachafight.skills.managers.SkillDamageSource;
import me.diu.gachafight.skills.utils.Skill;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GhostSwordSkill implements Skill {

    private final GachaFight plugin;
    private static FileConfiguration config;
    private static int cooldownDuration;
    private static int skillDuration;
    private static double damage;
    private static double attackRange;
    private static double movementSpeed;
    private static final List<ItemDisplay> activeGhostSwords = new ArrayList<>();

    public GhostSwordSkill(GachaFight plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "Skills/epic.yml");
        if (!configFile.exists()) {
            plugin.saveResource("Skills/epic.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        cooldownDuration = config.getInt("ghost sword.cooldown");
        skillDuration = config.getInt("ghost sword.duration");
        damage = config.getDouble("ghost sword.damage");
        attackRange = config.getDouble("ghost sword.attack_range");
        movementSpeed = config.getDouble("ghost sword.movement_speed");
    }

    @Override
    public void useSkill(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();

        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ColorChat.chat("&cSkill on cooldown! " + remainingTime + " seconds remaining."));
            return;
        }

        summonGhostSword(player);
        SkillCooldownManager.setCooldown(playerUUID, slot, cooldownDuration);
    }

    private void summonGhostSword(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        ItemDisplay ghostSword = (ItemDisplay) player.getWorld().spawnEntity(loc, EntityType.ITEM_DISPLAY);

        ItemStack swordItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta swordMeta = swordItem.getItemMeta();
        swordMeta.setCustomModelData(10009);
        swordItem.setItemMeta(swordMeta);
        ghostSword.setItemStack(swordItem);

        float baseScale = 2.5f;
        ghostSword.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(0, 0, 0, 1),
                new Vector3f(baseScale, baseScale, baseScale),
                new Quaternionf(0, 0, 0, 1)
        ));

        player.sendMessage(ColorChat.chat("&bGhost Sword summoned! It will attack nearby enemies for you."));

        new BukkitRunnable() {
            int ticks = 0;
            float angle = 0;
            boolean isSwinging = false;
            int attackCooldown = 0;
            final int ATTACK_COOLDOWN_MAX = 30;
            final double ATTACK_DISTANCE = 1.5;
            final float SWING_SPEED = 0.3f;
            final double IDLE_DISTANCE = 2.0;

            @Override
            public void run() {
                if (ticks >= skillDuration * 20 || !player.isOnline()) {
                    ghostSword.remove();
                    this.cancel();
                    return;
                }

                LivingEntity target = findNearestEnemy(player, attackRange);
                if (target != null) {
                    Vector direction = target.getLocation().add(0, 1, 0).subtract(ghostSword.getLocation()).toVector();
                    double distanceToTarget = direction.length();
                    direction.normalize();

                    if (distanceToTarget > ATTACK_DISTANCE) {
                        smoothTeleport(ghostSword, ghostSword.getLocation().add(direction.multiply(movementSpeed)));
                    }

                    if (attackCooldown == 0) {
                        if (!isSwinging) {
                            isSwinging = true;
                            angle = 0;
                            smoothUpdateSwordScale(ghostSword, baseScale * 1.2f);
                        }
                        angle += SWING_SPEED;
                        if (angle > Math.PI) {
                            angle = 0;
                            isSwinging = false;
                            double damageMultiplier = applySkillEffect(player, target);
                            target.damage(damage, SkillDamageSource.damageSource(player));
                            smoothUpdateSwordScale(ghostSword, baseScale);
                            attackCooldown = ATTACK_COOLDOWN_MAX;
                        }
                        updateSwordRotation(ghostSword, angle, direction);
                    } else {
                        float idleAngle = (float) (Math.sin(ticks * 0.05) * 0.2);
                        smoothUpdateSwordRotation(ghostSword, idleAngle, direction);
                        smoothUpdateSwordScale(ghostSword, baseScale);
                    }
                } else {
                    Location playerLoc = player.getLocation();
                    Vector playerDir = playerLoc.getDirection();
                    Vector rightSide = playerDir.getCrossProduct(new Vector(0, 1, 0)).normalize();
                    Location idleLocation = playerLoc.clone().add(rightSide.multiply(IDLE_DISTANCE)).add(0, 1.5, 0);

                    Vector toIdle = idleLocation.toVector().subtract(ghostSword.getLocation().toVector());
                    double distanceToIdle = toIdle.length();

                    if (distanceToIdle > 0.1) {
                        toIdle.normalize().multiply(Math.min(distanceToIdle, movementSpeed));
                        smoothTeleport(ghostSword, ghostSword.getLocation().add(toIdle));
                    }

                    float idleAngle = (float) (Math.sin(ticks * 0.05) * 0.2);
                    smoothUpdateSwordRotation(ghostSword, idleAngle, playerDir);
                    smoothUpdateSwordScale(ghostSword, baseScale);
                }

                if (attackCooldown > 0) {
                    attackCooldown--;
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // Smoothly teleport the sword with interpolation
    public static void smoothTeleport(ItemDisplay ghostSword, Location newLocation) {
        ghostSword.setTeleportDuration(1);
        ghostSword.teleport(newLocation);
    }
    public static void updateSwordRotation(ItemDisplay ghostSword, float angle, Vector direction) {
        Vector rotationAxis = direction.getCrossProduct(new Vector(0, 1, 0)).normalize();

        Quaternionf rotation = new Quaternionf().rotationAxis(-angle,
                (float) rotationAxis.getX(),
                (float) rotationAxis.getY(),
                (float) rotationAxis.getZ());

        Matrix4f matrix = new Matrix4f()
                .translate(ghostSword.getTransformation().getTranslation())
                .rotate(rotation)
                .scale(2f); // 10x scale

        ghostSword.setTransformationMatrix(matrix);
    }

    // Smoothly update the sword's rotation
    public static void smoothUpdateSwordRotation(ItemDisplay ghostSword, float angle, Vector direction) {
        Vector rotationAxis = direction.getCrossProduct(new Vector(0, 1, 0)).normalize();

        Quaternionf rotation = new Quaternionf().rotationAxis(-angle,
                (float) rotationAxis.getX(),
                (float) rotationAxis.getY(),
                (float) rotationAxis.getZ());

        Matrix4f matrix = new Matrix4f()
                .translate(ghostSword.getTransformation().getTranslation())
                .rotate(rotation)
                .scale(2f); // Example scale

        ghostSword.setTransformationMatrix(matrix);
        ghostSword.setInterpolationDuration(10); // Smooth over 10 ticks
        ghostSword.setInterpolationDelay(0); // No delay
    }

    // Smoothly update the sword's scale
    public static void smoothUpdateSwordScale(ItemDisplay ghostSword, float scale) {
        Matrix4f matrix = new Matrix4f()
                .translate(ghostSword.getTransformation().getTranslation())
                .rotate(ghostSword.getTransformation().getLeftRotation())
                .scale(2.5f * scale); // Example scale

        ghostSword.setTransformationMatrix(matrix);
        ghostSword.setInterpolationDuration(10); // Smooth scaling over 10 ticks
        ghostSword.setInterpolationDelay(0); // No delay
    }


    public static LivingEntity findNearestEnemy(Player player, double range) {
        LivingEntity nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (LivingEntity entity : player.getWorld().getLivingEntities()) {
            if (entity != player && entity.getLocation().distanceSquared(player.getLocation()) <= range * range) {
                double distanceSquared = entity.getLocation().distanceSquared(player.getLocation());
                if (distanceSquared < nearestDistanceSquared) {
                    nearest = entity;
                    nearestDistanceSquared = distanceSquared;
                }
            }
        }

        return nearest;
    }

    @Override
    public boolean hasActiveState() {
        return false;
    }

    @Override
    public boolean isSkillActive(Player player) {
        return false;
    }

    @Override
    public double applySkillEffect(Player player, LivingEntity target) {
        return 1.0; // No additional effect
    }

    @Override
    public void deactivateSkill(Player player) {
        // No deactivation needed
    }
    public static void removeAllGhostSwords() {
        for (ItemDisplay ghostSword : activeGhostSwords) {
            if (ghostSword != null && !ghostSword.isDead()) {
                ghostSword.remove();
            }
        }
        activeGhostSwords.clear();
    }
}
