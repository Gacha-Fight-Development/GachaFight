package me.diu.gachafight.utils;

public class RarityUtils {
    public static String getRarityColor(String rarityName) {
        switch (rarityName.toLowerCase()) {
            case "common":
                return "&f"; // White
            case "uncommon":
                return "&7"; // Green
            case "rare":
                return "&a";
            case "epic":
                return "&b";
            case "unique":
                return "&5";
            case "legendary":
                return "&6&l"; // Gold
            case "mythic":
                return "&4&l"; // Light Purple
            default:
                return "&7"; // Gray (default)
        }
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
}
