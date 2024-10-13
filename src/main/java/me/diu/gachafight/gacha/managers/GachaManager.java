package me.diu.gachafight.gacha.managers;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.commands.GuideCommand;
import me.diu.gachafight.guides.TutorialGuideSystem;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.gacha.gui.RaritySelectionGUI;
import me.diu.gachafight.quest.Quest;
import me.diu.gachafight.quest.managers.QuestManager;
import me.diu.gachafight.quest.objectives.KeyOpenObjective;
import me.diu.gachafight.quest.utils.QuestUtils;
import me.diu.gachafight.utils.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GachaManager {

    private final GachaFight plugin;
    private final Random random;
    private final LuckPerms luckPerms;
    private final QuestManager questManager;

    public GachaManager(GachaFight plugin, LuckPerms luckPerms, QuestManager questManager) {
        this.plugin = plugin;
        this.random = new Random();
        this.luckPerms = luckPerms;
        this.questManager = questManager;
    }

    // Main method for when players open Gacha chest using keys
    public void openGacha(Player player, ItemStack key) {
        // Checks for Full Inventory
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ColorChat.chat("&cFull Inventory"));
            return;
        }
        // Randomly choose a rarity based on custom probabilities using the key
        int rarityIndex = getRandomRarity(key, player); // Each Rarity has a rarity Index 0 = Common, ect..

        // Get a random item from the chosen rarity's loot table
        ItemStack reward = GachaLootTableManager.getRandomItem(rarityIndex); // gets the loot table from /editgacha
        // then selects a random item inside it

        if (reward != null) {
            // Clone the item to avoid modifying the loot table
            ItemStack customizedReward = reward.clone();
            if (isCrystal(customizedReward)) { //check for crystal (perma stats upgrade) (prob ganna remove this soon)
                applyCrystalBoost(player, customizedReward);
            } else if (isPotion(customizedReward)) { //check for potion (if its potion then no need to modify lore)
                player.getInventory().addItem(customizedReward);
                player.sendMessage(ColorChat.chat("&a+ &6Potion"));
            } else { //Equipment
                // alright this is where it gets confusing
                List<String> lore = customizedReward.getItemMeta().getLore(); // Gets Lore of random item Example: Armor 0.7/1.3
                // Example of customizedReward at this point: DisplayName: [<player lvl>] <item name> | lore: Damage/Armor/HP: 1.23
                customizedReward = customizeItem(customizedReward, player, rarityIndex); // This is Item AFTER LORE MODIFICATION Example: Armor 1.23
                List<Double> statPercentages = new ArrayList<>(); // no need to worry about this for now
                double minStatDamagePDC = 0;
                double maxStatDamagePDC = 0;
                double minStatArmorPDC = 0;
                double maxStatArmorPDC = 0;
                double minStatHPPDC = 0;
                double maxStatHPPDC = 0;
                for (String line : lore) { // variable "lore" has UNMODIFIED Lore Example: Armor: 0.7/1.3
                    if (line.contains("Damage:")) { //Check lore line if Contains "Damage:"
                        // Variable minStat and maxStat is for calculating Quality of Item (Example: 20%)
                        // findMinStat() and findMaxStat checks for if you want to find min & max with or without Rarity Multi or Level multi
                        // Example: minStat = 0.7*levelmulti*rarity | maxStat = 1.3*levelmulti*rarity
                        double minStat = ExtractLore.findMinStat(line, player, true, true, getRarityMultiplier(rarityIndex));
                        double maxStat = ExtractLore.findMaxStat(line, player, true, true, getRarityMultiplier(rarityIndex));
                        //PDC Values are RAW Values meaning 0.7/1.3 -> 0.7 | 1.3
                        //BUT you cannot use these values for Item Quality because live example: 1/2.5 gives a 12.58 armor so its ganna be over 100%!
                        // You use the variable minStat & maxStat because it includes the Player Level & Rarity

                        minStatDamagePDC = ExtractLore.findMinStat(line, player, false, false, null);
                        maxStatDamagePDC = ExtractLore.findMaxStat(line, player, false, false, null);
                        statPercentages.add(calculatePercentage(ExtractLore.getDamageFromLore(customizedReward.getLore()), minStat, maxStat));
                    } else if (line.contains("Armor:")) { //same thing
                        double minStat = ExtractLore.findMinStat(line, player, true, true, getRarityMultiplier(rarityIndex));
                        double maxStat = ExtractLore.findMaxStat(line, player,  true, true, getRarityMultiplier(rarityIndex));
                        minStatArmorPDC = ExtractLore.findMinStat(line, player,  false, false, null);
                        maxStatArmorPDC = ExtractLore.findMaxStat(line, player,  false, false, null);
                        statPercentages.add(calculatePercentage(ExtractLore.getArmorFromLore(customizedReward.getLore()), minStat, maxStat));
                    } else if (line.contains("HP:")) { // same thing
                        double minStat = ExtractLore.findMinStat(line, player,  true, true, getRarityMultiplier(rarityIndex));
                        double maxStat = ExtractLore.findMaxStat(line, player,  true, true, getRarityMultiplier(rarityIndex));
                        minStatHPPDC = ExtractLore.findMinStat(line, player,  false, false, null);
                        maxStatHPPDC = ExtractLore.findMaxStat(line, player,  false, false, null);
                        statPercentages.add(calculatePercentage(ExtractLore.getMaxHpFromLore(customizedReward.getLore()), minStat, maxStat));
                    }
                }
                // Apply player's level to modify the reward stats
                double statMedium = 0;
                for (int i = 0; i < statPercentages.size(); i++) {
                    statMedium += statPercentages.get(i); //adds all the stats together (ghost armor has 2 stats: Armor & HP)
                    // if Armor is 100% & HP is 50% -> 150%
                }
                statMedium = statMedium / statPercentages.size(); //150% divided by 2 = 75%
                String percentageDisplay = String.format("(%.0f%%)", statMedium); // 75% (rounded)
                ItemMeta meta = customizedReward.getItemMeta(); // AFTER MODIFIED LORE

                if (meta != null) {
                    PersistentDataContainer pdc = meta.getPersistentDataContainer(); //PDC
                    for (int i = 0; i < statPercentages.size(); i++) {
                        if (minStatDamagePDC != 0) {
                            NamespacedKey minMaxKey = new NamespacedKey(plugin, "MinMaxDamage");
                            pdc.set(minMaxKey, PersistentDataType.STRING, minStatDamagePDC + "/" + maxStatDamagePDC);
                            minStatDamagePDC = 0; // SETS TO 0 TO NOT LOOP THIS AGAIN
                        } if (minStatArmorPDC != 0) {
                            NamespacedKey minMaxKey = new NamespacedKey(plugin, "MinMaxArmor");
                            pdc.set(minMaxKey, PersistentDataType.STRING, minStatArmorPDC + "/" + maxStatArmorPDC);
                            minStatArmorPDC = 0;
                        } if (minStatHPPDC != 0) {
                            NamespacedKey minMaxKey = new NamespacedKey(plugin, "MinMaxHP");
                            pdc.set(minMaxKey, PersistentDataType.STRING, minStatHPPDC + "/" + maxStatHPPDC);
                            minStatHPPDC = 0;
                        }
                    }
                    // Convert [<PLAYER LVL>] <ITEM NAME> -> [<PLAYER LVL>] <ITEM NAME> (75%)
                    meta.setDisplayName(meta.getDisplayName() + " " + percentageDisplay);
                    customizedReward.setItemMeta(meta);
                }

                String plainRarityName = PlainTextComponentSerializer.plainText().serialize(
                        MiniMessage.miniMessage().deserialize(RaritySelectionGUI.RARITY_NAMES[rarityIndex])
                );

                User user = luckPerms.getUserManager().getUser(player.getUniqueId());

                boolean itemSold = false;

                // checks rarity (string) and the item percent -statMedium-  (double 0.0 to 100.0) against autoSellCutoff.  if autoSellCutoff is larger, sell the item
                if (player.hasPermission("gacha.autosell") || player.hasPermission("gacha.vip")) {
                    // This checks to see what percent the player has set via the /autosellgacha interface
                    String autoSellPerms;
                    autoSellPerms = user.getNodes().stream()
                            .filter(node -> node.getKey().startsWith("gacha.autosell." + plainRarityName.toLowerCase() + "."))
                            .map(Node::getKey)
                            .collect(Collectors.joining(", "));
                    // Double value of what the player set.  gacha.autosell.rarity.X <<<
                    double autoSellCutoff = ((double)(Integer.parseInt(autoSellPerms.split("\\.")[3])));
                    // If the items percent is less then or equal to the player set percent, autosell item
                    if (autoSellCutoff >= statMedium) {
                        // Auto-sell the item
                        double sellPrice = SellPriceCalculator.calculateSellPrice(customizedReward, rarityIndex);
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Auto-sold " + RaritySelectionGUI.RARITY_NAMES[rarityIndex] + " item for " + String.format("%.1f", sellPrice) + " money!"));
                        PlayerStats playerStats = PlayerStats.getPlayerStats(player);
                        playerStats.setMoney(playerStats.getMoney() + sellPrice);

                        // Reduce keys by 1
                        key.setAmount(key.getAmount() - 1);
                        itemSold = true;
                    }
                }
                // If item was not sold this method will run
                if(!itemSold) {
                    // Give the item to the player
                    player.getInventory().addItem(customizedReward);
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You received a " + getRarityColor(rarityIndex) + RaritySelectionGUI.RARITY_NAMES[rarityIndex] + " item!"));

                    // Reduce keys by 1
                    key.setAmount(key.getAmount() - 1);
                }


            }
        } else {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 991 1 " + player.getName());
        }
        // ==========TUTORIAL============
        if (player.hasPermission("gacha.tutorial") && !player.hasPermission("op")) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission unset gacha.tutorial");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set gacha.tutorial.1");
            TutorialBossBar.showPostGachaChestBossBar(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getGuideSystem().guidePlayerToLocation(player, GuideCommand.preSetLocations.get("tutorialmushroom"));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
            }.runTaskLater(plugin, 85L*2);
        }
    }

    public static double calculatePercentage(double actualStat, double minStat, double maxStat) {
        if (actualStat == 0) return -1;
        if (actualStat < minStat) return 0;
        else if (actualStat > maxStat) return 100;
        return ((actualStat - minStat) / (maxStat - minStat)) * 100; // Calculate percentage within range
    }



    private Component generateRandomStatLore(String loreLine, int rarityIndex, String statType, String prefix, Player player) {
        // Trim whitespace to handle any leading or trailing spaces
        loreLine = loreLine.trim();

        // Normalize both the prefix and the loreLine to ensure they match
        String normalizedLoreLine = PlainTextComponentSerializer.plainText().serialize(MiniMessage.miniMessage().deserialize(loreLine)).trim();
        String normalizedPrefix = PlainTextComponentSerializer.plainText().serialize(MiniMessage.miniMessage().deserialize(prefix)).trim();

        if (normalizedLoreLine.startsWith(normalizedPrefix)) {
            String statRange = normalizedLoreLine.substring(normalizedPrefix.length()).trim(); // Remove the prefix

            try {
                // Split the stat range into min and max values
                String[] parts = statRange.split("/");

                // Ensure the split is correct
                if (parts.length == 2) {
                    double minStat = Double.parseDouble(parts[0].trim());
                    double maxStat = Double.parseDouble(parts[1].trim());

                    // Apply the rarity multiplier
                    double multiplier = getRarityMultiplier(rarityIndex);
                    minStat *= multiplier;
                    maxStat *= multiplier;

                    // Generate a random value between minStat and maxStat, including decimal values
                    PlayerStats playerStats = PlayerStats.getPlayerStats(player);
                    int playerLevel = playerStats.getLevel();
                    double boostMultiplier = Calculations.playerLevelMultiplier(playerLevel);
                    double randomStat = minStat + (random.nextDouble() * (maxStat - minStat));
                    randomStat = randomStat * boostMultiplier;

                    // Return the formatted lore line with the new stat value
                    return MiniMessage.miniMessage().deserialize(prefix + String.format("%.2f", randomStat));
                } else {
                    plugin.getLogger().severe("Invalid stat format for " + statType + " in lore: " + loreLine);
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().severe("Failed to parse " + statType + " range in lore: " + loreLine + ". Error: " + e.getMessage());
            }
        } else {
            plugin.getLogger().severe("Lore line does not start with expected prefix: " + loreLine);
        }

        // Return the original lore line if parsing failed
        return MiniMessage.miniMessage().deserialize(loreLine);
    }




    public static double getRarityMultiplier(int rarityIndex) {
        switch (rarityIndex) {
            case 1: return 1.5;  // Uncommon
            case 2: return 3;  // Rare
            case 3: return 4.5;  // Epic
            case 4: return 6;  // Unique
            case 5: return 7.5;  // Legendary
            case 6: return 10; // Mythic
            default: return 1.0; // Common or Event (no multiplier)
        }
    }

    public static String getRarityColor(int rarityIndex) {
        // Define the color for each rarity using MiniMessage formatting
        switch (rarityIndex) {
            case 0: return "<!i><white>"; // Common
            case 1: return "<!i><gray>";  // Uncommon
            case 2: return "<!i><green>"; // Rare
            case 3: return "<!i><aqua>";  // Epic
            case 4: return "<!i><light_purple>"; // Unique
            case 5: return "<!i><gold>"; // Legendary
            case 6: return "<!i><red>";  // Mythic
            case 7: return "<!i><rainbow>"; // Event/Custom
            default: return "<!i><white>"; // Default to white if something goes wrong
        }
    }

    private int getRandomRarity(ItemStack key, Player player) {
        if (key == null || !key.hasItemMeta()) {
            plugin.getLogger().warning("Gacha key has no ItemMeta, defaulting to common probabilities.");
            return 0;  // Default to common if the item has no metadata
        }

        ItemMeta meta = key.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            plugin.getLogger().warning("Gacha key has no display name, defaulting to common probabilities.");
            return 0;  // Default to common if the item has no display name
        }

        double[] probabilities;
        String displayName = meta.getDisplayName();

        // Quest Detection for Key Opening todo: Make Seperate Method maybe inside quest package or utils package
        for (Quest quest : questManager.getActiveQuestsForPlayer(player)) {
            if (quest.getObjective() instanceof KeyOpenObjective) {
                KeyOpenObjective objective = (KeyOpenObjective) quest.getObjective();

                // Check if the key type matches the objective's target
                if (displayName.contains(objective.getTarget())) {
                    // Increment quest progress
                    QuestUtils.incrementQuestProgress(player, quest, "keyOpen", 1);
                    player.sendMessage("§aProgress updated for quest: " + quest.getName());
                }
            }
        }

        // Determine which probability table to use based on the gacha key's display name
        if (displayName.contains("Common Gacha Key")) {
            probabilities = commonKeyProbabilities;
        } else if (displayName.contains("Uncommon Gacha Key")) {
            probabilities = uncommonKeyProbabilities;
        } else if (displayName.contains("Rare Gacha Key")) {
            probabilities = rareKeyProbabilities;
        } else if (displayName.contains("Epic Gacha Key")) {
            probabilities = epicKeyProbabilities;
        } else if (displayName.contains("Unique Gacha Key")) {
            probabilities = uniqueKeyProbabilities;
        } else if (displayName.contains("Legendary Gacha Key")) {
            probabilities = legendaryKeyProbabilities;
        } else if (displayName.contains("Mythic Gacha Key")) {
            probabilities = mythicKeyProbabilities;
        } else if (displayName.contains("Event Gacha Key")) {
            probabilities = eventKeyProbabilities;
        } else {
            plugin.getLogger().warning("Unknown Gacha Key, defaulting to common probabilities.");
            probabilities = commonKeyProbabilities;  // Default to common probabilities if no key matches
        }

        // Select rarity based on probabilities
        double randomValue = random.nextDouble() * 100;
        double cumulative = 0.0;

        for (int i = 0; i < probabilities.length; i++) {
            cumulative += probabilities[i];
            if (randomValue <= cumulative) {
                return i;
            }
        }
        return 0; // Default to common if something goes wrong
    }

    // Modifying Lore of random item from loottable into lore that is usable
    private ItemStack customizeItem(ItemStack item, Player player, int rarityIndex) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<Component> lore = meta.lore(); //Gets Lore but in a list of Component
            if (lore == null) {
                lore = new ArrayList<>(); //this will be used for meta.lore(lore) (replacing the old lore with new lore basically
            }
            // make a for loop for all lines of the item lore.
            for (int i = 0; i < lore.size(); i++) {
                String plainText = PlainTextComponentSerializer.plainText().serialize(lore.get(i)); // component into string plain text

                if (plainText.contains("Damage:")) { // checks for Damage Lore
                    // Turns Damage min/max -> Damage: random number between min and max Example: Damage: 1/3 -> Damage: 2.22
                    lore.set(i, generateRandomStatLore(plainText, rarityIndex, "Damage", "<!i><red>🗡 <gray>Damage: <red>", player));
                } else if (plainText.contains("Armor:")) { // same thing but for armor
                    lore.set(i, generateRandomStatLore(plainText, rarityIndex, "Armor", "<!i><green>🛡 <gray>Armor: <green>", player));
                } else if (plainText.contains("HP:")) { // same thing but for hp
                    lore.set(i, generateRandomStatLore(plainText, rarityIndex, "HP", "<!i><color:#FB035F>❤ <gray>HP: <color:#FB035F>", player));
                }
            }

            // Add blank line and rarity information in the lore
            lore.add(MiniMessage.miniMessage().deserialize(""));  // Add blank line to look nice :)
            String rarityColor = getRarityColor(rarityIndex); // Look at getRarityColor() should be self explanitory
            String rarityName = RaritySelectionGUI.RARITY_NAMES[rarityIndex]; // Grabs the Name of the rarity using rarityIndex
            lore.add(MiniMessage.miniMessage().deserialize(rarityColor + "⮞ " + rarityName)); // Rarity line lore
            // newName = [<player's level>] <Item Name> | This does not include the quality of sword (%)
            Component newName = MiniMessage.miniMessage().deserialize("<!i><dark_gray>[<gold>" + PlayerStats.getPlayerStats(player).getLevel() + "<dark_gray>] " + getRarityColor(rarityIndex) + PlainTextComponentSerializer.plainText().serialize(meta.displayName()));
            meta.displayName(newName); //sets the display name of the item
            meta.lore(lore); // sets the lore of the item
            item.setItemMeta(meta); //saves all changes to the item
        }
        return item; //return the modified item.
    }

    private boolean isPotion(ItemStack item) {
        if (item.getType() == Material.LEATHER_HORSE_ARMOR && item.getItemMeta().getDisplayName().contains("Potion")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isCrystal(ItemStack item) {
        if (item.getType() == Material.NETHER_STAR && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            String displayName = meta.getDisplayName();
            return displayName != null && (displayName.contains("Crystal"));
        }
        return false;
    }

    // Method to apply the appropriate stat boost based on the type of crystal
    private void applyCrystalBoost(Player player, ItemStack crystal) {
        if (crystal.hasItemMeta()) {
            ItemMeta meta = crystal.getItemMeta();
            String displayName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());

            PlayerStats stats = PlayerStats.getPlayerStats(player);

            if (displayName.contains("HP Crystal")) {
                // Increase HP by 1
                stats.setMaxhp(stats.getMaxhp() + 1);
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Your HP has been increased by 1!"));
            } else if (displayName.contains("Damage Crystal")) {
                // Increase Damage by 0.1
                stats.setDamage(stats.getDamage() + 0.1);
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Your Damage has been increased by 0.1!"));
            } else if (displayName.contains("Armor Crystal")) {
                // Increase Armor by 0.2
                stats.setArmor(stats.getArmor() + 0.2);
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Your Armor has been increased by 0.2!"));
            }
        }
    }


    // GetRandomRarity() gets these Probabilities and select which rarity it will choose.
    // Probability table for Common Gacha Key
    private final double[] commonKeyProbabilities = { //will equal to 100% or if its less than 100 then it will default to common
            89, // common 89%
            10.4, // uncommon 10.4%
            0.4, // rare 0.6%
            0,  // epic
            0,  // unique
            0,  // legendary
            0,  // mythic
            0   // event
    };

    private final double[] uncommonKeyProbabilities = {
            54, // common
            44, // uncommon
            1.8, // rare
            0.2,  // epic
            0,  // unique
            0,  // legendary
            0,  // mythic
            0   // event
    };

    // Probability table for Rare Gacha Key
    private final double[] rareKeyProbabilities = {
            30, // common
            40, // uncommon
            28, // rare
            1.95,  // epic
            0.05,  // unique
            0,  // legendary
            0,  // mythic
            0   // event
    };
    private final double[] epicKeyProbabilities = {
            10, // common 10
            40, // uncommon 40
            30, // rare 30
            19.9,   // epic 19.1
            0.1,  // unique 0.8
            0,  // legendary 0.1
            0,  // mythic
            0   // event
    };
    private final double[] uniqueKeyProbabilities = {
            0, // common
            25, // uncommon
            30, // rare
            25,  // epic
            19.95,  // unique
            0.05,  // legendary
            0,  // mythic
            0   // event
    };

    private final double[] legendaryKeyProbabilities = {
            0, // common
            1, // uncommon
            35, // rare
            30,  // epic
            24,  // unique
            9.5,  // legendary
            0.5,  // mythic
            0   // event
    };


    // Probability table for Mythic Gacha Key
    private final double[] mythicKeyProbabilities = {
            0,  // common
            0,  // uncommon
            10, // rare
            40, // epic
            30, // unique
            15, // legendary
            5,  // mythic
            0   // event
    };

    private final double[] eventKeyProbabilities = {
            80,  // common
            0,  // uncommon
            0, // rare
            0, // epic
            0, // unique
            0, // legendary
            0,  // mythic
            20   // event
    };

}
