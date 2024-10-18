package me.diu.gachafight.guild;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;

public class GuildManager {
    public static final int MAX_GUILD_SIZE = 50;
    private static GachaFight plugin;
    public static File configFile;
    private static FileConfiguration config;

    public static void initialize(GachaFight plugin) {
        GuildManager.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "guilds.yml");
        if (!configFile.exists()) {
            plugin.saveResource("guilds.yml", true);
            configFile = new File(plugin.getDataFolder(), "guilds.yml");
            plugin.getLogger().info("Created new guilds.yml file.");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        if (config == null) {
            plugin.getLogger().severe("Failed to load guilds.yml configuration!");
        } else {
            plugin.getLogger().info("Successfully loaded guilds.yml configuration.");
            plugin.getLogger().info("Number of guilds loaded: " + config.getKeys(false).size());
        }
    }

    public static void createGuild(OfflinePlayer leader, String guildName) {
        String guildId = UUID.randomUUID().toString();
        config.set(guildId + ".name", guildName);
        config.set(guildId + ".leader", leader.getUniqueId().toString());
        config.set(guildId + ".coLeader", "");
        config.set(guildId + ".members", new ArrayList<String>());
        config.set(guildId + ".level", 1);
        config.set(guildId + ".exp", 0);
        config.set(guildId + ".upgrades", new HashMap<String, Integer>());
        config.set(guildId + ".chatIcon", "&fG");
        config.set(guildId + ".lastLeaderActivity", System.currentTimeMillis());
        saveConfig();
    }

    public static boolean addToGuild(String guildId, OfflinePlayer member) {
        List<String> members = config.getStringList(guildId + ".members");
        if (members.size() < MAX_GUILD_SIZE - 1) {
            members.add(member.getUniqueId().toString());
            config.set(guildId + ".members", members);
            saveConfig();
            return true;
        }
        return false;
    }

    public static void removeFromGuild(String guildId, OfflinePlayer member) {
        List<String> members = config.getStringList(guildId + ".members");
        String memberUUID = member.getUniqueId().toString();
        String leaderUUID = config.getString(guildId + ".leader");

        if (leaderUUID.equals(memberUUID)) {
            // If the leader is leaving, disband the guild
            config.set(guildId, null);
            // Notify all online members that the guild has been disbanded
            for (String uuid : members) {
                OfflinePlayer guildMember = getOfflinePlayer(uuid);
                if (guildMember != null && guildMember.isOnline()) {
                    guildMember.getPlayer().sendMessage(ColorChat.chat("&cThe guild has been disbanded as the leader left."));
                }
            }
        } else {
            // If a regular member is leaving, just remove them from the guild
            members.remove(memberUUID);
            config.set(guildId + ".members", members);

            // Notify the remaining online guild members
            for (String uuid : members) {
                OfflinePlayer guildMember = getOfflinePlayer(uuid);
                if (guildMember != null && guildMember.isOnline()) {
                    guildMember.getPlayer().sendMessage(ColorChat.chat("&e" + member.getName() + " has left the guild."));
                }
            }
        }

        saveConfig();
    }

