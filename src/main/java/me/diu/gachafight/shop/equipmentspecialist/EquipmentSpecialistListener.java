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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EquipmentSpecialistListener implements Listener {
    private final GachaFight plugin;
    private static double costForLevel;
    private static double costForReroll;

    public EquipmentSpecialistListener(GachaFight plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMenuClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        Inventory inventory = event.getClickedInventory();
        Inventory guiInventory = event.getView().getTopInventory();
        PersistentDataContainer pdc;
        ItemStack chestArrow1 = createCustomItem(Material.PAPER, "§aArrow Check", 10123, "§cYou will need 2 of the same equipment to Level Up!");
        ItemStack chestArrow2 = createCustomItem(Material.PAPER, "§aArrow Check", 10123, "§cYou will need 2 of the same equipment to Level Up!");
        if (event.getView().getTitle().equals("Equipment Specialist")) {
            event.setCancelled(true);  // Prevent moving items in the menu

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            // Check if they clicked on "Level Up Equipment"
            if (clickedItem.getType() == Material.ANVIL && clickedItem.getItemMeta().getDisplayName().equals("§aLevel Up Equipment")) {
                openLevelUpMenu(player);  // Open the second GUI for Level Up Equipment
            }

            // Check if they clicked on "Reroll Stats"
            if (clickedItem.getType() == Material.GRINDSTONE && clickedItem.getItemMeta().getDisplayName().equals("§bReroll Stats")) {
                openRerollStatsMenu(player);  // Open the third GUI for Reroll Stats
            }
        }else if (event.getView().getTitle().equals("Reroll Stats")) { //Level: Stay | Percentage: Change
            event.setCancelled(true);  // Prevent moving items in the menu
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            if (event.getSlot() == 13 && clickedItem.getType() != Material.LIGHT_BLUE_STAINED_GLASS_PANE && event.getClickedInventory().equals(event.getView().getTopInventory())) {
                guiInventory.setItem(13, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
                guiInventory.setItem(10, chestArrow1);
                guiInventory.setItem(16, chestArrow2);
                player.getInventory().addItem(clickedItem);
            }
            if (event.getSlot() == 10 && !clickedItem.getItemMeta().getDisplayName().contains("Arrow Check")) {
                guiInventory.setItem(10, chestArrow1);
                player.getInventory().addItem(clickedItem);
            }
            if (event.getSlot() == 16 && !clickedItem.getItemMeta().getDisplayName().contains("Arrow Check")) {
                guiInventory.setItem(16, chestArrow2);
                player.getInventory().addItem(clickedItem);
            }
            // Slot 22: Confirm
            if (event.getSlot() == 22 && clickedItem.getType() == Material.PAPER && clickedItem.getItemMeta().getDisplayName().equals("§aConfirm")) {
                ItemStack equipment = inventory.getItem(10);  // Equipment slot
                ItemStack cloneItem = inventory.getItem(16);  // Clone item slot
                int rarity = ExtractLore.extractRarityFromLore(equipment.getLore());

                if (!isValidEquipmentPair(equipment, cloneItem)) {
                    player.sendMessage("§cItems are not valid for rerolling.");
                    return;  // Stop if the items are not valid
                }

                if (equipment != null && cloneItem != null) {
                    PlayerStats playerStats = PlayerStats.getPlayerStats(player);

                    // Ensure the player has enough currency for the reroll
                    if (playerStats.getMoney() >= costForReroll) {
                        // Deduct currency for reroll
                        playerStats.setMoney(playerStats.getMoney() - costForReroll);

                        // Get the ItemMeta and PDC from the equipment
                        ItemStack itemReroll;
                        if (ExtractLore.extractLevelFromName(equipment.getItemMeta().getDisplayName()) >= ExtractLore.extractLevelFromName(cloneItem.getItemMeta().getDisplayName())) {
                            itemReroll = guiInventory.getItem(10);
                            ItemMeta RerollItemMeta = itemReroll.getItemMeta();
                            int level = ExtractLore.extractLevelFromName(RerollItemMeta.getDisplayName());
                            double levelMulti = Calculations.playerLevelMultiplier(level);
                            if (RerollItemMeta != null) {
                                pdc = RerollItemMeta.getPersistentDataContainer();
                                upgradeStat(player, RerollItemMeta, pdc, new NamespacedKey(plugin, "MinMaxDamage"), "Damage", levelMulti, rarity, true);
                                upgradeStat(player, RerollItemMeta, pdc, new NamespacedKey(plugin, "MinMaxArmor"), "Armor", levelMulti, rarity, true);
                                upgradeStat(player, RerollItemMeta, pdc, new NamespacedKey(plugin, "MinMaxHP"), "HP", levelMulti, rarity, true);
                                itemReroll.setItemMeta(RerollItemMeta);
                                RerollItemMeta = itemReroll.getItemMeta();
                                Component newName = rerollDisplayName(RerollItemMeta, player, pdc);
                                RerollItemMeta.displayName(newName);
                                itemReroll.setItemMeta(RerollItemMeta);
                                inventory.setItem(10, chestArrow1);
                                inventory.setItem(16, chestArrow2);
                                inventory.setItem(13, itemReroll);  // Place the leveled-up item in slot 13
                            }
                        } else {
                            itemReroll = guiInventory.getItem(16);
                            ItemMeta RerollItemMeta = itemReroll.getItemMeta();
                            int level = ExtractLore.extractLevelFromName(RerollItemMeta.getDisplayName());
                            double levelMulti = Calculations.playerLevelMultiplier(level);
                            if (RerollItemMeta != null) {
                                pdc = RerollItemMeta.getPersistentDataContainer();
                                upgradeStat(player, RerollItemMeta, pdc, new NamespacedKey(plugin, "MinMaxDamage"), "Damage", levelMulti, rarity, true);
                                upgradeStat(player, RerollItemMeta, pdc, new NamespacedKey(plugin, "MinMaxArmor"), "Armor", levelMulti, rarity, true);
                                upgradeStat(player, RerollItemMeta, pdc, new NamespacedKey(plugin, "MinMaxHP"), "HP", levelMulti, rarity, true);
                                itemReroll.setItemMeta(RerollItemMeta);
                                RerollItemMeta = itemReroll.getItemMeta();
                                Component newName = rerollDisplayName(RerollItemMeta, player, pdc);
                                RerollItemMeta.displayName(newName);
                                itemReroll.setItemMeta(RerollItemMeta);
                                inventory.setItem(10, chestArrow1);
                                inventory.setItem(16, chestArrow2);
                                inventory.setItem(13, itemReroll);  // Place the leveled-up item in slot 13
                            }
                        }
                        player.sendMessage("§aItem rerolled successfully!");
                    } else {
                        player.sendMessage("§cNot enough money.");
                    }
                } else {
                    player.sendMessage("§cYou must place items in both slots.");
                }
            }
            equipmentCheck(event, player, true);

        } else if (event.getView().getTitle().equals("Level Up Equipment")) { //Level: Change | Percentage: Stay
            event.setCancelled(true);  // Prevent moving items in the menu

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            if (event.getClickedInventory().equals(event.getView().getTopInventory())) {
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

            }
            // Slot 22: Confirm
            if (event.getSlot() == 22 && clickedItem.getType() == Material.PAPER && clickedItem.getItemMeta().getDisplayName().equals("§aConfirm")) {
                ItemStack equipment = guiInventory.getItem(10);  // Equipment slot
                ItemStack cloneItem = guiInventory.getItem(16);  // Clone item slot
                int rarity = ExtractLore.extractRarityFromLore(equipment.getLore());
                // Check if the two items are a valid pair for leveling up
                if (!isValidEquipmentPair(equipment, cloneItem)) {
                    player.sendMessage("§cItems are not valid for leveling up.");
                    return;  // Stop if the items are not valid
                }
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
                                pdc = levelUpItemMeta.getPersistentDataContainer();
                                upgradeStat(player, levelUpItemMeta, pdc, new NamespacedKey(plugin, "MinMaxDamage"), "Damage", levelMulti, rarity, false);
                                upgradeStat(player, levelUpItemMeta, pdc, new NamespacedKey(plugin, "MinMaxArmor"), "Armor", levelMulti, rarity, false);
                                upgradeStat(player, levelUpItemMeta, pdc, new NamespacedKey(plugin, "MinMaxHP"), "HP", levelMulti, rarity, false);
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
                                pdc = levelUpItemMeta.getPersistentDataContainer();
                                upgradeStat(player, levelUpItemMeta, pdc, new NamespacedKey(plugin, "MinMaxDamage"), "Damage", levelMulti, rarity, false);
                                upgradeStat(player, levelUpItemMeta, pdc, new NamespacedKey(plugin, "MinMaxArmor"), "Armor", levelMulti, rarity, false);
                                upgradeStat(player, levelUpItemMeta, pdc, new NamespacedKey(plugin, "MinMaxHP"), "HP", levelMulti, rarity, false);
                                Component newName = LevelUpDisplayName(levelUpItemMeta.displayName(), player, rarity);
                                levelUpItemMeta.displayName(newName);
                                itemLevelUp.setItemMeta(levelUpItemMeta);
                                inventory.setItem(10, chestArrow1);
                                inventory.setItem(16, chestArrow2);
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
            equipmentCheck(event, player, false);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onMenuClose(@NotNull InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                player.updateInventory();
            }
        }.runTaskLater(plugin, 1L);
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
        } else if (event.getView().getTitle().equals("Reroll Stats")) {
            if (guiInventory.getItem(13).getType() != Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
                event.getPlayer().getInventory().addItem(guiInventory.getItem(13));
            }
            if (guiInventory.getItem(16).getType() != Material.PAPER) {
                event.getPlayer().getInventory().addItem(guiInventory.getItem(16));
            }
            if (guiInventory.getItem(10).getType() != Material.PAPER) {
                event.getPlayer().getInventory().addItem(guiInventory.getItem(10));
            }
        }
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
        ItemStack chestArrow1 = createCustomItem(Material.PAPER, "§aArrow Check", 10123, "§6This is a slot for your equipment");
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
    public static ItemStack createCustomItem(Material material, String name, int customModelData, String... lore) {
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

    private void upgradeStat(Player player, ItemMeta itemMeta, PersistentDataContainer pdc, NamespacedKey key, String statType, double levelMulti, int rarity, boolean isReroll) {
        if (pdc.has(key, PersistentDataType.STRING)) {
            List<String> minMax = ExtractLore.extractMinAndMaxFromPDC(pdc, key);
            double min = Double.parseDouble(minMax.get(0));
            double max = Double.parseDouble(minMax.get(1));
            double newStat;
            if (isReroll) {
                newStat = rerollItem(min, max, levelMulti, GachaManager.getRarityMultiplier(rarity));
            } else {
                newStat = levelUpItem(min, max, levelMulti, GachaManager.getRarityMultiplier(rarity), ExtractLore.extractPercentageFromName(itemMeta.getDisplayName()));
            }
            // Update the lore with the new stat
            List<String> loreString = itemMeta.getLore();
            List<Component> lore = itemMeta.lore();
            int statLineIndex = ExtractLore.getLoreLine(loreString, statType + ":");
            if (statLineIndex != -1) {
                lore.set(statLineIndex, MiniMessage.miniMessage().deserialize(Prefix.getPrefixForStat(statType) + String.format("%.2f", newStat)));
                itemMeta.lore(lore);
            }
        }
    }

    public static double rerollItem(double min, double max, double levelmulti, double rarity) {
        // Calculate the difference between the max and min values, both scaled by rarity
        System.out.println("MIN MAX:" + min +" "+ max);
        double scaledMin = min * rarity;
        double scaledMax = max * rarity;
        System.out.println(scaledMax + " " + scaledMin);
        // Calculate the new stat based on the percentage between the min and max
        double newStat = scaledMin + (Math.random() * (scaledMax - scaledMin));
        System.out.println(newStat);
        // Apply the level multiplier
        System.out.println("Level multi " + levelmulti);
        newStat *= levelmulti;
        System.out.println(newStat);

        // Return the final new stat value
        return newStat;
    }

    private boolean isValidEquipmentPair(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) return false;

        // Check if both items are non-stackable
        if (item1.getMaxStackSize() > 1 || item2.getMaxStackSize() > 1) {
            return false;
        }

        // Compare item types
        if (item1.getType() != item2.getType()) {
            return false;
        }

        // Compare item names
        String itemName1 = ExtractLore.ExtractItemName(item1.getItemMeta().getDisplayName());
        String itemName2 = ExtractLore.ExtractItemName(item2.getItemMeta().getDisplayName());
        if (!itemName1.equals(itemName2)) {
            return false;
        }

        // Compare item rarities from lore
        int rarity1 = ExtractLore.extractRarityFromLore(item1.getLore());
        int rarity2 = ExtractLore.extractRarityFromLore(item2.getLore());
        if (rarity1 != rarity2) {
            return false;
        }

        return true;  // If all checks pass, the items are valid for pairing
    }

    public static void equipmentCheck(InventoryClickEvent event, Player player, boolean reroll) {
        Inventory guiInventory = event.getView().getTopInventory();
        ItemStack clickedItem = event.getCurrentItem();
        if (event.getClickedInventory() != guiInventory) {  // Prevent placing from menu itself
            ItemStack slot10 = guiInventory.getItem(10);  // First chest arrow slot
            ItemStack slot16 = guiInventory.getItem(16);  // Second chest arrow slot

            if (slot10 == null || slot10.getItemMeta().getDisplayName().contains("Arrow Check")) {
                if (clickedItem.getMaxStackSize() > 1) {
                    player.sendMessage("§cThis Item Is Stackable!");
                    return;
                }
                if (!slot16.getItemMeta().getDisplayName().contains("Arrow Check")) {
                    player.sendMessage("§cRemove item from Slot 2!");
                    return;
                }
                if (ExtractLore.extractLevelFromName(clickedItem.getItemMeta().getDisplayName()) >= PlayerStats.getPlayerStats(player).getLevel() && !reroll) {
                    player.sendMessage("§cThis Item is already your level!");
                    return; //only applies to level up
                }
                guiInventory.setItem(10, clickedItem);
                player.getInventory().setItem(event.getSlot(), null);  // Remove item from player's cursor
                int rarity = ExtractLore.extractRarityFromLore(clickedItem.getLore());
                ItemStack greenCheck;
                if (reroll) {
                    if (ExtractLore.extractLevelFromName(clickedItem.getItemMeta().getDisplayName()) >= ExtractLore.extractLevelFromName(slot10.getItemMeta().getDisplayName())) {
                        costForReroll = 10 * Math.pow(ExtractLore.extractLevelFromName(clickedItem.getItemMeta().getDisplayName()), (1+(rarity*0.45)));
                        //System.out.println(costForReroll);
                        System.out.println(slot10.getItemMeta().getDisplayName());
                        //System.out.println(ExtractLore.extractLevelFromName(slot10.getItemMeta().getDisplayName()));
                    } else {
                        costForReroll = 10 * Math.pow(ExtractLore.extractLevelFromName(slot10.getItemMeta().getDisplayName()), (1+(rarity*0.45)));
                        //System.out.println(costForReroll);
                        System.out.println(slot16.getItemMeta().getDisplayName());
                        //System.out.println(ExtractLore.extractLevelFromName(slot16.getItemMeta().getDisplayName()));
                    }
                    greenCheck = createCustomItem(Material.PAPER, "§aConfirm", 10109, "§aCost: §6" + String.format("%.2f",costForReroll));
                } else {
                    costForLevel = 25 * Math.pow( PlayerStats.getPlayerStats(player).getLevel(), (1+(rarity*0.67)));
                    greenCheck = createCustomItem(Material.PAPER, "§aConfirm", 10109, "§aCost: §6" + String.format("%.2f",costForLevel));
                }
                guiInventory.setItem(22, greenCheck);
                player.sendMessage("§aItem placed in slot 1");
            } else if (slot16 == null || slot16.getItemMeta().getDisplayName().contains("Arrow Check")) {
                if (clickedItem.getMaxStackSize() > 1) {
                    player.sendMessage("§cThis Item Is Stackable!");
                    return;
                }
                if (ExtractLore.ExtractItemName(guiInventory.getItem(10).getItemMeta().getDisplayName())
                        .equals(ExtractLore.ExtractItemName(clickedItem.getItemMeta().getDisplayName())) &&  //same item name
                        ExtractLore.extractRarityFromLore(guiInventory.getItem(10).getItemMeta().getLore()) ==
                                ExtractLore.extractRarityFromLore(clickedItem.getItemMeta().getLore())) { //same rarity
                    guiInventory.setItem(16, clickedItem);
                    player.getInventory().setItem(event.getSlot(), null);  // Remove item from player's cursor
                    int rarity = ExtractLore.extractRarityFromLore(clickedItem.getLore());
                    ItemStack greenCheck;
                    if (reroll) {
                        if (ExtractLore.extractLevelFromName(clickedItem.getItemMeta().getDisplayName()) >= ExtractLore.extractLevelFromName(slot10.getItemMeta().getDisplayName())) {
                            costForReroll = 10 * Math.pow(ExtractLore.extractLevelFromName(clickedItem.getItemMeta().getDisplayName()), (1+(rarity*0.45)));
                        } else {
                            costForReroll = 10 * Math.pow(ExtractLore.extractLevelFromName(slot10.getItemMeta().getDisplayName()), (1+(rarity*0.45)));
                        }
                        greenCheck = createCustomItem(Material.PAPER, "§aConfirm", 10109, "§aCost: §6" + String.format("%.2f",costForReroll));
                    } else {
                        costForLevel = 25 * Math.pow( PlayerStats.getPlayerStats(player).getLevel(), (1+(rarity*0.67)));
                        greenCheck = createCustomItem(Material.PAPER, "§aConfirm", 10109, "§aCost: §6" + String.format("%.2f",costForLevel));
                    }
                    guiInventory.setItem(22, greenCheck);
                    player.sendMessage("§aItem placed in slot 2");
                } else {
                    player.sendMessage("§cNot the same item! (Wrong Rarity or Wrong Item Type!)");
                }
            } else {
                player.sendMessage("§cBoth slots are already filled.");
            }
        }
    }

    public Component rerollDisplayName(ItemMeta itemMeta, Player player, PersistentDataContainer pdc) {
        List<String> lore = itemMeta.getLore();
        System.out.println(itemMeta.getDisplayName());
        int level = ExtractLore.extractLevelFromName(itemMeta.getDisplayName());
        int rarityIndex = ExtractLore.extractRarityFromLore(lore);
        double rarityMulti = GachaManager.getRarityMultiplier(rarityIndex);
        double levelMulti = Calculations.playerLevelMultiplier(level);
        // List to hold percentages of stats
        List<Double> statPercentages = new ArrayList<>();
        List<NamespacedKey> keys = new ArrayList<>();
        keys.add(new NamespacedKey(plugin, "MinMaxDamage"));
        keys.add(new NamespacedKey(plugin, "MinMaxArmor"));
        keys.add(new NamespacedKey(plugin, "MinMaxHP"));
        for (NamespacedKey key : keys) {
            if (pdc.has(key, PersistentDataType.STRING)) {
                System.out.println("test");
                List<String> minMax = ExtractLore.extractMinAndMaxFromPDC(pdc, key);
                double min = Double.parseDouble(minMax.get(0));
                double max = Double.parseDouble(minMax.get(1));
                min = min * rarityMulti * levelMulti;
                max = max * rarityMulti * levelMulti;
                if (key.equals(new NamespacedKey(plugin, "MinMaxDamage"))) {
                    statPercentages.add(GachaManager.calculatePercentage(ExtractLore.getDamageFromLore(lore), min, max));
                } else if (key.equals(new NamespacedKey(plugin, "MinMaxArmor"))) {
                    System.out.println(min + " " + max);
                    statPercentages.add(GachaManager.calculatePercentage(ExtractLore.getArmorFromLore(lore), min, max));
                } else if (key.equals(new NamespacedKey(plugin, "MinMaxHP"))) {
                    statPercentages.add(GachaManager.calculatePercentage(ExtractLore.getMaxHpFromLore(lore), min, max));
                }
            }
        }
        // Calculate the average percentage of all stats
        double statMedium = 0;
        for (int i = 0; i < statPercentages.size(); i++) {
            System.out.println(statPercentages.get(i));
            statMedium += statPercentages.get(i);
        }
        statMedium = statMedium / statPercentages.size();
        // Format the new percentage as a string to append to the display name
        String percentageDisplay = String.format("(%.0f%%)", statMedium);

        // Create the new display name with the rerolled percentage
        String levelPrefix = "<dark_gray>[<gold>" + level + "<dark_gray>] ";
        String itemName = GachaManager.getRarityColor(rarityIndex) + ExtractLore.ExtractItemName(itemMeta.getDisplayName());
        return MiniMessage.miniMessage().deserialize("<!i>" + levelPrefix + itemName + " " +  percentageDisplay);
    }
}