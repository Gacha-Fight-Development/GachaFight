package me.diu.gachafight.skills.utils;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.gacha.gui.RaritySelectionGUI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class SkillFileUtils {
    public static final List<String> SKILL_NAMES = List.of(
            "Sword Charge", "Sword Slash", "Sword Spin", "Sword Burst",
            "Thunder Strike", "Fire Strike", "Dush", "Life Steal", "Ghost Sword"
    );
    public static final List<String> COMMON_SKILLS = List.of("Sword Charge", "Sword Slash");
    public static final List<String> UNCOMMON_SKILLS = List.of("Sword Spin", "Sword Burst");
    public static final List<String> RARE_SKILLS = List.of("Thunder Strike", "Fire Strike", "Dush");
    public static final List<String> EPIC_SKILLS = List.of("Life Steal", "Ghost Sword");

    public static FileConfiguration loadSkillConfig(String skillName) {
        for (String rarity : RaritySelectionGUI.RARITY_NAMES) {
            File configFile = new File(GachaFight.getInstance().getDataFolder(), "Skills/" + rarity.toLowerCase() + ".yml");
            if (configFile.exists()) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                // Check if the skill exists in this rarity file
                if (config.contains(skillName)) {
                    return config;
                }
            }
        }
        return null;
    }

    public static File loadSkillFile(String skillName) {
        for (String rarity : RaritySelectionGUI.RARITY_NAMES) {
            File configFile = new File(GachaFight.getInstance().getDataFolder(), "Skills/" + rarity.toLowerCase() + ".yml");
            if (configFile.exists()) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                // Check if the skill exists in this rarity file
                if (config.contains(skillName)) {
                    return configFile;
                }
            }
        }
        return null;
    }
}
