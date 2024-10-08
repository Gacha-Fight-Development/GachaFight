package me.diu.gachafight.utils;

import io.lumine.mythic.bukkit.utils.lib.lang3.tuple.Pair;
import me.diu.gachafight.gacha.managers.GachaManager;
import me.diu.gachafight.playerstats.PlayerStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractLore {

    // Helper method to extract damage from lore
    public static double getDamageFromLore(List<String> lore) {
        for (String loreLine : lore) {
            if (loreLine.contains("Damage:")) {
                return extractStatFromLore(loreLine, "🗡 &7Damage: ");
            }
        }
        return 0;
    }

    // Helper method to extract armor from lore
    public static double getArmorFromLore(List<String> lore) {
        for (String loreLine : lore) {
            if (loreLine.contains("Armor:")) {
                return extractStatFromLore(loreLine, "&a🛡 &7Armor: &a");
            }
        }
        return 0;
    }

    public static double getCritFromLore(List<String> lore) {
        for (String line : lore) {
            if (line.contains("Crit:")) {
                return extractStatFromLore(line, "Crit:");
            }
        }
        return 0;
    }

    public static double getMaxHpFromLore(List<String> lore) {
        for (String line : lore) {
            if (line.contains("HP:")) {
                return extractStatFromLore(line, "&#FB035F❤ &7HP: &#FB035F");
            }
        }
        return 0;
    }
    // General method to extract a stat value from a lore line
    public static double extractStatFromLore(String loreLine, String prefix) {

        // Find the index where the prefix ends
        int prefixEndIndex = loreLine.indexOf(prefix) + prefix.length();

        // Extract the part of the string after the prefix
        String statPart = loreLine.substring(prefixEndIndex).trim();

        // Remove all color codes and non-numeric characters before and after the stat using regex
        statPart = statPart.replaceAll("§[0-9A-Fa-fK-Ok-orx]|&#[A-Fa-f0-9]{6}", ""); // Remove Minecraft color codes and hex codes
        statPart = statPart.replaceAll("[^0-9.]", "").trim(); // Keep only numeric characters and the decimal point

        try {
            // Parse the cleaned stat part
            double statValue = Double.parseDouble(statPart);
            return statValue;
        } catch (NumberFormatException e) {
            System.out.println("Failed to parse stat value from lore: " + loreLine + ", Error: " + e.getMessage());
            return 0;
        }
    }


    public static double findMaxStat(String lore, Player player, boolean withPlayerLevel, Boolean withRarity, Double rarity) {
        PlayerStats playerStats = PlayerStats.getPlayerStats(player);
        int playerLevel = playerStats.getLevel();
        double boostMultiplier = Calculations.playerLevelMultiplier(playerLevel);

        double maxStat = 0;
        if (lore != null) {
            // Detect and extract all stat lines (e.g., Damage, Armor, Crit, Max HP)
            String[] parts = extractStatRangeFromLore(lore);
            if (parts != null && parts.length == 2) {
                if (withPlayerLevel) {
                    if (withRarity) {
                        double currentMaxStat = Double.parseDouble(parts[1].trim()) * boostMultiplier * rarity;
                        if (currentMaxStat > maxStat) {
                            maxStat = currentMaxStat;  // Update maxStat if the current one is larger
                        }
                    } else {
                        double currentMaxStat = Double.parseDouble(parts[1].trim()) * boostMultiplier;
                        if (currentMaxStat > maxStat) {
                            maxStat = currentMaxStat;  // Update maxStat if the current one is larger
                        }
                    }
                } else { //without level boost
                    if (withRarity) {
                        double currentMaxStat = Double.parseDouble(parts[1].trim()) * rarity;
                        if (currentMaxStat > maxStat) {
                            maxStat = currentMaxStat;  // Update maxStat if the current one is larger
                        }
                    } else {
                        double currentMaxStat = Double.parseDouble(parts[1].trim());
                        if (currentMaxStat > maxStat) {
                            maxStat = currentMaxStat;  // Update maxStat if the current one is larger
                        }
                    }
                }
            }
        }
        return maxStat;
    }

    public static double findMinStat(String lore, Player player, boolean withPlayerLevel, Boolean withRarity, Double rarity) {
        PlayerStats playerStats = PlayerStats.getPlayerStats(player);
        int playerLevel = playerStats.getLevel();
        double boostMultiplier = Calculations.playerLevelMultiplier(playerLevel);

        double minStat = 0;
        if (lore != null) {
            //String plainText = PlainTextComponentSerializer.plainText().serialize(loreLine);
            // Detect and extract all stat lines (e.g., Damage, Armor, Crit, Max HP)
            String[] parts = extractStatRangeFromLore(lore);
            if (parts != null && parts.length == 2) {
                if (withPlayerLevel) {
                    if (withRarity) {
                        double currentMinStat = Double.parseDouble(parts[0].trim()) * boostMultiplier * rarity;
                        if (currentMinStat > minStat) {
                            minStat = currentMinStat;  // Update minStat if the current one is smaller
                        }
                    } else {
                        double currentMinStat = Double.parseDouble(parts[0].trim()) * boostMultiplier;
                        if (currentMinStat > minStat) {
                            minStat = currentMinStat;  // Update minStat if the current one is smaller
                        }
                    }
                } else {
                    if (withRarity) {
                        double currentMinStat = Double.parseDouble(parts[0].trim()) * rarity;
                        if (currentMinStat > minStat) {
                            minStat = currentMinStat;  // Update minStat if the current one is smaller
                        }
                    } else {
                        double currentMinStat = Double.parseDouble(parts[0].trim());
                        if (currentMinStat > minStat) {
                            minStat = currentMinStat;  // Update minStat if the current one is smaller
                        }
                    }
                }
            }
        }
        return minStat; // Return 0 if no stat found
    }

    // Utility method to extract the stat range (min/max) from a lore line
    private static String[] extractStatRangeFromLore(String loreLine) {
        // Look for a pattern like "X / Y" where X is the minimum stat and Y is the maximum stat
        String regex = "(\\d+(\\.\\d+)?)/(\\d+(\\.\\d+)?)";  // This regex matches integers and decimal numbers

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(loreLine);

        // Check if the pattern is found in the loreLine
        if (matcher.find()) {
            String minStat = matcher.group(1);  // Group 1 corresponds to the number before the slash (with optional decimal)
            String maxStat = matcher.group(3);  // Group 3 corresponds to the number after the slash (with optional decimal)

            return new String[]{minStat, maxStat};
        }
        return null;  // Return null if no valid stat range is found
    }


    public static int extractLevelFromName(String itemName) {
        String cleanedName = itemName.replaceAll("§[0-9A-FK-ORa-fk-or]", "");
        // Regular expression to match a number inside square brackets at the start of the string
        String regex = "\\[(\\d+)]";

        // Create a Pattern object to compile the regex
        Pattern pattern = Pattern.compile(regex);

        // Create a Matcher object to find matches in the itemName
        Matcher matcher = pattern.matcher(cleanedName);

        // Check if a match is found
        if (matcher.find()) {
            try {
                // Extract the level from the matched group (the number inside the brackets)
                String levelStr = matcher.group(1);  // group(1) refers to the part inside the brackets
                return Integer.parseInt(levelStr);  // Convert the level string to an integer
            } catch (NumberFormatException e) {
                System.out.println("Failed to parse level from item name: " + itemName + ", Error: " + e.getMessage());
                return 0;  // Return 0 if there's a parsing issue
            }
        }
        // Return 0 if no level is found in the item name
        return 0;
    }

    public static double extractPercentageFromName(String itemName) {
        // Regular expression to match a percentage in the format "50%"
        String regex = "\\((\\d+)%\\)";

        // Compile the pattern
        Pattern pattern = Pattern.compile(regex);

        // Match the pattern in the itemName
        Matcher matcher = pattern.matcher(itemName);

        // Check if a match is found
        if (matcher.find()) {
            try {
                // Extract the percentage value as a string (the number before the % sign)
                String percentageStr = matcher.group(1);  // group(1) refers to the part inside the parentheses
                // Convert the percentage string to a double and divide by 100 to get the actual percentage
                return Double.parseDouble(percentageStr) / 100.0;
            } catch (NumberFormatException e) {
                System.out.println("Failed to parse percentage from item name: " + itemName + ", Error: " + e.getMessage());
                return 0.0;  // Return 0.0 if there's a parsing issue
            }
        }

        // Return 0.0 if no percentage is found in the item name
        return 0.0;
    }

    public static List<String> extractMinAndMaxFromPDC(PersistentDataContainer container, NamespacedKey key) {
        // Retrieve the stored value from the PersistentDataContainer
        String value = container.get(key, PersistentDataType.STRING);

        if (value != null && value.contains("/")) {
            // Split the string into two parts by "/"
            String[] parts = value.split("/");

            if (parts.length == 2) {
                // Create a List containing the min and max values
                return Arrays.asList(parts[0], parts[1]);
            }
        }

        // Return null or an empty list if the value is invalid
        return Collections.emptyList();
    }

    public static int extractRarityFromLore(List<String> lore) {
        for (String loreLine : lore) {
            if (loreLine.contains("⮞ ")) {
                // Check for rarity keywords in the lore line
                if (loreLine.contains(" Common")) {
                    return 0;
                } else if (loreLine.contains(" Uncommon")) {
                    return 1;
                } else if (loreLine.contains(" Rare")) {
                    return 2;
                } else if (loreLine.contains(" Epic")) {
                    return 3;
                } else if (loreLine.contains(" Unique")) {
                    return 4;
                } else if (loreLine.contains(" Legendary")) {
                    return 5;
                } else if (loreLine.contains(" Mythic")) {
                    return 6;
                } else if (loreLine.contains(" Event")) {
                    return 7;
                }
            }
        }
        return 0; // Return 0 if no rarity is found
    }

    public static int getLoreLine(List<String> lore, String keyword) {
        // Iterate through all the lore lines
        for (int i = 0; i < lore.size(); i++) {
            // Check if the current lore line contains the keyword
            if (lore.get(i).contains(keyword)) {
                return i;  // Return the line number (index) if the keyword is found
            }
        }
        return -1;  // Return -1 if no matching line is found
    }
    public static String ExtractItemName(String displayName) {
        // Remove color codes from the display name
        String cleanedDisplayName = displayName.replaceAll("§[0-9A-FK-ORa-fk-or]", "");

        // Regular expression to capture the part of the name between the level and the percentage
        String regex = "\\[\\d+\\] (.+?) \\(\\d+%\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(cleanedDisplayName);

        // If the pattern matches, extract the item name
        if (matcher.find()) {
            // Group 1 will contain the item name part between the level and percentage
            return matcher.group(1);
        }

        // If no match is found, return an empty string or original name
        return cleanedDisplayName;
    }

    public static String[] extractHealingAndDuration(String lore) {
        // Remove Minecraft color codes, like §7
        String cleanedLore = lore.replaceAll("§[0-9a-fk-or]", "");

        // Use regular expressions to capture the healing amount and duration
        String healingAmount = "";
        String duration = "";

        // Pattern to match the healing amount (e.g., 0.5)
        Pattern healingPattern = java.util.regex.Pattern.compile("([0-9]*\\.?[0-9]+)HP/s");
        Matcher healingMatcher = healingPattern.matcher(cleanedLore);

        // Pattern to match the duration (e.g., 5)
        Pattern durationPattern = java.util.regex.Pattern.compile("\\((\\d+)s\\)");
        Matcher durationMatcher = durationPattern.matcher(cleanedLore);

        // If matches are found, extract values
        if (healingMatcher.find()) {
            healingAmount = healingMatcher.group(1);  // Get the first group (0.5)
        }
        if (durationMatcher.find()) {
            duration = durationMatcher.group(1);  // Get the first group (5)
        }

        // Return the result as a string array [healingAmount, duration]
        return new String[]{healingAmount, duration};
    }


}