    public static Set<OfflinePlayer> getGuildMembers(String guildId) {
        Set<OfflinePlayer> members = new HashSet<>();

        // Add all members, including those who are offline
        members.addAll(config.getStringList(guildId + ".members").stream()
                .map(GuildManager::getOfflinePlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        // Add the leader to the set
        OfflinePlayer leader = getOfflinePlayer(config.getString(guildId + ".leader"));
        if (leader != null) {
            members.add(leader);
        }

        return members;
    }

    public static String getGuildId(OfflinePlayer player) {
        if (player == null) {
            return null;
        }
        String playerUUID = player.getUniqueId().toString();
        for (String guildId : config.getKeys(false)) {
            if (config.getString(guildId + ".leader").equals(playerUUID) ||
                    config.getStringList(guildId + ".members").contains(playerUUID)) {
                return guildId;
            }
        }
        return null;
    }

    public static boolean isInGuild(OfflinePlayer player) {
        return getGuildId(player) != null;
    }

    public static void addGuildExp(String guildId, int exp) {
        int currentExp = config.getInt(guildId + ".exp");
        int currentLevel = config.getInt(guildId + ".level");
        int newExp = currentExp + exp;
        int expForNextLevel = getExpForNextLevel(currentLevel);

        while (newExp >= expForNextLevel) {
            newExp -= expForNextLevel;
            currentLevel++;
            expForNextLevel = getExpForNextLevel(currentLevel);
        }

        config.set(guildId + ".exp", newExp);
        config.set(guildId + ".level", currentLevel);
        saveConfig();
    }

    public static boolean upgradeGuild(String guildId, String upgrade) {
        // Check if the upgrade is valid
        if (!isValidUpgrade(upgrade)) {
            return false;
        }

        int currentLevel = config.getInt(guildId + ".upgrades." + upgrade, 0);
        config.set(guildId + ".upgrades." + upgrade, currentLevel + 1);

        // Deduct resources (you need to implement this logic)
        deductUpgradeResources(guildId, upgrade);

        saveConfig();
        return true;
    }

    private static boolean isValidUpgrade(String upgrade) {
        // Add logic to check if the upgrade is valid
        // For example, you might have a list of valid upgrades
        List<String> validUpgrades = Arrays.asList("capacity", "exp_boost", "resource_generation");
        return validUpgrades.contains(upgrade);
    }

    private static int calculateRequiredResources(String upgrade, int currentLevel) {
        // Add logic to calculate required resources based on the upgrade and current level
        // This is just a simple example, you should adjust it based on your game's balance
        return (currentLevel + 1) * 1000;
    }

    private static void deductUpgradeResources(String guildId, String upgrade) {
        // Add logic to deduct resources from the guild after a successful upgrade
        int currentLevel = config.getInt(guildId + ".upgrades." + upgrade, 0);
        int requiredResources = calculateRequiredResources(upgrade, currentLevel);
        int guildResources = config.getInt(guildId + ".resources", 0);
        config.set(guildId + ".resources", guildResources - requiredResources);
    }

    public static String getChatIcon(String guildId) {
        return config.getString(guildId + ".chatIcon", "&7[G]");
    }

    public static void setChatIcon(String guildId, String icon) {
        config.set(guildId + ".chatIcon", icon);
        saveConfig();
    }

    public static int getExpForNextLevel(int currentLevel) {
        // This is a simple formula, you can adjust it as needed
        return 1000 * currentLevel;
    }

    private static void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save guilds.yml!");
            e.printStackTrace();
        }
    }

    public static OfflinePlayer getOfflinePlayer(String uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(uuid);
        if (player == null) {
            player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        }
        return player;
    }
    public static List<String> getAllGuildIds() {
        return new ArrayList<>(config.getKeys(false));
    }
    public static List<String> getAllGuildNames() {
        return getAllGuildIds().stream()
                .map(GuildManager::getGuildName)
                .collect(Collectors.toList());
    }
    public static List<OfflinePlayer> getAllGuildLeaders() {
        return getAllGuildIds().stream()
                .map(GuildManager::getGuildLeader)
                .collect(Collectors.toList());
    }
    public static List<Integer> getAllGuildLevels() {
        return getAllGuildIds().stream()
                .map(GuildManager::getGuildLevel)
                .collect(Collectors.toList());
    }

    public static String getGuildName(String guildId) {
        return config.getString(guildId + ".name");
    }

    public static int getGuildLevel(String guildId) {
        return config.getInt(guildId + ".level");
    }
    public static void setGuildLevel(String guildId, int level) {
        config.set(guildId + ".level", level);
        saveConfig();
    }
    public static void setGuildName(String guildId, String name) {
        config.set(guildId + ".name", name);
        saveConfig();
    }
    public static void setGuildLeader(String guildId, OfflinePlayer leader) {
        config.set(guildId + ".leader", leader.getUniqueId().toString());
        saveConfig();
    }
    public static void setGuildExp(String guildId, int exp) {
        config.set(guildId + ".exp", exp);
        saveConfig();
    }
    public static Map<String, Integer> getGuildUpgrades(String guildId) {
        Map<String, Integer> upgrades = new HashMap<>();
        if (config.contains(guildId + ".upgrades")) {
            ConfigurationSection upgradesSection = config.getConfigurationSection(guildId + ".upgrades");
            if (upgradesSection != null) {
                for (String key : upgradesSection.getKeys(false)) {
                    upgrades.put(key, upgradesSection.getInt(key));
                }
            }
        }
        return upgrades;
    }
    public static void setGuildUpgrades(String guildId, Map<String, Integer> upgrades) {
        config.set(guildId + ".upgrades", upgrades);
        saveConfig();
    }
    public static void resetGuildUpgrades(String guildId) {
        config.set(guildId + ".upgrades", new HashMap<>());
        saveConfig();
    }
    public static int getGuildUpgradeLevel(String guildId, String upgrade) {
        return config.getInt(guildId + ".upgrades." + upgrade, 0);
    }
    public static void setGuildUpgradeLevel(String guildId, String upgrade, int level) {
        config.set(guildId + ".upgrades." + upgrade, level);
        saveConfig();
    }

    public static int getGuildExp(String guildId) {
        return config.getInt(guildId + ".exp");
    }

