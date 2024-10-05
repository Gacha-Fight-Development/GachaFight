package me.diu.gachafight.gacha.managers;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.gacha.gui.RaritySelectionGUI;
import me.diu.gachafight.quest.Quest;
import me.diu.gachafight.quest.managers.QuestManager;
import me.diu.gachafight.quest.objectives.KeyOpenObjective;
import me.diu.gachafight.quest.utils.QuestUtils;
import me.diu.gachafight.utils.Calculations;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.ExtractLore;
import me.diu.gachafight.utils.SellPriceCalculator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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

    public void openGacha(Player player, ItemStack key) {
        // Logic to consume the Gacha Key
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ColorChat.chat("&cFull Inventory"));
            return;
        }
        // Randomly choose a rarity based on custom probabilities using the key
        int rarityIndex = getRandomRarity(key, player);

        // Get a random item from the chosen rarity's loot table
        ItemStack reward = GachaLootTableManager.getRandomItem(rarityIndex);

        if (reward != null) {
            // Clone the item to avoid modifying the loot table
            ItemStack customizedReward = reward.clone();
            if (isCrystal(customizedReward)) {
                applyCrystalBoost(player, customizedReward);
            } else if (isPotion(customizedReward)) {
                player.getInventory().addItem(customizedReward);
                player.sendMessage(ColorChat.chat("&a+ &6Potion"));
            } else {
                List<String> lore = customizedReward.getItemMeta().getLore();
                List<Double> statPercentages = new ArrayList<>();
                double minStatDamagePDC = 0;
                double maxStatDamagePDC = 0;
                double minStatArmorPDC = 0;
                double maxStatArmorPDC = 0;
                double minStatHPPDC = 0;
                double maxStatHPPDC = 0;
                customizedReward = customizeItem(customizedReward, player, rarityIndex);
                for (String line : lore) {
                    if (line.contains("Damage:")) {
                        double minStat = ExtractLore.findMinStat(line, player, true, true, getRarityMultiplier(rarityIndex));
                        double maxStat = ExtractLore.findMaxStat(line, player, true, true, getRarityMultiplier(rarityIndex));
                        minStatDamagePDC = ExtractLore.findMinStat(line, player, false, false, null);
                        maxStatDamagePDC = ExtractLore.findMaxStat(line, player, false, false, null);
                        System.out.println("Stats: " + minStat + " " + maxStat);
                        statPercentages.add(calculatePercentage(ExtractLore.getDamageFromLore(customizedReward.getLore()), minStat, maxStat));
                    } else if (line.contains("Armor:")) {
                        double minStat = ExtractLore.findMinStat(line, player, true, true, getRarityMultiplier(rarityIndex));
                        double maxStat = ExtractLore.findMaxStat(line, player,  true, true, getRarityMultiplier(rarityIndex));
                        minStatArmorPDC = ExtractLore.findMinStat(line, player,  false, false, null);
                        maxStatArmorPDC = ExtractLore.findMaxStat(line, player,  false, false, null);
                        System.out.println("Stats: " + minStat + " " + maxStat);
                        statPercentages.add(calculatePercentage(ExtractLore.getArmorFromLore(customizedReward.getLore()), minStat, maxStat));
                    } else if (line.contains("HP:")) {
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
                    statMedium += statPercentages.get(i);
                }
                statMedium = statMedium / statPercentages.size();
                String percentageDisplay = String.format("(%.0f%%)", statMedium);
                ItemMeta meta = customizedReward.getItemMeta();

                if (meta != null) {
                    PersistentDataContainer pdc = meta.getPersistentDataContainer();
                    for (int i = 0; i < statPercentages.size(); i++) {
                        if (minStatDamagePDC != 0) {
                            NamespacedKey minMaxKey = new NamespacedKey(plugin, "MinMaxDamage");
                            pdc.set(minMaxKey, PersistentDataType.STRING, minStatDamagePDC + "/" + maxStatDamagePDC);
                            minStatDamagePDC = 0;
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
                    meta.setDisplayName(meta.getDisplayName() + " " + percentageDisplay);
                    customizedReward.setItemMeta(meta);
                }

                String plainRarityName = PlainTextComponentSerializer.plainText().serialize(
                        MiniMessage.miniMessage().deserialize(RaritySelectionGUI.RARITY_NAMES[rarityIndex])
                );

                User user = luckPerms.getUserManager().getUser(player.getUniqueId());

                boolean itemSold = false;

                // checks rarity (string) and the item percent -statMedium-  (double 0.0 to 100.0) against autoSellCutoff.  if autoSellCutoff is larger, sell the item
                if(player.hasPermission("gacha.autosell") || player.hasPermission("gacha.vip")) {
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
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You received a " + RaritySelectionGUI.RARITY_NAMES[rarityIndex] + " item!"));

                    // Reduce keys by 1
                    key.setAmount(key.getAmount() - 1);
                }


            }
        } else {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "si give 991 1 " + player.getName());
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
            case 3: return 6;  // Epic
            case 4: return 12;  // Unique
            case 5: return 24;  // Legendary
            case 6: return 50; // Mythic
            default: return 1.0; // Common or Event (no multiplier)
        }
    }

    public static String getRarityColor(int rarityIndex) {
        // Define the color for each rarity using MiniMessage formatting
        switch (rarityIndex) {
            case 0: return "<!i><white>"; // Common
            case 1: return "<!i><gray>";  // Uncommon
            case 2: return "<!i><green>"; // Rare
            case 3: return "<!i><blue>";  // Epic
            case 4: return "<!i><purple>"; // Unique
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

        //Quest Detection
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


    private ItemStack customizeItem(ItemStack item, Player player, int rarityIndex) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {

            // Further customization (e.g., lore modifications) continues as before...
            List<Component> lore = meta.lore();
            if (lore == null) {
                lore = new ArrayList<>();
            }

            // Modify the existing lore for both damage and armor stats
            for (int i = 0; i < lore.size(); i++) {
                String plainText = PlainTextComponentSerializer.plainText().serialize(lore.get(i));

                if (plainText.contains("Damage:")) {
                    lore.set(i, generateRandomStatLore(plainText, rarityIndex, "Damage", "<!i><red>🗡 <gray>Damage: <red>", player));
                } else if (plainText.contains("Armor:")) {
                    lore.set(i, generateRandomStatLore(plainText, rarityIndex, "Armor", "<!i><green>🛡 <gray>Armor: <green>", player));
                } else if (plainText.contains("HP:")) {
                    lore.set(i, generateRandomStatLore(plainText, rarityIndex, "HP", "<!i><color:#FB035F>❤ <gray>HP: <color:#FB035F>", player));
                }
            }

            // Add blank line and rarity information in the lore
            lore.add(MiniMessage.miniMessage().deserialize(""));  // Add blank line
            String rarityColor = getRarityColor(rarityIndex);
            String rarityName = RaritySelectionGUI.RARITY_NAMES[rarityIndex];
            lore.add(MiniMessage.miniMessage().deserialize(rarityColor + "⮞ " + rarityName));
            Component newName = MiniMessage.miniMessage().deserialize("<!i><dark_gray>[<gold>" + PlayerStats.getPlayerStats(player).getLevel() + "<dark_gray>] " + getRarityColor(rarityIndex) + PlainTextComponentSerializer.plainText().serialize(meta.displayName()));
            meta.displayName(newName);
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
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



    // Probability table for Common Gacha Key
    private final double[] commonKeyProbabilities = {
            89, // common
            10.4, // uncommon
            0.5, // rare
            0.1,  // epic
            0,  // unique
            0,  // legendary
            0,  // mythic
            0   // event
    };

    private final double[] uncommonKeyProbabilities = {
            55, // common
            44.4, // uncommon
            0.5, // rare
            0.1,  // epic
            0,  // unique
            0,  // legendary
            0,  // mythic
            0   // event
    };

    // Probability table for Rare Gacha Key
    private final double[] rareKeyProbabilities = {
            30, // common
            40, // uncommon
            29.4, // rare
            0.5,  // epic
            0.1,  // unique
            0,  // legendary
            0,  // mythic
            0   // event
    };
    private final double[] epicKeyProbabilities = {
            10, // common
            40, // uncommon
            30, // rare
            19.1,  // epic
            0.8,  // unique
            0.1,  // legendary
            0,  // mythic
            0   // event
    };
    private final double[] uniqueKeyProbabilities = {
            0, // common
            10, // uncommon
            35, // rare
            30,  // epic
            24,  // unique
            0.99,  // legendary
            0.01,  // mythic
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
