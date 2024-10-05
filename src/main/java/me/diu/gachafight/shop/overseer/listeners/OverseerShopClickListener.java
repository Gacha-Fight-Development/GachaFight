package me.diu.gachafight.shop.overseer.listeners;

import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.shop.overseer.gui.OverseerShopGUI;
import me.diu.gachafight.utils.Calculations;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class OverseerShopClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        Player player = (Player) event.getWhoClicked();

        // Check if the clicked inventory is the "Overseer" GUI
        if (event.getView().getTitle().contains("Overseer")) {
            event.setCancelled(true);  // Prevent normal click behavior

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;  // No item clicked
            }

            switch (clickedItem.getType()) {
                case RED_DYE:
                    increaseHP(player);
                    break;
                case ORANGE_DYE:
                    increaseDamage(player);
                    break;
                case LIME_DYE:
                    increaseArmor(player);
                    break;
                case YELLOW_DYE:
                    if (!player.hasPermission("gachafight.glowstone")) {
                        player.sendMessage("Not Implemented yet");
                        return;
                    }
                    increaseCritChance(player);
                    break;
                case GLOWSTONE_DUST:
                    if (!player.hasPermission("gachafight.glowstone")) {
                        player.sendMessage("Not Implemented yet");
                        return;
                    }
                    increaseCritDamage(player);
                    break;
                case SUGAR:
                    if (!player.hasPermission("gachafight.glowstone")) {
                        player.sendMessage("Not Implemented yet");
                        return;
                    }
                    increaseSpeed(player);
                    break;
                case LIGHT_GRAY_DYE:
                    if (!player.hasPermission("gachafight.glowstone")) {
                        player.sendMessage("Not Implemented yet");
                        return;
                    }
                    increaseDodgeChance(player);
                    break;
                case COMPASS:
                    event.getWhoClicked().closeInventory();  // Close the inventory on 'back'
                    break;
            }
            OverseerShopGUI.openOverseerGUI(player);
        }
    }

    // Stub methods for increasing stats
    private void increaseHP(Player player) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        double cost = Calculations.overseerHPCost(stats.getHp());
        if (stats.getMoney() >= cost) {
            stats.setMoney(stats.getMoney() - cost);
            stats.setMaxhp(stats.getMaxhp()+0.1);
            player.sendMessage("HP increased!");
        } else {
            player.sendMessage(ColorChat.chat("&eNot Enough Money"));
        }
    }

    private void increaseDamage(Player player) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        double cost = Calculations.overseerDamageCost(stats.getDamage());
        if (stats.getMoney() >= cost) {
            stats.setMoney(stats.getMoney() - cost);
            stats.setDamage(stats.getDamage()+0.1);
            player.sendMessage(ColorChat.chat("&a+ &&cDamage"));
        } else {
            player.sendMessage(ColorChat.chat("&eNot Enough Money"));
        }

    }

    private void increaseArmor(Player player) {
        // Your logic to increase armor
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        double cost = Calculations.overseerArmorCost(stats.getArmor());
        if (stats.getMoney() >= cost) {
            stats.setMoney(stats.getMoney() - cost);
            stats.setArmor(stats.getArmor()+0.1);
            player.sendMessage("Armor increased!");
        } else {
            player.sendMessage(ColorChat.chat("&eNot Enough Money"));
        }
    }

    private void increaseCritChance(Player player) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        double cost = Calculations.overseerCritChanceCost(stats.getCritChance());
        if (stats.getMoney() >= cost) {
            stats.setMoney(stats.getMoney() - cost);
            stats.setArmor(stats.getArmor()+0.1);
            player.sendMessage("Crit Rate increased!");
        } else {
            player.sendMessage(ColorChat.chat("&eNot Enough Money"));
        }
    }

    private void increaseCritDamage(Player player) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        double cost = Calculations.overseerCritChanceCost(stats.getCritDmg());
        if (stats.getMoney() >= cost) {
            stats.setMoney(stats.getMoney() - cost);
            stats.setArmor(stats.getArmor()+0.1);
            player.sendMessage("Crit Damage increased!");
        } else {
            player.sendMessage(ColorChat.chat("&eNot Enough Money"));
        }
    }

    private void increaseSpeed(Player player) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        double cost = Calculations.overseerCritChanceCost(stats.getSpeed());
        if (stats.getMoney() >= cost) {
            stats.setMoney(stats.getMoney() - cost);
            stats.setArmor(stats.getSpeed()+0.1);
            player.sendMessage("Speed increased!");
        } else {
            player.sendMessage(ColorChat.chat("&eNot Enough Money"));
        }
    }

    private void increaseDodgeChance(Player player) {
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        double cost = Calculations.overseerCritChanceCost(stats.getDodge());
        if (stats.getMoney() >= cost) {
            stats.setMoney(stats.getMoney() - cost);
            stats.setArmor(stats.getArmor()+0.1);
            player.sendMessage("Crit Rate increased!");
        } else {
            player.sendMessage(ColorChat.chat("&eNot Enough Money"));
        }
    }
}
