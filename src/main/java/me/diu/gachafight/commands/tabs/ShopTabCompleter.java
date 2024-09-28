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

public class ShopTabCompleter implements TabCompleter {
    private static final List<String> TYPE = List.of("buy", "sell", "potion", "quest", "bank");
    public ShopTabCompleter(GachaFight plugin) {
        plugin.getCommand("shop").setTabCompleter(this);
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
