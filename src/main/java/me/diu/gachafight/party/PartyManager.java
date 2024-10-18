    package me.diu.gachafight.party;

    import me.diu.gachafight.GachaFight;
    import me.diu.gachafight.utils.ColorChat;
    import org.bukkit.Bukkit;
    import org.bukkit.OfflinePlayer;
    import org.bukkit.configuration.file.FileConfiguration;
    import org.bukkit.configuration.file.YamlConfiguration;

    import java.io.File;
    import java.io.IOException;
    import java.util.*;
    import java.util.stream.Collectors;

    public class PartyManager {
        private static final int MAX_PARTY_SIZE = 4;
        private static GachaFight plugin;
        public static File configFile;
        private static FileConfiguration config;

        public static void initialize(GachaFight plugin) {
            PartyManager.plugin = plugin;
            configFile = new File(plugin.getDataFolder(), "parties.yml");
            config = YamlConfiguration.loadConfiguration(configFile);
        }

        public static void createParty(OfflinePlayer leader) {
            String leaderUUID = leader.getUniqueId().toString();
            config.set(leaderUUID + ".members", new ArrayList<String>());
            saveConfig();
        }

        public static boolean addToParty(OfflinePlayer leader, OfflinePlayer member) {
            String leaderUUID = leader.getUniqueId().toString();
            List<String> members = config.getStringList(leaderUUID + ".members");
            if (members.size() < MAX_PARTY_SIZE - 1) {
                members.add(member.getUniqueId().toString());
                config.set(leaderUUID + ".members", members);
                saveConfig();
                return true;
            }
            return false;
        }

        public static void removeFromParty(OfflinePlayer leader, OfflinePlayer member) {
            String leaderUUID = leader.getUniqueId().toString();
            List<String> members = config.getStringList(leaderUUID + ".members");
            String memberUUID = member.getUniqueId().toString();

            if (leader.getUniqueId().equals(member.getUniqueId())) {
                // If the leader is leaving, disband the party
                config.set(leaderUUID, null);
                // Notify all online members that the party has been disbanded
                for (String uuid : members) {
                    OfflinePlayer partyMember = getOfflinePlayer(uuid);
                    if (partyMember != null && partyMember.isOnline()) {
                        partyMember.getPlayer().sendMessage(ColorChat.chat("&cThe party has been disbanded as the leader left."));
                    }
                }
            } else {
                // If a regular member is leaving, just remove them from the party
                members.remove(memberUUID);
                config.set(leaderUUID + ".members", members);

                // Notify the remaining online party members
                for (String uuid : members) {
                    OfflinePlayer partyMember = getOfflinePlayer(uuid);
                    if (partyMember != null && partyMember.isOnline()) {
                        // Add notification message here if needed
                    }
                }
            }

            saveConfig();
        }

        public static Set<OfflinePlayer> getPartyMembers(OfflinePlayer leader) {
            return getPartyMembers(leader.getUniqueId().toString());
        }

        private static Set<OfflinePlayer> getPartyMembers(String leaderUUID) {
            Set<OfflinePlayer> members = new HashSet<>();

            // Add all members, including those who are offline
            members.addAll(config.getStringList(leaderUUID + ".members").stream()
                    .map(PartyManager::getOfflinePlayer)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));

            // Add the leader to the set
            OfflinePlayer leader = getOfflinePlayer(leaderUUID);
            if (leader != null) {
                members.add(leader);
            }

            return members;
        }

        public static boolean isInParty(OfflinePlayer player) {
            String playerUUID = player.getUniqueId().toString();
            if (config.contains(playerUUID)) {
                return true;  // Player is a party leader
            }
            for (String leaderUUID : config.getKeys(false)) {
                List<String> members = config.getStringList(leaderUUID + ".members");
                if (members.contains(playerUUID)) {
                    return true;  // Player is a member of this party
                }
            }
            return false;  // Player is not in any party
        }

        public static OfflinePlayer getPartyLeader(OfflinePlayer player) {
            String playerUUID = player.getUniqueId().toString();

            // If the player is a party leader
            if (config.contains(playerUUID)) {
                return player;
            }

            // Check if the player is in someone else's party
            for (String leaderUUID : config.getKeys(false)) {
                List<String> members = config.getStringList(leaderUUID + ".members");
                if (members.contains(playerUUID)) {
                    return getOfflinePlayer(leaderUUID);
                }
            }
            // Player is not in any party
            return null;
        }

        public static boolean isPartyFull(OfflinePlayer leader) {
            String leaderUUID = leader.getUniqueId().toString();
            return config.getStringList(leaderUUID + ".members").size() >= MAX_PARTY_SIZE - 1;
        }

        public static int getPartySize(OfflinePlayer leader) {
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

        public static OfflinePlayer getOfflinePlayer(String uuid) {
            OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(uuid);
            if (player == null) {
                player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            }
            return player;
        }
        public static Set<OfflinePlayer> getAllPartyLeaders() {
            Set<OfflinePlayer> leaders = new HashSet<>();
            for (String leaderUUID : config.getKeys(false)) {
                OfflinePlayer leader = Bukkit.getOfflinePlayer(UUID.fromString(leaderUUID));
                leaders.add(leader);
            }
            return leaders;
        }
    }
