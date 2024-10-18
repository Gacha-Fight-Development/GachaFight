package me.diu.gachafight.commands.tabs;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.guild.GuildManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GuildTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("create", "invite", "accept", "leave", "kick", "list", "info", "upgrade", "promote", "demote");


    public GuildTabCompleter(GachaFight plugin) {
        plugin.getCommand("guild").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(subcommand -> subcommand.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && sender instanceof Player) {
            Player player = (Player) sender;
            switch (args[0].toLowerCase()) {
                case "invite":
                    return getOnlinePlayerNames(args[1]).stream()
                            .filter(name -> !GuildManager.isInGuild(Bukkit.getPlayer(name)))
                            .collect(Collectors.toList());
                case "kick":
                case "promote":
                case "demote":
                    return GuildManager.getGuildMembers(GuildManager.getGuildId(player)).stream()
                            .filter(member -> member != player)
                            .map(OfflinePlayer::getName)
                            .filter(name -> name != null && name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "upgrade":
            }
        }

        return completions;
    }

    private List<String> getOnlinePlayerNames(String prefix) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