    public static OfflinePlayer getGuildLeader(String guildId) {
        String leaderUUID = config.getString(guildId + ".leader");
        return leaderUUID != null ? Bukkit.getOfflinePlayer(UUID.fromString(leaderUUID)) : null;
    }

    public static boolean isGuildLeader(OfflinePlayer player, String guildId) {
        String leaderUUID = config.getString(guildId + ".leader");
        return leaderUUID != null && leaderUUID.equals(player.getUniqueId().toString());
    }

    public static boolean isGuildFull(String guildId) {
        List<String> members = config.getStringList(guildId + ".members");
        // Add 1 to account for the leader who is not in the members list
        return members.size() + 1 >= MAX_GUILD_SIZE;
    }
    public static void setCoLeader(String guildId, OfflinePlayer coLeader) {
        config.set(guildId + ".coLeader", coLeader.getUniqueId().toString());
        saveConfig();
    }

    public static OfflinePlayer getCoLeader(String guildId) {
        String coLeaderUUID = config.getString(guildId + ".coLeader");
        return coLeaderUUID != null && !coLeaderUUID.isEmpty() ? getOfflinePlayer(coLeaderUUID) : null;
    }

    public static boolean isCoLeader(OfflinePlayer player, String guildId) {
        String coLeaderUUID = config.getString(guildId + ".coLeader");
        return coLeaderUUID != null && coLeaderUUID.equals(player.getUniqueId().toString());
    }

    public static void updateLeaderActivity(String guildId) {
        config.set(guildId + ".lastLeaderActivity", System.currentTimeMillis());
        saveConfig();
    }

    public static void checkLeaderInactivity() {
        long currentTime = System.currentTimeMillis();
        long sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L;

        for (String guildId : config.getKeys(false)) {
            long lastActivity = config.getLong(guildId + ".lastLeaderActivity");
            if (currentTime - lastActivity > sevenDaysInMillis) {
                OfflinePlayer coLeader = getCoLeader(guildId);
                if (coLeader != null) {
                    promoteCoLeaderToLeader(guildId);
                } else {
                    handleInactiveGuildWithoutCoLeader(guildId);
                }
            }
        }
    }
    private static void handleInactiveGuildWithoutCoLeader(String guildId) {
        // Get the guild name for notifications

        // Get all guild members
        List<String> memberUUIDs = config.getStringList(guildId + ".members");
        String leaderUUID = config.getString(guildId + ".leader");
        String guildName = getGuildName(guildId);
        if (leaderUUID != null) {
            memberUUIDs.add(leaderUUID);
        }

        // Notify all online members about the guild disbanding
        for (String memberUUID : memberUUIDs) {
            Player member = Bukkit.getPlayer(UUID.fromString(memberUUID));
            if (member != null && member.isOnline()) {
            }
        }

        // Remove the guild from the configuration
        config.set(guildId, null);
        saveConfig();

        // Log the event
        plugin.getLogger().info("Guild " + guildName + " (ID: " + guildId + ") has been disbanded due to leader inactivity.");
    }


    private static void promoteCoLeaderToLeader(String guildId) {
        String coLeaderUUID = config.getString(guildId + ".coLeader");
        String leaderUUID = config.getString(guildId + ".leader");
        String guildName = getGuildName(guildId);

        config.set(guildId + ".leader", coLeaderUUID);
        config.set(guildId + ".coLeader", "");
        List<String> members = config.getStringList(guildId + ".members");
        members.add(leaderUUID);
        members.remove(coLeaderUUID);
        config.set(guildId + ".members", members);

        saveConfig();

        // Notify online members about the leadership change
        for (OfflinePlayer member : getGuildMembers(guildId)) {
            if (member != null && member.isOnline()) {
                UUID uuid = member.getUniqueId();
                Player memberOnline = Bukkit.getPlayer(uuid);
                memberOnline.sendMessage(ColorChat.chat("&6The leadership of " + guildName + " has changed due to inactivity."));
            }
        }
    }

    public static void promoteToLeader(String guildId, OfflinePlayer newLeader) {
        String oldLeaderUUID = config.getString(guildId + ".leader");
        config.set(guildId + ".leader", newLeader.getUniqueId().toString());
        config.set(guildId + ".lastLeaderActivity", System.currentTimeMillis());

        List<String> members = config.getStringList(guildId + ".members");
        members.add(oldLeaderUUID);
        members.remove(newLeader.getUniqueId().toString());
        config.set(guildId + ".members", members);

        saveConfig();
    }
    public static void removeCoLeader(String guildId) {
        config.set(guildId + ".coLeader", "");
        saveConfig();
    }

    public static String getGuildChatIcon(String guildId) {
        if (config == null || guildId == null) {
            return "";
        }
        return ColorChat.chat("&6[&r" + config.getString(guildId + ".chatIcon", "") + "&6]");
    }
}
