package me.diu.gachafight.skills.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.managers.SkillManager;
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
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (!isSword(handItem) || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        boolean isSneaking = player.isSneaking();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            useSkill(player, isSneaking ? 16 : 15); //if sneaking 16 else 15
        } else if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) && isSneaking) {
            useSkill(player, 17);
        }
    }

    private void useSkill(Player player, int slot) {
        ItemStack skillItem = player.getInventory().getItem(slot);
        if (skillItem == null || skillItem.getType() == Material.ITEM_FRAME) {
            player.sendMessage("No skill assigned to this slot.");
            return;
        }

        ItemMeta skillMeta = skillItem.getItemMeta();
        if (skillMeta == null) {
            return;
        }

        String displayName = ChatColor.stripColor(skillMeta.getDisplayName());
        SkillManager.useSkill(player, displayName, slot);

        String actionDescription = switch (slot) {
            case 15 -> "Right-click with sword";
            case 16 -> "Shift + Right-click with sword";
            case 17 -> "Shift + Left-click with sword";
            default -> "Unknown action";
        };
        player.sendMessage("Using skill from slot " + slot + " (" + actionDescription + ")!");
    }

    private boolean isSword(ItemStack item) {
        if (item == null) return false;
        Material material = item.getType();
        return material == Material.WOODEN_SWORD || material == Material.STONE_SWORD ||
                material == Material.IRON_SWORD || material == Material.GOLDEN_SWORD ||
                material == Material.DIAMOND_SWORD || material == Material.NETHERITE_SWORD;
    }
}
