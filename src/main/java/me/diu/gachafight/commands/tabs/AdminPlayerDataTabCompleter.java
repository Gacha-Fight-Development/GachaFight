package me.diu.gachafight.commands.tabs;

import me.diu.gachafight.GachaFight;
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

    private static final List<String> ACTIONS = List.of("view", "set");
    private static final List<String> STATS = List.of("level", "exp", "damage", "armor", "hp", "luck");

    public AdminPlayerDataTabCompleter(GachaFight plugin) {
        plugin.getCommand("adminplayerdata").setTabCompleter(this);
    }

    @Override
    @NotNull
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if (args.length == 1) {
            // First argument: player name
            return getPlayerNamesMatching(args[0]);
        } else if (args.length == 2) {
            // Second argument: action
            return getMatchingOptions(args[1], ACTIONS);
        } else if (args.length == 3) {
            // Third argument: stat (only for set and add actions)
            if ("set".equalsIgnoreCase(args[1]) || "add".equalsIgnoreCase(args[1])) {
                return getMatchingOptions(args[2], STATS);
            }
        }
        return Collections.emptyList();
    }


    private List<String> getMatchingOptions(String current, List<String> options) {
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(current.toLowerCase())) {
                result.add(option);
            }
        }
        return result;
    }

    private List<String> getPlayerNamesMatching(String current) {
        return Bukkit.getOnlinePlayers().stream()
                .map(player -> player.getName())
                .filter(name -> name.toLowerCase().startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }
}

