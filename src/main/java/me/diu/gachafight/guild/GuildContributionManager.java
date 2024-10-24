package me.diu.gachafight.guild;

import me.diu.gachafight.GachaFight;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildContributionManager {
    private static GachaFight plugin;
    private static File configFile;
    private static FileConfiguration config;

    public static void initialize(GachaFight plugin) {
        GuildContributionManager.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "guildcontributions.yml");
        if (!configFile.exists()) {
            plugin.saveResource("guildcontributions.yml", true);
            configFile = new File(plugin.getDataFolder(), "guildcontributions.yml");
            plugin.getLogger().info("Created new guildcontributions.yml file.");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public static void addExpContribution(String guildId, UUID playerUUID, int amount) {
        String path = guildId + ".exp." + playerUUID.toString();
        int currentContribution = config.getInt(path, 0);
        saveConfig();
    }

    public static void addGoldContribution(String guildId, UUID playerUUID, int amount) {
        String path = guildId + ".gold." + playerUUID.toString();
        int currentContribution = config.getInt(path, 0);
        saveConfig();
    }

    public static int getExpContribution(String guildId, UUID playerUUID) {
        return config.getInt(guildId + ".exp." + playerUUID.toString(), 0);
    }

    public static int getGoldContribution(String guildId, UUID playerUUID) {
        return config.getInt(guildId + ".gold." + playerUUID.toString(), 0);
    }

    public static Map<UUID, Integer> getAllExpContributions(String guildId) {
        Map<UUID, Integer> contributions = new HashMap<>();
        if (config.contains(guildId + ".exp")) {
            for (String uuidString : config.getConfigurationSection(guildId + ".exp").getKeys(false)) {
                UUID playerUUID = UUID.fromString(uuidString);
                int contribution = config.getInt(guildId + ".exp." + uuidString);
                contributions.put(playerUUID, contribution);
            }
        }
        return contributions;
    }

    public static Map<UUID, Integer> getAllGoldContributions(String guildId) {
        Map<UUID, Integer> contributions = new HashMap<>();
        if (config.contains(guildId + ".gold")) {
            for (String uuidString : config.getConfigurationSection(guildId + ".gold").getKeys(false)) {
                UUID playerUUID = UUID.fromString(uuidString);
                int contribution = config.getInt(guildId + ".gold." + uuidString);
                contributions.put(playerUUID, contribution);
            }
        }
        return contributions;
    }

    public static void resetContributions(String guildId) {
        config.set(guildId, null);
        saveConfig();
    }

    private static void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save guildcontributions.yml!");
            e.printStackTrace();
        }
    }
}
