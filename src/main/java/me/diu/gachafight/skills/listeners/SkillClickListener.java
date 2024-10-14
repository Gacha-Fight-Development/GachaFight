package me.diu.gachafight.skills.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.SkillSystem;
import me.diu.gachafight.skills.utils.SkillItemUtils;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class SkillClickListener implements Listener {
    private GachaFight plugin;

    public SkillClickListener(GachaFight plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSkillSlotClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        if (slot != 15 && slot != 16 && slot != 17) {
            // Checks clicked inventory and slots
            return;
        }
        if (!event.getClickedInventory().equals(event.getView().getBottomInventory())) {
            return;
        }
        event.setCancelled(true);

        ItemStack cursorItem = event.getCursor();
        ItemStack clickedItem = event.getView().getBottomInventory().getItem(slot);

        if (cursorItem == null || cursorItem.getType().isAir()) {
            if (SkillItemUtils.isNetheriteUpgradeTemplate(clickedItem)) {
                player.getInventory().addItem(clickedItem);
                event.getView().getBottomInventory().setItem(slot, null);
                SkillSystem.setupSkillSlots(player);
                player.closeInventory();
                player.sendMessage(ColorChat.chat("&aUnequipped a Skill"));
            } else if (clickedItem.getType().equals(Material.ITEM_FRAME)) {

            }
            else {
                player.getInventory().addItem(clickedItem);
                event.getView().getBottomInventory().setItem(slot, null);
                SkillSystem.setupSkillSlots(player);
                player.closeInventory();
            }
        }
        if (SkillItemUtils.isNetheriteUpgradeTemplate(cursorItem)) {
            if (SkillItemUtils.isNetheriteUpgradeTemplate(clickedItem)) {
                event.getView().getBottomInventory().setItem(slot, cursorItem);
                player.getInventory().addItem(clickedItem);
            } else {
                event.getView().getBottomInventory().setItem(slot, cursorItem);
                event.setCursor(new ItemStack(Material.AIR));
            }
            event.getView().getBottomInventory().setItem(slot, cursorItem);
        }
        event.getWhoClicked().getInventory().remove(Material.ITEM_FRAME);
        SkillSystem.setupSkillSlots(player);
        player.updateInventory();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        event.getPlayer().getInventory().remove(Material.ITEM_FRAME);
        Player player = (Player) event.getPlayer();
        SkillSystem.setupSkillSlots(player);
    }
}
