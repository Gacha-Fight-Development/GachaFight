package me.diu.gachafight.utils;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TabCompleteUtils {
    public static List<String> getMatchingOptions(String current, List<String> options) {
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(current.toLowerCase())) {
                result.add(option);
            }
        }
        return result;
    }
    public static List<String> getPlayerNamesMatching(String current) {
        return Bukkit.getOnlinePlayers().stream()
                .map(player -> player.getName())
                .filter(name -> name.toLowerCase().startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }
}
