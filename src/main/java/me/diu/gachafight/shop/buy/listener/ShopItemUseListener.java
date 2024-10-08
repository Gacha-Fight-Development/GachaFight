package me.diu.gachafight.shop.buy.listener;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShopItemUseListener implements Listener {

    public ShopItemUseListener(GachaFight plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.getType().equals(Material.IRON_INGOT) && item.getItemMeta().getCustomModelData() == 10003) {
            event.setCancelled(true);
            if (item.getItemMeta().getDisplayName().contains(ColorChat.chat("&e/AutoSellGacha"))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),"lp user " + player.getName() + " permission set " + "gacha.autosell");
                item.setAmount(item.getAmount() - 1);
                player.sendMessage(ColorChat.chat("&aUnlocked /AutoSellGacha"));
            }
            if (item.getItemMeta().getDisplayName().contains(ColorChat.chat("&e/Shop"))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),"lp user " + player.getName() + " permission set " + "gacha.shop");
                item.setAmount(item.getAmount() - 1);
                player.sendMessage(ColorChat.chat("&aUnlocked /Shop"));
            }
        }
    }
}
