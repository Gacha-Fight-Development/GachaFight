package me.diu.gachafight.skills.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.rarity.SkillList;
import me.diu.gachafight.skills.rarity.common.SwordChargeSkill;
import me.diu.gachafight.skills.rarity.common.SwordSlashSkill;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SkillUseListener implements Listener {
    private final GachaFight plugin;

    public SkillUseListener(GachaFight plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand(); // Get the item in the player's main hand

        // Check if the player is holding any type of sword
        if (!isSword(handItem)) {
            return; // Exit if the player is not holding a sword
        }
        if (!player.hasPermission("op")) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // Ignore offhand interactions
        }

        // Check for right-click, shift-right-click, and shift-left-click actions
        Action action = event.getAction();
        boolean isSneaking = player.isSneaking();

        // Right-click with any sword -> trigger skill in slot 15
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (!isSneaking) {
                useSkill(player, 15); // Trigger skill in slot 15
            } else {
                useSkill(player, 16); // Trigger skill in slot 16 (shift + right click)
            }
        }

        // Left-click while sneaking -> trigger skill in slot 17
        if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) && isSneaking) {
            useSkill(player, 17); // Trigger skill in slot 17 (shift + left click)
        }
    }

    // Method to trigger the skill based on the player's inventory slot
    private void useSkill(Player player, int slot) {
        ItemStack skillItem = player.getInventory().getItem(slot);
        ItemMeta skillMeta = skillItem.getItemMeta();

        // If there is no item in the skill slot, do nothing
        if (skillItem == null || skillItem.getType() == Material.ITEM_FRAME) {
            player.sendMessage("No skill assigned to this slot.");
            return;
        }

        String displayName = ChatColor.stripColor(skillMeta.getDisplayName());
        // Here, you can define how each skill works based on the item in the slot
        // For now, we will just send a message to the player and simulate skill activation.
        switch (slot) {
            case 15:
                SkillList.skillCheck(player, displayName, slot);
                player.sendMessage("Using skill from slot 15 (Right-click with sword)!");
                // Add actual skill logic here
                break;
            case 16:
                SkillList.skillCheck(player, displayName, slot);
                player.sendMessage("Using skill from slot 16 (Shift + Right-click with sword)!");
                // Add actual skill logic here
                break;
            case 17:
                SkillList.skillCheck(player, displayName, slot);
                player.sendMessage("Using skill from slot 17 (Shift + Left-click with sword)!");
                // Add actual skill logic here
                break;
            default:
                break;
        }
    }
    private boolean isSword(ItemStack item) {
        if (item == null) return false;
        Material material = item.getType();
        return material == Material.WOODEN_SWORD || material == Material.STONE_SWORD ||
                material == Material.IRON_SWORD || material == Material.GOLDEN_SWORD ||
                material == Material.DIAMOND_SWORD || material == Material.NETHERITE_SWORD;
    }
}
