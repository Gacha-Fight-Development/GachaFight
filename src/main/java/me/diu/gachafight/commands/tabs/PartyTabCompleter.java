package me.diu.gachafight.commands.tabs;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.party.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PartyTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("create", "invite", "accept", "leave", "kick", "list");

    public PartyTabCompleter(GachaFight plugin) {
        plugin.getCommand("party").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String subcommand : SUBCOMMANDS) {
                if (subcommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("kick"))) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (PartyManager.isInParty(player) && PartyManager.getPartyLeader(player) == player) {
                    if (args[0].equalsIgnoreCase("invite")) {
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            String playerName = onlinePlayer.getName();
                            if (playerName.toLowerCase().startsWith(args[1].toLowerCase())) {
                                completions.add(playerName);
                            }
                        }
                    }
                } else if (args[0].equalsIgnoreCase("kick")) {
                    for (OfflinePlayer partyMember : PartyManager.getPartyMembers(player)) {
                        if (partyMember != player) {
                            String playerName = partyMember.getName();
                            if (playerName.toLowerCase().startsWith(args[1].toLowerCase())) {
                                completions.add(playerName);
                            }
                        }
                    }
                }
            }
        }
        return completions;
    }
}
