package me.diu.gachafight.skills.managers;

import lombok.Getter;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MobDropSelector {
    @Getter
    private static String mob;  // Current mob eligible for rare skill book drops
    private static final long THIRTY_MINUTES = 30 * 60 * 1000L; // 30 minutes in milliseconds
    private static File configFile;
    private static FileConfiguration config;


    public static List<String> listMobs = List.of("rpg_slime_cube", "rpg_poison_slime_cube", "rpg_skeleton", "rpg_skeleton_crossbow", "rpg_sand_golem", "rpg_stone_golem",
            "rpg_mushroom", "rpg_mushroom_red", "rpg_rat", "rpg_rat_undead", "rpg_bat", "rpg_zombie", "rpg_zombie_head", "goblin warrior",
            "goblin king", "goblin shaman", "Possessed Cinder Armor", "Possessed Ruin Armor", "Possessed Viking Armor", "Shadow Sorcerer",
            "Shadow Maskerer", "Shadow Pursued", "Shadow Mask", "Shadow Golem");

    public static void changeMobs(Player player) {
        long savedTime = config.getLong("timestamp");
        if (System.currentTimeMillis() - savedTime > THIRTY_MINUTES) {
            clearMobData();
        }
        if (mob == null) {
            Random random = new Random();
            int index = random.nextInt(listMobs.size());
            mob = listMobs.get(index);
            config.set("mob", mob);
            config.set("timestamp", System.currentTimeMillis());
            saveConfig();
            Bukkit.broadcastMessage(ColorChat.chat("&7[&dMagical Orb&7] &6" + player.getName() + "&7 Used Magical Orb."));
        }
    }
    public static void init() {
        // Initialize the rareskillmob.yml file
        configFile = new File(Bukkit.getPluginManager().getPlugin("GachaFight").getDataFolder(), "rareskillmob.yml");

        // Load the file if it exists, otherwise create a new one
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Load the mob and the timestamp from the config file if available
        loadMobData();
    }

    private static void loadMobData() {
        // Load the mob and timestamp from the config file
        if (config.contains("mob") && config.contains("timestamp")) {
            mob = config.getString("mob");
            long savedTime = config.getLong("timestamp");

            // If 30 minutes haven't passed yet, keep the mob
            if (System.currentTimeMillis() - savedTime > THIRTY_MINUTES) {
                clearMobData();
            }
        }
    }

    private static void clearMobData() {
        Bukkit.broadcastMessage(ColorChat.chat("&6Mob: &e" + mob + " &7no longer drops rare skill book"));
        mob = null;
        config.set("mob", null);
        config.set("timestamp", null);
        saveConfig();
    }

    private static void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void scheduleTimer() {
        new BukkitRunnable() {
            public void run() {
                long savedTime = config.getLong("timestamp");
                if (mob != null) {
                    if (System.currentTimeMillis() - savedTime > THIRTY_MINUTES) {
                        clearMobData();
                    }
                }
            }
        }.runTaskTimer(GachaFight.getInstance(), 0, 1200);
    }
}
