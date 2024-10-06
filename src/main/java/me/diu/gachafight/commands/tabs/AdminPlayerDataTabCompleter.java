package me.diu.gachafight.commands.tabs;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.TabCompleteUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AdminPlayerDataTabCompleter implements TabCompleter {

    private static final List<String> ACTIONS = List.of("view", "set", "add");
    private static final List<String> STATS = List.of("level", "exp", "damage", "armor", "hp", "luck", "gem", "money", "critchance", "critdmg", "dodge", "speed");

    public AdminPlayerDataTabCompleter(GachaFight plugin) {
        plugin.getCommand("adminplayerdata").setTabCompleter(this);
    }

    @Override
    @NotNull
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if (args.length == 1) {
            // First argument: player name
            return TabCompleteUtils.getPlayerNamesMatching(args[0]);
        } else if (args.length == 2) {
            // Second argument: action
            return TabCompleteUtils.getMatchingOptions(args[1], ACTIONS);
        } else if (args.length == 3) {
            // Third argument: stat (only for set and add actions)
            if ("set".equalsIgnoreCase(args[1]) || "add".equalsIgnoreCase(args[1])) {
                return TabCompleteUtils.getMatchingOptions(args[2], STATS);
            }
        }
        return Collections.emptyList();
    }
}

