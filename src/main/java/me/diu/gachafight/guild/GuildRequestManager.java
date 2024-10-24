package me.diu.gachafight.guild;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.gacha.managers.GachaManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuildRequestManager {
    private final GachaFight plugin;
    public static Map<String, Map<UUID, GuildRequest>> guildRequests;
    public static Map<UUID, Map<String, GuildRequest>> playerRequests;
    private static File requestFile;
    private static YamlConfiguration requestConfig;

    public GuildRequestManager(GachaFight plugin) {
        this.plugin = plugin;
        guildRequests = new ConcurrentHashMap<>();
        playerRequests = new ConcurrentHashMap<>();
        requestFile = new File(plugin.getDataFolder(), "guild_requests.yml");
        requestConfig = YamlConfiguration.loadConfiguration(requestFile);
        loadRequests();
    }

    public static void addRequest(GuildRequest request, Runnable onSuccess, Runnable onFailure) {
        UUID playerUUID = request.getPlayerUUID();
        String guildId = request.getGuildId();

        GachaFight.getInstance().getServer().getScheduler().runTaskAsynchronously(GachaFight.getInstance(), () -> {
            // Check if the request already exists in the YAML file
            if (requestConfig.contains(guildId + "." + playerUUID)) {
                // Request already exists
                GachaFight.getInstance().getServer().getScheduler().runTask(GachaFight.getInstance(), onFailure);
                return;
            }

            // Add the request to the YAML file
            String path = guildId + "." + playerUUID;
            requestConfig.set(path + ".name", request.getPlayerName());
            requestConfig.set(path + ".time", request.getRequestTime().getEpochSecond());

            try {
                requestConfig.save(requestFile);
                // Add to in-memory maps
                guildRequests.computeIfAbsent(guildId, k -> new HashMap<>()).put(playerUUID, request);
                playerRequests.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(guildId, request);
                GachaFight.getInstance().getServer().getScheduler().runTask(GachaFight.getInstance(), onSuccess);
            } catch (IOException e) {
                GachaFight.getInstance().getLogger().severe("Could not save guild request: " + e.getMessage());
                GachaFight.getInstance().getServer().getScheduler().runTask(GachaFight.getInstance(), onFailure);
            }
        });
    }

    public static void removeRequest(String guildId, UUID playerUUID, Runnable onSuccess, Runnable onFailure) {
        GachaFight.getInstance().getServer().getScheduler().runTaskAsynchronously(GachaFight.getInstance(), () -> {
            requestConfig.set(guildId + "." + playerUUID, null);
            try {
                requestConfig.save(requestFile);
                guildRequests.getOrDefault(guildId, new HashMap<>()).remove(playerUUID);
                playerRequests.getOrDefault(playerUUID, new HashMap<>()).remove(guildId);
                GachaFight.getInstance().getServer().getScheduler().runTask(GachaFight.getInstance(), onSuccess);
            } catch (IOException e) {
                GachaFight.getInstance().getLogger().severe("Could not remove guild request: " + e.getMessage());
                GachaFight.getInstance().getServer().getScheduler().runTask(GachaFight.getInstance(), onFailure);
            }
        });
    }

    public static void saveRequests() {
        for (Map.Entry<String, Map<UUID, GuildRequest>> entry : guildRequests.entrySet()) {
            String guildId = entry.getKey();
            for (GuildRequest request : entry.getValue().values()) {
                String path = guildId + "." + request.getPlayerUUID();
                requestConfig.set(path + ".name", request.getPlayerName());
                requestConfig.set(path + ".time", request.getRequestTime().getEpochSecond());
            }
        }
        try {
            requestConfig.save(requestFile);
        } catch (IOException e) {
            GachaFight.getInstance().getLogger().severe("Could not save guild requests: " + e.getMessage());
        }
    }

    public static void loadRequests() {
        GachaFight.getInstance().getServer().getScheduler().runTaskAsynchronously(GachaFight.getInstance(), () -> {
            guildRequests.clear();
            playerRequests.clear();

            for (String guildId : requestConfig.getKeys(false)) {
                if (requestConfig.getConfigurationSection(guildId) == null) continue;

                for (String uuidString : requestConfig.getConfigurationSection(guildId).getKeys(false)) {
                    UUID playerUUID = UUID.fromString(uuidString);
                    String playerName = requestConfig.getString(guildId + "." + uuidString + ".name");
                    long requestTime = requestConfig.getLong(guildId + "." + uuidString + ".time");
                    GuildRequest request = new GuildRequest(playerUUID, playerName, guildId);
                    request.setRequestTime(Instant.ofEpochSecond(requestTime));

                    if (!request.isExpired()) {
                        guildRequests.computeIfAbsent(guildId, k -> new HashMap<>()).put(playerUUID, request);
                        playerRequests.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(guildId, request);
                    } else {
                        // Remove expired requests from the config
                        requestConfig.set(guildId + "." + uuidString, null);
                    }
                }

                // Remove empty guild sections
                if (requestConfig.getConfigurationSection(guildId).getKeys(false).isEmpty()) {
                    requestConfig.set(guildId, null);
                }
            }

            // Save the config if any expired requests were removed
            try {
                requestConfig.save(requestFile);
            } catch (IOException e) {
                GachaFight.getInstance().getLogger().severe("Could not save guild requests after loading: " + e.getMessage());
            }
        });
    }


    public void cleanExpiredRequests() {
        GachaFight.getInstance().getServer().getScheduler().runTaskAsynchronously(GachaFight.getInstance(), () -> {
            boolean changed = false;
            for (String guildId : requestConfig.getKeys(false)) {
                for (String uuidString : requestConfig.getConfigurationSection(guildId).getKeys(false)) {
                    long requestTime = requestConfig.getLong(guildId + "." + uuidString + ".time");
                    if (Instant.ofEpochSecond(requestTime).plus(3, ChronoUnit.DAYS).isBefore(Instant.now())) {
                        requestConfig.set(guildId + "." + uuidString, null);
                        changed = true;
                    }
                }
            }
            if (changed) {
                try {
                    requestConfig.save(requestFile);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not save guild requests after cleaning: " + e.getMessage());
                }
            }
            // Update in-memory maps
            guildRequests.values().forEach(requests ->
                    requests.values().removeIf(GuildRequest::isExpired));
            playerRequests.values().forEach(requests ->
                    requests.values().removeIf(GuildRequest::isExpired));
        });
    }
    public static Map<UUID, GuildRequest> getGuildRequests(String guildId) {
        return Collections.unmodifiableMap(guildRequests.getOrDefault(guildId, new HashMap<>()));
    }

    public static Map<String, GuildRequest> getPlayerRequests(UUID playerUUID) {
        return Collections.unmodifiableMap(playerRequests.getOrDefault(playerUUID, new HashMap<>()));
    }
}
