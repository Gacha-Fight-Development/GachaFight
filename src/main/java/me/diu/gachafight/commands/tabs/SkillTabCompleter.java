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

public class SkillTabCompleter implements TabCompleter {

    private static final List<String> ACTIONS = Arrays.asList("get", "give", "drop");

    public SkillTabCompleter(GachaFight plugin) {
        plugin.getCommand("skill").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Suggest actions
            return TabCompleteUtils.getMatchingOptions(args[0], ACTIONS);
        } else if (args.length == 2) {
            // Suggest skill names
            String partialSkillName = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();
            return TabCompleteUtils.getMatchingOptions(partialSkillName, SkillFileUtils.SKILL_NAMES);
        } else if (args.length >= 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("drop"))) {
            if (args[0].equalsIgnoreCase("give") && args.length == 3) {
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
}

