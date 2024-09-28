package me.diu.gachafight.shop.sell;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.utils.SellPriceCalculator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopManager implements Listener {

    private final GachaFight plugin;
    private final ThreadLocal<Boolean> isReopening = ThreadLocal.withInitial(() -> false);

    public ShopManager(GachaFight plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // Opens the shop GUI for the player
    public static void openShopGUI(Player player) {
        Inventory shopInventory = Bukkit.createInventory(new ShopInventoryHolder(GachaFight.getInstance()), 54, MiniMessage.miniMessage().deserialize("<!i><gold>Sell Items"));

        // Set the sell button in the middle of the last row (slot 49)
        ItemStack sellButton = createSellButton();
        shopInventory.setItem(49, sellButton);

        player.openInventory(shopInventory);
    }

    private static ItemStack createSellButton() {
        ItemStack sellButton = new ItemStack(Material.EMERALD);
        ItemMeta meta = sellButton.getItemMeta();

        meta.displayName(MiniMessage.miniMessage().deserialize("<!i><green>Sell Items"));

        // Initialize lore with a placeholder for item counts
        meta.lore(updateSellButtonLore(0, 0, 0, 0, 0, 0));

        sellButton.setItemMeta(meta);
        return sellButton;
    }

    private static List<Component> updateSellButtonLore(int common, int uncommon, int rare, int unique, int legendary, int mythic) {
        List<Component> lore = new ArrayList<>();
        lore.add(MiniMessage.miniMessage().deserialize("<!i><white>Common: " + common + "</white>"));
        lore.add(MiniMessage.miniMessage().deserialize("<!i><gray>Uncommon: " + uncommon + "</gray>"));
        lore.add(MiniMessage.miniMessage().deserialize("<!i><green>Rare: " + rare + "</green>"));
        lore.add(MiniMessage.miniMessage().deserialize("<!i><light_purple>Unique: " + unique + "</light_purple>"));
        lore.add(MiniMessage.miniMessage().deserialize("<!i><gold>Legendary: " + legendary + "</gold>"));
        lore.add(MiniMessage.miniMessage().deserialize("<!i><red>Mythic: " + mythic + "</red>"));
        lore.add(MiniMessage.miniMessage().deserialize("<!i>"));
        lore.add(MiniMessage.miniMessage().deserialize("<!i><yellow>Click to sell all items!</yellow>"));
        return lore;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // Ignore offhand interactions
        }
        if (event.getRightClicked().getName().equalsIgnoreCase("sell shop")) {
            event.setCancelled(true);
            openShopGUI(event.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof ShopInventoryHolder) {
            Player player = (Player) event.getWhoClicked();

            if (event.getClickedInventory() == null || event.getCurrentItem() == null) return;

            int slot = event.getSlot();

            // Allow item manipulation in the first 45 slots
            if (slot < 45) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    int[] counts = countItemsByRarity(event.getInventory());
                    updateSellButton(event.getInventory(), counts);
                }, 1L);
                return;  // Allow adding/removing items in the main area
            }

            // Prevent interactions with the sell button and the rest of the GUI
            event.setCancelled(true);

            // Handle item selling
            if (slot == 49 && event.getCurrentItem().getType() == Material.EMERALD) {
                double totalMoney = sellItems(event.getInventory());
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You sold your items for <gold>" + String.format("%.1f", totalMoney) + " <green>money!"));
                PlayerStats.getPlayerStats(player).setMoney(PlayerStats.getPlayerStats(player).getMoney() + totalMoney);
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof ShopInventoryHolder) {
            Inventory shopInventory = event.getInventory();
            Player player = (Player) event.getPlayer();

            // Check if there are any items left in the first 45 slots (excluding the sell button)
            boolean hasItemsLeft = false;
            for (int i = 0; i < 45; i++) {
                if (shopInventory.getItem(i) != null && shopInventory.getItem(i).getType() != Material.AIR) {
                    hasItemsLeft = true;
                    break;
                }
            }

            if (hasItemsLeft && !isReopening.get()) {
                // Reopen the shop and remind the player to sell items
                isReopening.set(true); // Set the flag to indicate reopening
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.openInventory(shopInventory); // Reopen the inventory
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Please sell your items before closing the shop!"));
                    isReopening.set(false); // Reset the flag after reopening
                });
            }
        }
    }

    private int[] countItemsByRarity(Inventory inventory) {
        int common = 0, uncommon = 0, rare = 0, unique = 0, legendary = 0, mythic = 0;

        for (int i = 0; i < 45; i++) { // Iterate through the first 45 slots (excluding the sell button)
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                int rarityIndex = getItemRarityIndex(item);
                switch (rarityIndex) {
                    case 0: common++; break;
                    case 1: uncommon++; break;
                    case 2: rare++; break;
                    case 3: unique++; break;
                    case 4: legendary++; break;
                    case 5: mythic++; break;
                }
            }
        }
        return new int[]{common, uncommon, rare, unique, legendary, mythic};
    }

    private void updateSellButton(Inventory inventory, int[] counts) {
        ItemStack sellButton = inventory.getItem(49);
        if (sellButton != null && sellButton.hasItemMeta()) {
            ItemMeta meta = sellButton.getItemMeta();
            meta.lore(updateSellButtonLore(counts[0], counts[1], counts[2], counts[3], counts[4], counts[5]));
            sellButton.setItemMeta(meta);
        }
    }

    private double sellItems(Inventory inventory) {
        double totalMoney = 0.0;

        for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                int rarityIndex = getItemRarityIndex(item);
                totalMoney += SellPriceCalculator.calculateSellPrice(item, rarityIndex);
                inventory.setItem(i, null); // Remove the item from the inventory
            }
        }

        return totalMoney;
    }

    private int getItemRarityIndex(ItemStack item) {
        // Retrieve item rarity based on lore or other custom logic
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<Component> lore = item.getItemMeta().lore();
            if (lore != null) {
                for (Component loreLine : lore) {
                    String plainTextLore = MiniMessage.miniMessage().deserialize(loreLine.toString()).toString();
                    if (plainTextLore.contains("Common")) return 0;
                    if (plainTextLore.contains("Uncommon")) return 1;
                    if (plainTextLore.contains("Rare")) return 2;
                    if (plainTextLore.contains("Unique")) return 3;
                    if (plainTextLore.contains("Legendary")) return 4;
                    if (plainTextLore.contains("Mythic")) return 5;
                }
            }
        }
        return 0; // Default to common if no rarity found
    }
}
