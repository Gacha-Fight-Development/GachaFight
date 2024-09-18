package me.diu.gachafight.shop.equipmentspecialist;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.gacha.managers.GachaManager;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.utils.Calculations;
import me.diu.gachafight.utils.ExtractLore;
import me.diu.gachafight.utils.Prefix;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EquipmentSpecialistListener implements Listener {
    private final GachaFight plugin;
    private double costForLevel;
    private double costForReroll;

    public EquipmentSpecialistListener(GachaFight plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMenuClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        Inventory inventory = event.getClickedInventory();
        ItemStack chestArrow1 = createCustomItem(Material.PAPER, "§aSlot 1", 10123, "§cYou will need 2 of the same equipment to Level Up!");
        ItemStack chestArrow2 = createCustomItem(Material.PAPER, "§aSlot 2", 10123, "§cYou will need 2 of the same equipment to Level Up!");
        if (event.getView().getTitle().equals("Equipment Specialist")) {
            event.setCancelled(true);  // Prevent moving items in the menu

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            // Check if they clicked on "Level Up Equipment"
            if (clickedItem.getType() == Material.ANVIL && clickedItem.getItemMeta().getDisplayName().equals("§aLevel Up Equipment")) {
                openLevelUpMenu(player);  // Open the second GUI for Level Up Equipment
            }

            // Check if they clicked on "Reroll Stats"
            if (clickedItem.getType() == Material.GRINDSTONE && clickedItem.getItemMeta().getDisplayName().equals("§bReroll Stats")) {
                if (player.hasPermission("gachafight.admin")) return;
                openRerollStatsMenu(player);  // Open the third GUI for Reroll Stats
            }
        } else if (event.getView().getTitle().equals("Reroll Stats")) {
            event.setCancelled(true);  // Prevent moving items in the menu

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            // Slot 22: Confirm
            if (event.getSlot() == 22 && clickedItem.getType() == Material.PAPER && clickedItem.getItemMeta().getDisplayName().equals("§aConfirm")) {
                ItemStack equipment = inventory.getItem(10);  // Equipment slot
                ItemStack cloneItem = inventory.getItem(16);  // Clone item slot

                if (equipment != null && cloneItem != null) {
                    PlayerStats playerStats = PlayerStats.getPlayerStats(player);
                    // Check if player has enough money (this part depends on how you're handling player stats)
                    if (playerStats.getMoney() >= costForReroll) {
                        // Deduct money
                        playerStats.setMoney(playerStats.getMoney()-costForReroll);

                        // Create the clone of the equipment and place it in the middle slot (13)
                        ItemStack clonedItem = equipment.clone();
                        inventory.setItem(13, clonedItem);

                        player.sendMessage("§aItem leveled up successfully!");

                    } else {
                        player.sendMessage("§cNot enough money.");
                    }
                } else {
                    player.sendMessage("§cYou must place items in both slots.");
                }
            }

        } else if (event.getView().getTitle().equals("Level Up Equipment")) {
            event.setCancelled(true);  // Prevent moving items in the menu

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            Inventory guiInventory = event.getView().getTopInventory();
            if (event.getSlot() == 13 && clickedItem.getType() != Material.BLUE_STAINED_GLASS_PANE) {
                guiInventory.setItem(13, new ItemStack(Material.BLUE_STAINED_GLASS_PANE));
                guiInventory.setItem(10, chestArrow1);
                guiInventory.setItem(16, chestArrow2);
                player.getInventory().addItem(clickedItem);
            }

            if (event.getSlot() == 10 && clickedItem.getType() != Material.PAPER) {
                guiInventory.setItem(10, chestArrow1);
                player.getInventory().addItem(clickedItem);
            }

            if (event.getSlot() == 16 && clickedItem.getType() != Material.PAPER) {
                guiInventory.setItem(16, chestArrow2);
                player.getInventory().addItem(clickedItem);
            }

            // Slot 22: Confirm
            if (event.getSlot() == 22 && clickedItem.getType() == Material.PAPER && clickedItem.getItemMeta().getDisplayName().equals("§aConfirm")) {
                ItemStack equipment = guiInventory.getItem(10);  // Equipment slot
                ItemStack cloneItem = guiInventory.getItem(16);  // Clone item slot
                int rarity = ExtractLore.extractRarityFromLore(equipment.getLore());

                if (equipment != null && cloneItem != null && equipment.getType() != Material.PAPER && cloneItem.getType() != Material.PAPER) {
                    PlayerStats playerStats = PlayerStats.getPlayerStats(player);
                    double levelMulti = Calculations.playerLevelMultiplier(playerStats.getLevel());
                    // Check if player has enough money (this part depends on how you're handling player stats)
                    if (playerStats.getMoney() >= costForLevel) {
                        // Deduct money
                        playerStats.setMoney(playerStats.getMoney()-costForLevel);
                        ItemStack itemLevelUp;

                        if (ExtractLore.extractPercentageFromName(equipment.getItemMeta().getDisplayName()) >= ExtractLore.extractPercentageFromName(cloneItem.getItemMeta().getDisplayName())) {
                            itemLevelUp = guiInventory.getItem(10);
                            ItemMeta levelUpItemMeta = itemLevelUp.getItemMeta();
                            if (levelUpItemMeta != null) {
                                PersistentDataContainer pdc = levelUpItemMeta.getPersistentDataContainer();
                                NamespacedKey damageKey = new NamespacedKey(plugin, "MinMaxDamage");
                                NamespacedKey armorKey = new NamespacedKey(plugin, "MinMaxArmor");
                                NamespacedKey hpKey = new NamespacedKey(plugin, "MinMaxHP");
                                if (pdc.has(damageKey, PersistentDataType.STRING)) {
                                    List<String> minMax = ExtractLore.extractMinAndMaxFromPDC(pdc, damageKey);
                                    double min = Double.parseDouble(minMax.get(0));  // Get the min value
                                    double max = Double.parseDouble(minMax.get(1));  // Get the max value
                                    player.sendMessage("Min: " + min + ", Max: " + max);
                                    double newStat = levelUpItem(min, max, levelMulti, GachaManager.getRarityMultiplier(rarity), ExtractLore.extractPercentageFromName(levelUpItemMeta.getDisplayName()));

                                    // Find the lore line containing the "Damage:" keyword and update it with newStat
                                    List<String> loreString = levelUpItemMeta.getLore();
                                    List<Component> lore = levelUpItemMeta.lore();
                                    int damageLineIndex = ExtractLore.getLoreLine(loreString, "Damage:");
                                    if (damageLineIndex != -1) {
                                        lore.set(damageLineIndex, MiniMessage.miniMessage().deserialize(Prefix.getDamagePrefix() + String.format("%.2f", newStat)));  // Format the new stat
                                        levelUpItemMeta.lore(lore);  // Set updated lore
                                    }
                                }

                                if (pdc.has(armorKey, PersistentDataType.STRING)) {
                                    List<String> minMax = ExtractLore.extractMinAndMaxFromPDC(pdc, armorKey);
                                    double min = Double.parseDouble(minMax.get(0));  // Get the min value
                                    double max = Double.parseDouble(minMax.get(1));  // Get the max value
                                    player.sendMessage("Min: " + min + ", Max: " + max);
                                    double newStat = levelUpItem(min, max, levelMulti, GachaManager.getRarityMultiplier(rarity), ExtractLore.extractPercentageFromName(levelUpItemMeta.getDisplayName()));

                                    // Find the lore line containing the "Armor:" keyword and update it with newStat
                                    List<String> loreString = levelUpItemMeta.getLore();
                                    List<Component> lore = levelUpItemMeta.lore();
                                    int armorLineIndex = ExtractLore.getLoreLine(loreString, "Armor:");
                                    if (armorLineIndex != -1) {
                                        lore.set(armorLineIndex, MiniMessage.miniMessage().deserialize(Prefix.getArmorPrefix()+ String.format("%.2f", newStat)));  // Format the new stat
                                        levelUpItemMeta.lore(lore);  // Set updated lore
                                    }
                                }

                                if (pdc.has(hpKey, PersistentDataType.STRING)) {
                                    List<String> minMax = ExtractLore.extractMinAndMaxFromPDC(pdc, hpKey);
                                    double min = Double.parseDouble(minMax.get(0));  // Get the min value
                                    double max = Double.parseDouble(minMax.get(1));  // Get the max value
                                    double newStat = levelUpItem(min, max, levelMulti, GachaManager.getRarityMultiplier(rarity), ExtractLore.extractPercentageFromName(levelUpItemMeta.getDisplayName()));

                                    // Find the lore line containing the "HP:" keyword and update it with newStat
                                    List<String> loreString = levelUpItemMeta.getLore();
                                    List<Component> lore = levelUpItemMeta.lore();
                                    int hpLineIndex = ExtractLore.getLoreLine(loreString, "HP:");
                                    if (hpLineIndex != -1) {
                                        lore.set(hpLineIndex, MiniMessage.miniMessage().deserialize(Prefix.getHealthPrefix()+ String.format("%.2f", newStat)));  // Format the new stat
                                        levelUpItemMeta.lore(lore);  // Set updated lore
                                    }
                                }
                                Component newName = LevelUpDisplayName(levelUpItemMeta.displayName(), player, rarity);
                                levelUpItemMeta.displayName(newName);
                                itemLevelUp.setItemMeta(levelUpItemMeta);
                                inventory.setItem(10, chestArrow1);
                                inventory.setItem(16, chestArrow2);
                                inventory.setItem(13, itemLevelUp);  // Place the leveled-up item in slot 13
                            }
                        } else {
                            itemLevelUp = guiInventory.getItem(16);
                            ItemMeta levelUpItemMeta = itemLevelUp.getItemMeta();
                            if (levelUpItemMeta != null) {
                                PersistentDataContainer pdc = levelUpItemMeta.getPersistentDataContainer();
                                NamespacedKey damageKey = new NamespacedKey(plugin, "MinMaxDamage");
                                NamespacedKey armorKey = new NamespacedKey(plugin, "MinMaxArmor");
                                NamespacedKey hpKey = new NamespacedKey(plugin, "MinMaxHP");
                                if (pdc.has(damageKey, PersistentDataType.STRING)) {
                                    List<String> minMax = ExtractLore.extractMinAndMaxFromPDC(pdc, damageKey);
                                    double min = Double.parseDouble(minMax.get(0));  // Get the min value
                                    double max = Double.parseDouble(minMax.get(1));  // Get the max value
                                    player.sendMessage("Min: " + min + ", Max: " + max);
                                    double newStat = levelUpItem(min, max, levelMulti, GachaManager.getRarityMultiplier(rarity), ExtractLore.extractPercentageFromName(levelUpItemMeta.getDisplayName()));

                                    // Find the lore line containing the "Damage:" keyword and update it with newStat
                                    List<String> loreString = levelUpItemMeta.getLore();
                                    List<Component> lore = levelUpItemMeta.lore();
                                    int damageLineIndex = ExtractLore.getLoreLine(loreString, "Damage:");
                                    if (damageLineIndex != -1) {
                                        lore.set(damageLineIndex, MiniMessage.miniMessage().deserialize(Prefix.getDamagePrefix() + String.format("%.2f", newStat)));  // Format the new stat
                                        levelUpItemMeta.lore(lore);  // Set updated lore
                                    }
                                }

                                if (pdc.has(armorKey, PersistentDataType.STRING)) {
                                    List<String> minMax = ExtractLore.extractMinAndMaxFromPDC(pdc, armorKey);
                                    double min = Double.parseDouble(minMax.get(0));  // Get the min value
                                    double max = Double.parseDouble(minMax.get(1));  // Get the max value
                                    player.sendMessage("Min: " + min + ", Max: " + max);
                                    double newStat = levelUpItem(min, max, levelMulti, GachaManager.getRarityMultiplier(rarity), ExtractLore.extractPercentageFromName(levelUpItemMeta.getDisplayName()));

                                    // Find the lore line containing the "Armor:" keyword and update it with newStat
                                    List<String> loreString = levelUpItemMeta.getLore();
                                    List<Component> lore = levelUpItemMeta.lore();
                                    int armorLineIndex = ExtractLore.getLoreLine(loreString, "Armor:");
                                    if (armorLineIndex != -1) {
                                        lore.set(armorLineIndex, MiniMessage.miniMessage().deserialize(Prefix.getArmorPrefix()+ String.format("%.2f", newStat)));  // Format the new stat
                                        levelUpItemMeta.lore(lore);  // Set updated lore
                                    }
                                }

                                if (pdc.has(hpKey, PersistentDataType.STRING)) {
                                    List<String> minMax = ExtractLore.extractMinAndMaxFromPDC(pdc, hpKey);
                                    double min = Double.parseDouble(minMax.get(0));  // Get the min value
                                    double max = Double.parseDouble(minMax.get(1));  // Get the max value
                                    double newStat = levelUpItem(min, max, levelMulti, GachaManager.getRarityMultiplier(rarity), ExtractLore.extractPercentageFromName(levelUpItemMeta.getDisplayName()));

                                    // Find the lore line containing the "HP:" keyword and update it with newStat
                                    List<String> loreString = levelUpItemMeta.getLore();
                                    List<Component> lore = levelUpItemMeta.lore();
                                    int hpLineIndex = ExtractLore.getLoreLine(loreString, "HP:");
                                    if (hpLineIndex != -1) {
                                        lore.set(hpLineIndex, MiniMessage.miniMessage().deserialize(Prefix.getHealthPrefix()+ String.format("%.2f", newStat)));  // Format the new stat
                                        levelUpItemMeta.lore(lore);  // Set updated lore
                                    }
                                }
                                Component newName = LevelUpDisplayName(levelUpItemMeta.displayName(), player, rarity);
                                levelUpItemMeta.displayName(newName);
                                itemLevelUp.setItemMeta(levelUpItemMeta);
                                inventory.setItem(13, itemLevelUp);  // Place the leveled-up item in slot 13
                            }
                        }
                        player.sendMessage("§aItem leveled up successfully!");

                    } else {
                        player.sendMessage("§cNot enough money.");
                    }
                } else {
                    player.sendMessage("§cYou must place items in both slots.");
                }
            }
            if (event.getClickedInventory() != guiInventory) {  // Prevent placing from menu itself
                ItemStack slot10 = guiInventory.getItem(10);  // First chest arrow slot
                ItemStack slot16 = guiInventory.getItem(16);  // Second chest arrow slot

                if (slot10 == null || slot10.getType() == Material.PAPER) {
                    if (clickedItem.getMaxStackSize() > 1) {
                        player.sendMessage("§cThis Item Is Stackable!");
                        return;
                    }
                    if (ExtractLore.extractLevelFromName(clickedItem.getItemMeta().getItemName()) >= PlayerStats.getPlayerStats(player).getLevel()) {
                        player.sendMessage("§cThis Item is already your level!");
                        return;
                    }
                    guiInventory.setItem(10, clickedItem);
                    player.getInventory().setItem(event.getSlot(), null);  // Remove item from player's cursor
                    int rarity = ExtractLore.extractRarityFromLore(clickedItem.getLore());
                    costForLevel = 100* Math.pow( PlayerStats.getPlayerStats(player).getLevel(), (1+(rarity*0.67)));
                    ItemStack greenCheck = createCustomItem(Material.PAPER, "§aConfirm", 10109, "§aCost: §6" + String.format("%.2f",costForLevel));
                    guiInventory.setItem(22, greenCheck);
                    player.sendMessage("§aItem placed in slot 1");
                } else if (slot16 == null || slot16.getType() == Material.PAPER) {
                    if (ExtractLore.ExtractItemName(guiInventory.getItem(10).getItemMeta().getDisplayName())
                            .equals(ExtractLore.ExtractItemName(clickedItem.getItemMeta().getDisplayName())) &&  //same item name
                            ExtractLore.extractRarityFromLore(guiInventory.getItem(10).getItemMeta().getLore()) ==
                                    ExtractLore.extractRarityFromLore(clickedItem.getItemMeta().getLore())) { //same rarity
                        guiInventory.setItem(16, clickedItem);
                        player.getInventory().setItem(event.getSlot(), null);  // Remove item from player's cursor
                        player.sendMessage("§aItem placed in slot 2");
                    } else {
                        player.sendMessage("§cNot the same item! (Wrong Rarity or Wrong Item Type!)");
                    }
                } else {
                    player.sendMessage("§cBoth slots are already filled.");
                }
            }
        }
    }

    @EventHandler
    public void onMenuClose(@NotNull InventoryCloseEvent event) {
        Inventory guiInventory = event.getView().getTopInventory();
        if (event.getView().getTitle().equals("Level Up Equipment")) {
            if (guiInventory.getItem(13).getType() != Material.BLUE_STAINED_GLASS_PANE) {
                event.getPlayer().getInventory().addItem(guiInventory.getItem(13));
            }
            if (guiInventory.getItem(16).getType() != Material.PAPER) {
                event.getPlayer().getInventory().addItem(guiInventory.getItem(16));
            }
            if (guiInventory.getItem(10).getType() != Material.PAPER) {
                event.getPlayer().getInventory().addItem(guiInventory.getItem(10));
            }
        }
        Player player = (Player) event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                player.updateInventory();
            }
        }.runTaskLater(plugin, 1L);
    }

    public void openLevelUpMenu(Player player) {
        Inventory levelUpMenu = Bukkit.createInventory(null, 27, "Level Up Equipment");

        // Setup arrows, checkmarks, and stained glass as per the second screenshot
        ItemStack rightArrow = createCustomItem(Material.PAPER, null, 10099);  // No tooltip
        ItemStack leftArrow = createCustomItem(Material.PAPER, null, 10097);   // No tooltip
        ItemStack greenCheck = createCustomItem(Material.PAPER, "§aConfirm", 10109, "§aCost: §6" + costForLevel);
        ItemStack chestArrow1 = createCustomItem(Material.PAPER, "§aClick on your equipment", 10123, "§6This is a slot for your equipment");
        ItemStack chestArrow2 = createCustomItem(Material.PAPER, "§aArrow Check", 10123, "§6This is a slot for your clone item");
        ItemStack guide = createCustomItem(Material.COMPASS, "§dLevel Up Equipment Guide", 1, "§6Leveling up equipment means that you have a", "§6equipment that is lower level than your current", "§6level, then leveled up item will be your level", "§6Example: [2] Sword -> [your level] Sword");
        // Cyan glass border for GUI
        ItemStack bluePane = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        for (int i = 0; i < levelUpMenu.getSize(); i++) {
            if (levelUpMenu.getItem(i) == null) {
                levelUpMenu.setItem(i, bluePane);
            }
        }

        // Set items in the level-up inventory
        levelUpMenu.setItem(4, guide);
        levelUpMenu.setItem(10, chestArrow1);  // Equipment slot
        levelUpMenu.setItem(16, chestArrow2);  // Clone item slot
        levelUpMenu.setItem(11, rightArrow);
        levelUpMenu.setItem(12, rightArrow);
        levelUpMenu.setItem(14, leftArrow);
        levelUpMenu.setItem(15, leftArrow);
        levelUpMenu.setItem(22, greenCheck);  // Confirm slot

        player.openInventory(levelUpMenu);  // Open the Level Up GUI
    }

    public void openRerollStatsMenu(Player player) {
        Inventory rerollStatsMenu = Bukkit.createInventory(null, 27, "Reroll Stats");

        // Setup arrows, checkmarks, and stained glass as per the third screenshot
        ItemStack rightArrow = createCustomItem(Material.PAPER, "§aRight", 10099);
        ItemStack leftArrow = createCustomItem(Material.PAPER, "§aLeft", 10097);
        ItemStack greenCheck = createCustomItem(Material.PAPER, "§aConfirm", 10109);
        ItemStack chestArrow = createCustomItem(Material.PAPER, "§aArrow Check", 10123);

        // Cyan glass border for GUI
        ItemStack lightBluePane = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        for (int i = 0; i < rerollStatsMenu.getSize(); i++) {
            if (rerollStatsMenu.getItem(i) == null) {
                rerollStatsMenu.setItem(i, lightBluePane);
            }
        }

        // Set items in the reroll stats inventory
        rerollStatsMenu.setItem(10, chestArrow);
        rerollStatsMenu.setItem(16, chestArrow);
        rerollStatsMenu.setItem(11, rightArrow);
        rerollStatsMenu.setItem(12, rightArrow);
        rerollStatsMenu.setItem(14, leftArrow);
        rerollStatsMenu.setItem(15, leftArrow);
        rerollStatsMenu.setItem(22, greenCheck);

        player.openInventory(rerollStatsMenu);  // Open the Reroll Stats GUI
    }

    // Helper function to create items with custom model data
    @NotNull
    private ItemStack createCustomItem(Material material, String name, int customModelData, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (name != null) {
            meta.setDisplayName(name);
        }
        meta.setCustomModelData(customModelData);
        if (lore != null && lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

    public static double levelUpItem(double min, double max, double levelmulti, double rarity, double percentage) {
        // Calculate the difference between the max and min values, both scaled by rarity
        double scaledMin = min * rarity;
        double scaledMax = max * rarity;
        Bukkit.getLogger().info(scaledMax + " " + scaledMin);
        // Calculate the new stat based on the percentage between the min and max
        double newStat = scaledMin + (percentage * (scaledMax - scaledMin));
        Bukkit.getLogger().info(scaledMax + " 2: " + scaledMin + " 3: " + newStat + " 4: " + percentage + " 5:" + levelmulti);

        // Apply the level multiplier
        newStat *= levelmulti;

        // Return the final new stat value
        return newStat;
    }

    public Component LevelUpDisplayName(Component originalName, Player player, int rarity) {
        // Convert Component to plain text for easier manipulation
        String originalNameText = PlainTextComponentSerializer.plainText().serialize(originalName);

        // Regular expression to match the level in the format [number]
        String regex = "\\[(\\d+)]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(originalNameText);

        // Player's new level
        int playerLevel = PlayerStats.getPlayerStats(player).getLevel();
        String newLevelText = "<dark_gray>[<gold>" + playerLevel + "<dark_gray>]"+ GachaManager.getRarityColor(rarity);

        // If the level is found, replace it with the player's current level
        if (matcher.find()) {
            // Replace the old level with the new one
            String updatedNameText = matcher.replaceFirst(newLevelText);

            // Convert the updated name back to a Component using MiniMessage
            return MiniMessage.miniMessage().deserialize("<!i>"+updatedNameText);
        }

        // If no level is found, return the original name
        return originalName;
    }

}
