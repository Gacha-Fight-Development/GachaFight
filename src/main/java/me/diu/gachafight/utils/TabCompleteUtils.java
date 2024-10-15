package me.diu.gachafight.utils;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TabCompleteUtils {
    public static List<String> getMatchingOptions(String input, List<String> options) {
        List<String> matchingOptions = new ArrayList<>();
        String lowercaseInput = input.toLowerCase();

        for (String option : options) {
            String lowercaseOption = option.toLowerCase();
            if (lowercaseOption.startsWith(lowercaseInput)) {
                matchingOptions.add(option);
            } else if (lowercaseOption.contains(" ") && lowercaseOption.replace(" ", "").startsWith(lowercaseInput)) {
                // Handle multi-word options where spaces are ignored in the input
                matchingOptions.add(option);
            }
        }

        return matchingOptions;
    }
    public static List<String> getPlayerNamesMatching(String current) {
        return Bukkit.getOnlinePlayers().stream()
                .map(player -> player.getName())
                .filter(name -> name.toLowerCase().startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }
}
