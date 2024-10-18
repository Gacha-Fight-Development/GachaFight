package me.diu.gachafight.commands.tabs;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.commands.HelpCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HelpTabCompleter implements TabCompleter {


    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partialTopic = args[0].toLowerCase();
            completions.addAll(HelpCommand.helpTopics.keySet().stream()
                    .filter(topic -> topic.startsWith(partialTopic))
                    .collect(Collectors.toList()));
        } else if (args.length == 2) {
            String topic = args[0].toLowerCase();
            String partialSection = args[1].toLowerCase();
            if (HelpCommand.helpTopics.containsKey(topic)) {
                completions.addAll(HelpCommand.getSections(HelpCommand.helpTopics.get(topic)).stream()
                        .filter(section -> section.startsWith(partialSection))
                        .collect(Collectors.toList()));
                if ("true".startsWith(partialSection)) {
                    completions.add("true");
                }
            }
        } else if (args.length == 3) {
            if ("true".startsWith(args[2].toLowerCase())) {
                completions.add("true");
            }
        }

        return completions;
    }
}
