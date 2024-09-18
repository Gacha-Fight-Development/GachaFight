package me.diu.gachafight.playerstats;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.display.Blocks;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.ExtractLore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerStatsListener implements Listener {

    private final GachaFight plugin;

    public PlayerStatsListener(GachaFight plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PlayerStats playerStats = PlayerStats.getPlayerStats(player);

        // Sync health and update actionbar when switching items
        playerStats.syncHealthWithHearts(player);
        playerStats.updateActionbar(player); // Update the actionbar with current health

        // Get the item the player is switching to
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());

        // If the new item is a weapon, update weapon stats
        if (newItem != null && isWeapon(newItem)) {
            updateWeaponStats(playerStats, newItem);
        } else {
            // If the new item is not a weapon (or air), clear the weapon stats
            clearWeaponStats(playerStats);
        }
    }


    // Handle armor changes
    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        PlayerStats playerStats = PlayerStats.getPlayerStats(event.getPlayer());
        playerStats.syncHealthWithHearts(event.getPlayer());
        playerStats.updateActionbar(event.getPlayer()); // Update the actionbar with current health
        updateSpecificGearStats(playerStats, event.getNewItem(), event.getSlotType());
    }


    // Handle item drops
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
        event.getPlayer().sendMessage(ColorChat.chat("&cUse /Disposal to trash items"));
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        PlayerStats playerStats = PlayerStats.getPlayerStats(event.getPlayer());
        ItemStack pickedUpItem = event.getItem().getItemStack();

        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if the item picked up is a weapon before applying stats
                if (isWeapon(pickedUpItem) && event.getPlayer().getItemInHand().equals(pickedUpItem)) {
                    updateWeaponStats(playerStats, pickedUpItem);
                    playerStats.syncHealthWithHearts(event.getPlayer());
                    playerStats.updateActionbar(event.getPlayer()); // Update the actionbar with current health
                }
            }
        }.runTaskLater(plugin, (long) 0.1);
    }

    @EventHandler
    public void onOffhandItemSwitch(PlayerSwapHandItemsEvent event) {
        PlayerStats playerStats = PlayerStats.getPlayerStats(event.getPlayer());
        if (event.getOffHandItem() != null) {
            clearOffhandStats(playerStats);
            updateOffhandStats(playerStats, event.getOffHandItem());
        } else {
            clearOffhandStats(playerStats);
        }
        if (event.getMainHandItem() != null && isWeapon(event.getMainHandItem())) {
            updateWeaponStats(playerStats, event.getMainHandItem());
        } else {
            // If the new item is not a weapon (or air), clear the weapon stats
            clearWeaponStats(playerStats);
        }
    }

    // Handle inventory item clicks (to manage item changes through inventory)
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        PlayerStats playerStats = PlayerStats.getPlayerStats(player);

        ItemStack currentItem = event.getCurrentItem(); // The item in the slot that was clicked
        ItemStack cursorItem = event.getCursor(); // The item on the cursor (what is being moved)

        // Handle item moving out of the main hand
        if (event.getSlot() == player.getInventory().getHeldItemSlot()) {
            if (currentItem != null && isWeapon(currentItem)) {
                updateWeaponStats(playerStats, currentItem);
            }
        }

        // Handle item moving out of the offhand
        if (event.getClick() == ClickType.SWAP_OFFHAND || event.getSlot() == 40) {
            if (currentItem != null) {
                clearOffhandStats(playerStats);
                updateOffhandStats(playerStats, cursorItem);
            } else {
                clearOffhandStats(playerStats);
            }
        }

        // Handle item moving into the main hand
        if (event.getSlot() == player.getInventory().getHeldItemSlot()) {
            if (cursorItem != null && isWeapon(cursorItem)) {
                clearWeaponStats(playerStats);
                updateWeaponStats(playerStats, cursorItem);
            }
        }
    }

    // Update weapon stats based on the item lore
    public static void updateWeaponStats(PlayerStats playerStats, ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasLore()) {
                double damageValue = ExtractLore.getDamageFromLore(meta.getLore());
                double armorValue = ExtractLore.getArmorFromLore(meta.getLore());
                double critValue = ExtractLore.getCritFromLore(meta.getLore());
                double maxHpValue = ExtractLore.getMaxHpFromLore(meta.getLore());
                playerStats.getWeaponStats().setDamage(damageValue);
                playerStats.getWeaponStats().setArmor(armorValue);
                playerStats.getWeaponStats().setCrit(critValue);
                playerStats.getWeaponStats().setMaxHp(maxHpValue);
            }
        }
    }

    // Method to clear the player's weapon stats
    public static void clearWeaponStats(PlayerStats playerStats) {
        playerStats.getWeaponStats().setDamage(0);
        playerStats.getWeaponStats().setArmor(0);
        playerStats.getWeaponStats().setCrit(0);
        playerStats.getWeaponStats().setMaxHp(0);
    }

    public static void updateOffhandStats(PlayerStats playerStats, ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasLore()) {
                double damageValue = ExtractLore.getDamageFromLore(meta.getLore());
                double armorValue = ExtractLore.getArmorFromLore(meta.getLore());
                double critValue = ExtractLore.getCritFromLore(meta.getLore());
                double maxHpValue = ExtractLore.getMaxHpFromLore(meta.getLore());
                playerStats.getGearStats().getOffhandStats().setDamage(damageValue);
                playerStats.getGearStats().getOffhandStats().setArmor(armorValue);
                playerStats.getGearStats().getOffhandStats().setCrit(critValue);
                playerStats.getGearStats().getOffhandStats().setMaxHp(maxHpValue);
            }
        }
    }

    // Method to clear the player's offhand stats
    public static void clearOffhandStats(PlayerStats playerStats) {
        playerStats.getGearStats().getOffhandStats().resetStats();
    }


    private boolean isWeapon(ItemStack item) {
        if (item == null) return false;

        switch (item.getType()) {
            case WOODEN_SWORD:
            case STONE_SWORD:
            case IRON_SWORD:
            case GOLDEN_SWORD:
            case DIAMOND_SWORD:
            case NETHERITE_SWORD:
            case CARROT_ON_A_STICK:
            case BOW:
            case CROSSBOW:
            case TRIDENT:
                return true;
            default:
                return false;
        }
    }

    public static void updateSpecificGearStats(PlayerStats playerStats, ItemStack item, PlayerArmorChangeEvent.SlotType slot) {
        double damageValue = 0;
        double armorValue = 0;
        double critValue = 0;
        double maxHpValue = 0;

        // Check if the item is not null or air
        if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasLore()) {
                damageValue = ExtractLore.getDamageFromLore(meta.getLore());
                armorValue = ExtractLore.getArmorFromLore(meta.getLore());
                critValue = ExtractLore.getCritFromLore(meta.getLore());
                maxHpValue = ExtractLore.getMaxHpFromLore(meta.getLore());
            }
        }

        // Use if statements to update the specific gear stats based on the slot type
        if (slot == PlayerArmorChangeEvent.SlotType.HEAD) {
            playerStats.getGearStats().getHelmetStats().setDamage(damageValue);
            playerStats.getGearStats().getHelmetStats().setArmor(armorValue);
            playerStats.getGearStats().getHelmetStats().setCrit(critValue);
            playerStats.getGearStats().getHelmetStats().setMaxHp(maxHpValue);
        } else if (slot == PlayerArmorChangeEvent.SlotType.CHEST) {
            playerStats.getGearStats().getChestplateStats().setDamage(damageValue);
            playerStats.getGearStats().getChestplateStats().setArmor(armorValue);
            playerStats.getGearStats().getChestplateStats().setCrit(critValue);
            playerStats.getGearStats().getChestplateStats().setMaxHp(maxHpValue);
        } else if (slot == PlayerArmorChangeEvent.SlotType.LEGS) {
            playerStats.getGearStats().getLeggingsStats().setDamage(damageValue);
            playerStats.getGearStats().getLeggingsStats().setArmor(armorValue);
            playerStats.getGearStats().getLeggingsStats().setCrit(critValue);
            playerStats.getGearStats().getLeggingsStats().setMaxHp(maxHpValue);
        } else if (slot == PlayerArmorChangeEvent.SlotType.FEET) {
            playerStats.getGearStats().getBootsStats().setDamage(damageValue);
            playerStats.getGearStats().getBootsStats().setArmor(armorValue);
            playerStats.getGearStats().getBootsStats().setCrit(critValue);
            playerStats.getGearStats().getBootsStats().setMaxHp(maxHpValue);
        } else {
            Bukkit.broadcastMessage("Error: Unknown slot type " + slot);
        }
    }

}
