package me.diu.gachafight.commands.tabs;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.skills.utils.SkillFileUtils;
import me.diu.gachafight.utils.TabCompleteUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SkillTabCompleter implements TabCompleter {

    private static final List<String> ACTIONS = Arrays.asList("get", "give", "drop");
    private static final List<String> RARITIES = Arrays.asList("common", "uncommon", "rare", "epic");

    public SkillTabCompleter(GachaFight plugin) {
        plugin.getCommand("skill").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Suggest actions
            return TabCompleteUtils.getMatchingOptions(args[0], ACTIONS);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("get")) {
                // Suggest rarities for 'get' action
                return TabCompleteUtils.getMatchingOptions(args[1], RARITIES);
            } else {
                // Suggest skill names for other actions
                String partialSkillName = args[1].toLowerCase();
                return TabCompleteUtils.getMatchingOptions(partialSkillName, SkillFileUtils.SKILL_NAMES);
            }
        } else if (args.length >= 3) {
            if (args[0].equalsIgnoreCase("get")) {
                // Suggest skills based on rarity for 'get' action
                String rarity = args[1].toLowerCase();
                List<String> skillsForRarity = getSkillsForRarity(rarity);
                String partialSkillName = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).toLowerCase();
                return TabCompleteUtils.getMatchingOptions(partialSkillName, skillsForRarity);
            } else if (args[0].equalsIgnoreCase("give") && args.length == 3) {
                // Suggest online player names for 'give' action
                return TabCompleteUtils.getPlayerNamesMatching(args[2]);
            } else if (args[0].equalsIgnoreCase("drop") && args.length == 5) {
                // No suggestions for coordinates
                return new ArrayList<>();
            } else {
                // Continue suggesting skill names for multi-word skills
                String partialSkillName = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();
                return TabCompleteUtils.getMatchingOptions(partialSkillName, SkillFileUtils.SKILL_NAMES);
            }
        }

        return new ArrayList<>();
    }

    private List<String> getSkillsForRarity(String rarity) {
        switch (rarity) {
            case "common":
                return SkillFileUtils.COMMON_SKILLS;
            case "uncommon":
                return SkillFileUtils.UNCOMMON_SKILLS;
            case "rare":
                return SkillFileUtils.RARE_SKILLS;
            case "epic":
                return SkillFileUtils.EPIC_SKILLS;
            default:
                return Stream.of(
                        SkillFileUtils.COMMON_SKILLS,
                        SkillFileUtils.UNCOMMON_SKILLS,
                        SkillFileUtils.RARE_SKILLS,
                        SkillFileUtils.EPIC_SKILLS
                ).flatMap(List::stream).collect(Collectors.toList());
        }
    }
}
