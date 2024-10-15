package me.diu.gachafight.skills.utils;

import me.diu.gachafight.commands.SkillCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class RandomSkillUtils {

    public static ItemStack getRandomCommonSkill() {
        Random random = new Random();
        String skillName = SkillFileUtils.COMMON_SKILLS.get(random.nextInt(SkillFileUtils.COMMON_SKILLS.size()));
        FileConfiguration skillConfig = SkillFileUtils.loadSkillConfig(skillName);
        return SkillCommand.createSkillItem(skillName, skillConfig);
    }
    public static ItemStack getRandomUncommonSkill() {
        Random random = new Random();
        String skillName = SkillFileUtils.UNCOMMON_SKILLS.get(random.nextInt(SkillFileUtils.UNCOMMON_SKILLS.size()));
        FileConfiguration skillConfig = SkillFileUtils.loadSkillConfig(skillName);
        return SkillCommand.createSkillItem(skillName, skillConfig);
    }
    public static ItemStack getRandomRareSkill() {
        Random random = new Random();
        String skillName = SkillFileUtils.RARE_SKILLS.get(random.nextInt(SkillFileUtils.RARE_SKILLS.size()));
        FileConfiguration skillConfig = SkillFileUtils.loadSkillConfig(skillName);
        return SkillCommand.createSkillItem(skillName, skillConfig);
    }
    public static ItemStack getRandomEpicSkill() {
        Random random = new Random();
        String skillName = SkillFileUtils.EPIC_SKILLS.get(random.nextInt(SkillFileUtils.EPIC_SKILLS.size()));
        FileConfiguration skillConfig = SkillFileUtils.loadSkillConfig(skillName);
        return SkillCommand.createSkillItem(skillName, skillConfig);
    }
}
