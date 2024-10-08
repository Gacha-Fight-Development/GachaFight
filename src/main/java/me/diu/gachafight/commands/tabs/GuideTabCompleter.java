package me.diu.gachafight.commands.tabs;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.TabCompleteUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class GuideTabCompleter implements TabCompleter {
    private static final List<String> TYPE = List.of("dungeon", "potion", "healer", "buyshop", "bank", "overseer", "quest");
    public GuideTabCompleter(GachaFight plugin) {
        plugin.getCommand("guide").setTabCompleter(this);
    }

    @Override
    @NotNull
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TabCompleteUtils.getMatchingOptions(args[0], TYPE);
        }
        return Collections.emptyList();
    }
}
