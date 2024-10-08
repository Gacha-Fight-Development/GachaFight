package me.diu.gachafight.playerstats;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;



@Getter
@Setter
public class PlayerStats {
    public static final Map<UUID, PlayerStats> playerStatsMap = new HashMap<>();
    private UUID playerUUID;
    private int level;
    private double exp;
    private double maxhp;
    private double hp;
    private double damage;
    private double armor;
    private double critChance;
    private double critDmg;
    private double dodge;
    private double speed;
    private double luck;
    private double money;
    private int gem;

    private GearStats gearStats;
    private WeaponStats weaponStats;
    public PlayerStats(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.level = 1;
        this.exp = 0.0;
        this.damage = 1;
        this.maxhp = 20;
        this.hp = this.getMaxhp();
        this.armor = 0.2;
        this.critChance = 0.01;
        this.critDmg = 1.5;
        this.dodge = 0.01;
        this.luck = 5;
        this.money = 0;
        this.gem = 0;
        this.speed = 1;
        this.gearStats = new GearStats();
        this.weaponStats = new WeaponStats();
    }

    public static PlayerStats getPlayerStats(Player player) {
        return playerStatsMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats(player.getUniqueId()));
    }
    public static PlayerStats getPlayerStats(UUID uuid) {
        return playerStatsMap.get(uuid);
    }

    public String showStats(Player player) {
        PlayerStats stats = getPlayerStats(player);
        return ColorChat.chat("&aStats:\n" +
                "&eLevel: " + stats.getLevel() + "\n" +
                "&eExp: " + String.format("%.2f", stats.getExp()) + "/" + stats.getRequiredExp() + "\n" +
                "&eHP: " + String.format("%.0f", stats.getMaxhp()) + " (+" + String.format("%.1f", stats.getGearStats().getTotalMaxHp()) + ")\n" +
                "&eStrength: " + String.format("%.1f", stats.getDamage()) + " (+" + String.format("%.1f", stats.getWeaponStats().getDamage()) + ")\n" +
                "&eArmor: " + String.format("%.1f", stats.getArmor()) + " (+" + String.format("%.1f", stats.getGearStats().getTotalArmor()) + ")\n" +
                "&eOffhand Stats: " + stats.getGearStats().getOffhandStats().getDamage() + " damage, " +
                stats.getGearStats().getOffhandStats().getArmor() + " armor\n" +
                "Crit Chance: " + stats.getCritChance() + "\n" +
                "Crit Damage: " + stats.getCritDmg() + "\n" +
                "&eSpeed: " + String.format("%.2f", stats.getSpeed()) + "\n" +
                "Dodge: " + stats.getDodge() + "\n" +
                "&eLuck: " + stats.getLuck() + "\n" +
                "Money: " + String.format("%.2f", stats.getMoney()) + " Gem: " + stats.getGem());
    }

    public String showStats(UUID uuid) {
        PlayerStats stats = getPlayerStats(uuid);
        return ColorChat.chat("&aStats:\n" +
                "&eLevel: " + stats.getLevel() + "\n" +
                "&eExp: " + String.format("%.2f", stats.getExp()) + "/" + stats.getRequiredExp() + "\n" +
                "&eStrength: " + String.format("%.1f", stats.getDamage()) + " (+" + String.format("%.1f", stats.getWeaponStats().getDamage()) + ")\n" +
                "&eArmor: " + String.format("%.1f", stats.getArmor()) + " (+" + String.format("%.1f", stats.getGearStats().getTotalArmor()) + ")\n" +
                "&eHP: " + stats.getMaxhp() + " (+" + String.format("%.1f", stats.getGearStats().getTotalMaxHp()) + ")\n" +
                "&eOffhand Stats: " + stats.getGearStats().getOffhandStats().getDamage() + " damage, " +
                stats.getGearStats().getOffhandStats().getArmor() + " armor\n" +
                "Crit Chance: " + stats.getCritChance() + "\n" +
                "Crit Damage: " + stats.getCritDmg() + "\n" +
                "&eSpeed: " + stats.getSpeed() + "\n" +
                "Dodge: " + stats.getDodge() + "\n" +
                "&eLuck: " + stats.getLuck() + "\n" +
                "Money: " + String.format("%.2f", stats.getMoney()) + " Gem: " + stats.getGem());
    }

    public void addExp(double amount, Player player) {
        this.exp += amount;
        syncPlayerLevelWithMinecraft(player);
        syncExpBarWithMinecraft(player);  // Sync experience bar progress

        if (this.exp >= getRequiredExp()) {
            this.exp -= getRequiredExp();
            this.level++;
            syncPlayerLevelWithMinecraft(player);  // Sync Minecraft level
        }
    }

    public int getRequiredExp() {
        return (int) (Math.pow(this.level, 2.88) + 25 * this.level);
    }

    // Added a new method to sync player's health with their hearts
    public void syncHealthWithHearts(Player player) {
        double currentHp = Math.min(this.hp + this.getGearStats().getTotalMaxHp(), this.maxhp+ this.getGearStats().getTotalMaxHp());
        if (currentHp < 0) {
        } else {
            player.setHealth(currentHp / (this.maxhp+this.getGearStats().getTotalMaxHp()) * 20); // Scale to Minecraft's health system (20 half hearts)
        }
    }


    // Updated method to update the player's actionbar with their current health status
    public void updateActionbar(Player player) {
        String actionbarText = ColorChat.chat("&cHP: " + (int) (this.hp + this.getGearStats().getTotalMaxHp())  + "/" + (int) (this.maxhp + this.getGearStats().getTotalMaxHp()));
        player.sendActionBar(actionbarText);
    }

    // Sync player's Minecraft level with custom level
    public void syncPlayerLevelWithMinecraft(Player player) {
        player.setLevel(this.level);  // Set Minecraft level to match PlayerStats level
    }

    // Sync player's experience bar with custom exp/expreq progress
    public void syncExpBarWithMinecraft(Player player) {
        if (this.exp >= getRequiredExp()) {
            this.exp -= getRequiredExp();
            this.level++;
            syncPlayerLevelWithMinecraft(player);  // Sync Minecraft level
        }
        float expProgress = (float) (this.exp / getRequiredExp());  // Calculate progress as a float between 0 and 1
        player.setExp(expProgress);  // Set Minecraft experience bar
    }

    public static UUID getUUIDFromUsername(String username) throws Exception {
        String MOJANG_API_URL = "https://api.mojang.com/users/profiles/minecraft/";
        // Build the URL
        URL url = new URL(MOJANG_API_URL + username);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Check the response code
        if (connection.getResponseCode() != 200) {
            throw new Exception("Error: Could not find player with username " + username);
        }

        // Read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        // Close connections
        in.close();
        connection.disconnect();

        // Parse the JSON response
        JsonObject json = JsonParser.parseString(content.toString()).getAsJsonObject();
        String uuidString = json.get("id").getAsString();

        // Format the UUID (Mojang's API returns a non-hyphenated UUID)
        return formatUUID(uuidString);
    }

    // Helper method to format the UUID correctly with hyphens
    private static UUID formatUUID(String uuid) {
        return UUID.fromString(
                uuid.substring(0, 8) + "-" +
                        uuid.substring(8, 12) + "-" +
                        uuid.substring(12, 16) + "-" +
                        uuid.substring(16, 20) + "-" +
                        uuid.substring(20, 32).replace(" ", ""));
    }

}
