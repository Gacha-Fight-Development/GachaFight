package me.diu.gachafight.party;

import me.diu.gachafight.GachaFight;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PartyManager {
    private static final int MAX_PARTY_SIZE = 3;
    private static GachaFight plugin;
    public static File configFile;
    private static FileConfiguration config;

    public static void initialize(GachaFight plugin) {
        PartyManager.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "parties.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public static void createParty(Player leader) {
        String leaderUUID = leader.getUniqueId().toString();
        config.set(leaderUUID + ".members", new HashSet<String>());
        saveConfig();
    }

    public static boolean addToParty(Player leader, Player member) {
        String leaderUUID = leader.getUniqueId().toString();
        Set<String> members = config.getStringList(leaderUUID + ".members").stream().collect(Collectors.toSet());
        if (members.size() < MAX_PARTY_SIZE - 1) {
            members.add(member.getUniqueId().toString());
            config.set(leaderUUID + ".members", members.stream().collect(Collectors.toList()));
            saveConfig();
            return true;
        }
        return false;
    }

    public static void removeFromParty(Player leader, Player member) {
        String leaderUUID = leader.getUniqueId().toString();
        Set<String> members = config.getStringList(leaderUUID + ".members").stream().collect(Collectors.toSet());
        members.remove(member.getUniqueId().toString());
        if (members.isEmpty()) {
            config.set(leaderUUID, null);
        } else {
            config.set(leaderUUID + ".members", members.stream().collect(Collectors.toList()));
        }
        saveConfig();
    }

    public static Set<Player> getPartyMembers(Player leader) {
        String leaderUUID = leader.getUniqueId().toString();
        return config.getStringList(leaderUUID + ".members").stream()
                .map(uuid -> Bukkit.getPlayer(UUID.fromString(uuid)))
                .filter(player -> player != null)
                .collect(Collectors.toSet());
    }

    public static boolean isInParty(Player player) {
        String playerUUID = player.getUniqueId().toString();
        if (config.contains(playerUUID)) {
            return true;
        }
        for (String leaderUUID : config.getKeys(false)) {
            return true;
        }
        return false;
    }

    public static Player getPartyLeader(Player player) {
        String playerUUID = player.getUniqueId().toString();
        if (config.contains(playerUUID)) {
            return player;
        }
        for (String leaderUUID : config.getKeys(false)) {
            {
                return Bukkit.getPlayer(UUID.fromString(leaderUUID));
            }
        }
        return null;

    }

    public static boolean isPartyFull(Player leader) {
        String leaderUUID = leader.getUniqueId().toString();
        return config.getStringList(leaderUUID + ".members").size() >= MAX_PARTY_SIZE - 1;
    }

    public static int getPartySize(Player leader) {
        String leaderUUID = leader.getUniqueId().toString();
        return config.getStringList(leaderUUID + ".members").size() + 1; // +1 to include the leader
    }

    private static void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save parties.yml!");
            e.printStackTrace();
        }
    }
}

