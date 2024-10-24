package me.diu.gachafight.skills.rarity.uncommon;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.managers.SkillCooldownManager;
import me.diu.gachafight.skills.utils.Skill;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class SwiftyHandsSkill implements Skill {

    private final GachaFight plugin;
    private int cooldown;
    private double radius;

    public SwiftyHandsSkill(GachaFight plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "Skills/uncommon.yml");
        if (!configFile.exists()) {
            plugin.saveResource("Skills/uncommon.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        cooldown = config.getInt("swifty hands.cooldown", 5);
        radius = config.getDouble("swifty hands.radius", 5.0);
    }

    @Override
    public void useSkill(Player player, int slot) {
        UUID playerUUID = player.getUniqueId();

        if (SkillCooldownManager.isOnCooldown(playerUUID, slot)) {
            long remainingTime = SkillCooldownManager.getRemainingCooldown(playerUUID, slot);
            player.sendMessage(ChatColor.RED + "Skill on cooldown! " + remainingTime + " seconds remaining.");
            return;
        }

        int itemsCollected = collectNearbyItems(player);

        if (itemsCollected > 0) {
            player.sendMessage(ChatColor.GREEN + "Swifty Hands collected " + itemsCollected + " items!");
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
        } else {
            player.sendMessage(ChatColor.YELLOW + "No items nearby to collect!");
        }

        SkillCooldownManager.setCooldown(playerUUID, slot, cooldown);
    }

    @Override
    public double applySkillEffect(Player player, LivingEntity target) {
        // This skill doesn't modify damage or target entities
        return 1.0;
    }

    private int collectNearbyItems(Player player) {
        int itemsCollected = 0;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Item) {
                Item item = (Item) entity;
                ItemStack stack = item.getItemStack();
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
                if (leftover.isEmpty()) {
                    item.remove();
                    itemsCollected++;
                } else {
                    stack.setAmount(leftover.get(0).getAmount());
                    item.setItemStack(stack);
                }
            }
        }
        return itemsCollected;
    }

    @Override
    public boolean hasActiveState() {
        return false; // This skill doesn't have an active state
    }
}
