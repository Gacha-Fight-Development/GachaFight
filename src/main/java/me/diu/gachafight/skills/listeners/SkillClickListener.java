package me.diu.gachafight.skills.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.SkillSystem;
import me.diu.gachafight.skills.utils.ItemUtils;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SkillClickListener implements Listener {
    private GachaFight plugin;

    public SkillClickListener(GachaFight plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSkillSlotClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!player.hasPermission("gacha.dev")) {
            return;
        }
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
            if (ItemUtils.isNetheriteUpgradeTemplate(clickedItem)) {
                player.getInventory().addItem(clickedItem);
                event.getView().getBottomInventory().setItem(slot, null);
                SkillSystem.setupSkillSlots(player);
                player.closeInventory();
                player.sendMessage(ColorChat.chat("&aUnequipped a Skill"));
            } else if (clickedItem.getType().equals(Material.ITEM_FRAME)) {
                event.setCursor(new ItemStack(Material.AIR));
            }
        }
        if (ItemUtils.isNetheriteUpgradeTemplate(cursorItem)) {
            if (ItemUtils.isNetheriteUpgradeTemplate(clickedItem)) {
                player.sendMessage("A1");
                event.getView().getBottomInventory().setItem(slot, cursorItem);
                player.getInventory().addItem(clickedItem);
            } else {
                player.sendMessage("A2");
                event.getView().getBottomInventory().setItem(slot, cursorItem);
                event.setCursor(new ItemStack(Material.AIR));
                //event.setCursor(null);
                //event.getWhoClicked().setItemOnCursor(null);
                //event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
                //event.setCursor(new ItemStack(Material.AIR));
                //player.setItemOnCursor(null);
                //player.setItemOnCursor(new ItemStack(Material.AIR));
            }
            event.getView().getBottomInventory().setItem(slot, cursorItem);
        }
        player.updateInventory();
    }
}
