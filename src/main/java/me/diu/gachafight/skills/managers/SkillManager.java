package me.diu.gachafight.skills.managers;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.rarity.common.SwordChargeSkill;
import me.diu.gachafight.skills.rarity.common.SwordSlashSkill;
import me.diu.gachafight.skills.rarity.epic.GhostSwordSkill;
import me.diu.gachafight.skills.rarity.epic.LifeStealSkill;
import me.diu.gachafight.skills.rarity.rare.DushSkill;
import me.diu.gachafight.skills.rarity.rare.FireStrikeSkill;
import me.diu.gachafight.skills.rarity.rare.ThunderStrikeSkill;
import me.diu.gachafight.skills.rarity.uncommon.SwordBurstSkill;
import me.diu.gachafight.skills.rarity.uncommon.SwordSpinSkill;
import me.diu.gachafight.skills.utils.Skill;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillManager {
    private final GachaFight plugin;
    public static final Map<String, Skill> skills = new HashMap<>();

    public SkillManager(GachaFight plugin) {
        this.plugin = plugin;
        initializeSkills();
        updateConfigFiles();
    }

    private void initializeSkills() {
        registerSkill("sword charge", new SwordChargeSkill(plugin));
        registerSkill("sword burst", new SwordBurstSkill(plugin));
        registerSkill("sword slash", new SwordSlashSkill(plugin));
        registerSkill("sword spin", new SwordSpinSkill(plugin));
        registerSkill("thunder strike", new ThunderStrikeSkill(plugin));
        registerSkill("fire strike", new FireStrikeSkill(plugin));
        registerSkill("dush", new DushSkill(plugin));
        registerSkill("life steal", new LifeStealSkill(plugin));
        registerSkill("ghost sword", new GhostSwordSkill(plugin));
    }

    private void updateConfigFiles() {
        plugin.saveResource("Skills/common.yml", true);
        plugin.saveResource("Skills/uncommon.yml", true);
        plugin.saveResource("Skills/rare.yml", true);
        plugin.saveResource("Skills/epic.yml", true);
        //plugin.saveResource("Skills/unique.yml", true);
        //plugin.saveResource("Skills/legendary.yml", true);
        //plugin.saveResource("Skills/mythic.yml", true);
    }

    public void registerSkill(String name, Skill skill) {
        skills.put(name, skill);
    }

    public static void useSkill(Player player, String skillName, int slot) {
        skillName = skillName.replace(" (Skill)", "");
        Skill skill = skills.get(skillName.toLowerCase());
        if (skill != null) {
            skill.useSkill(player, slot);
        } else {
            player.sendMessage(ChatColor.RED + "Skill not found: " + skillName);
        }
    }

    public static double applyActiveSkills(Player player, LivingEntity target) {
        double damageMultiplier = 1.0;
        for (Skill skill : skills.values()) {
            if (skill.hasActiveState()) {
                if (skill.isSkillActive(player)) {
                    damageMultiplier = skill.applySkillEffect(player, target);
                    skill.deactivateSkill(player);
                    break;
                }
            }
        }
        return damageMultiplier;
    }

    public static Skill getSkill(String skillName) {
        return skills.get(skillName);
    }

    public static Map<String, Skill> getAllSkills() {
        return new HashMap<>(skills);
    }

    public static List<String> getSkillNames() {
        return new ArrayList<>(skills.keySet());
    }
}
