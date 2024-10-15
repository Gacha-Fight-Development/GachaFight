package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.party.PartyManager;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCommand implements CommandExecutor {

    private final GachaFight plugin;

    public PartyCommand(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getCommand("party").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ColorChat.chat("&cUsage: /party <create|invite|accept|leave|kick|list>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                createParty(player);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ColorChat.chat("&cUsage: /party invite <player>"));
                    return true;
                }
                inviteToParty(player, args[1]);
                break;
            case "accept":
                acceptInvite(player);
                break;
            case "leave":
                leaveParty(player);
                break;
            case "kick":
                if (args.length < 2) {
                    player.sendMessage(ColorChat.chat("&cUsage: /party kick <player>"));
                    return true;
                }
                kickFromParty(player, args[1]);
                break;
            case "list":
                listPartyMembers(player);
                break;
            default:
                player.sendMessage(ColorChat.chat("&cUnknown subcommand. Use /party for help."));
        }

        return true;
    }
    private void createParty(Player leader) {
        if (PartyManager.isInParty(leader)) {
            leader.sendMessage(ColorChat.chat("&cYou are already in a party."));
            return;
        }
        PartyManager.createParty(leader);
        leader.sendMessage(ColorChat.chat("&aYou have created a new party."));
    }

    private void inviteToParty(Player inviter, String inviteeName) {
        Player invitee = Bukkit.getPlayer(inviteeName);
        if (invitee == null) {
            inviter.sendMessage(ColorChat.chat("&cPlayer not found."));
            return;
        }
        if (!PartyManager.isInParty(inviter)) {
            inviter.sendMessage(ColorChat.chat("&cYou are not in a party. Create one first."));
            return;
        }
        if (PartyManager.getPartyLeader(inviter) != inviter) {
            inviter.sendMessage(ColorChat.chat("&cOnly the party leader can invite players."));
            return;
        }
        if (PartyManager.isPartyFull(inviter)) {
            inviter.sendMessage(ColorChat.chat("&cYour party is already full (max 3 players)."));
            return;
        }
        // Here you would typically send an invitation to the invitee
        inviter.sendMessage(ColorChat.chat("&aInvitation sent to " + invitee.getName()));
        invitee.sendMessage(ColorChat.chat("&a" + inviter.getName() + " has invited you to their party. Use /party accept to join."));
    }

    private void acceptInvite(Player player) {
        // This is a simplified version. You'd typically check if there's a pending invitation
        if (PartyManager.isInParty(player)) {
            player.sendMessage(ColorChat.chat("&cYou are already in a party."));
            return;
        }
        // For simplicity, we're just adding the player to the first party we find
        for (Player leader : Bukkit.getOnlinePlayers()) {
            if (PartyManager.isInParty(leader) && PartyManager.getPartyLeader(leader) == leader) {
                if (PartyManager.isPartyFull(leader)) {
                    player.sendMessage(ColorChat.chat("&cThe party you're trying to join is already full."));
                    return;
                }
                if (PartyManager.addToParty(leader, player)) {
                    player.sendMessage(ColorChat.chat("&aYou have joined " + leader.getName() + "'s party."));
                    leader.sendMessage(ColorChat.chat("&a" + player.getName() + " has joined your party."));
                    return;
                }
            }
        }
        player.sendMessage(ColorChat.chat("&cNo party invitation found or all parties are full."));
    }

    private void leaveParty(Player player) {
        Player leader = PartyManager.getPartyLeader(player);
        if (leader == null) {
            player.sendMessage(ColorChat.chat("&cYou are not in a party."));
            return;
        }
        if (leader == player) {
            // If the leader leaves, disband the party
            for (Player member : PartyManager.getPartyMembers(leader)) {
                member.sendMessage(ColorChat.chat("&cThe party has been disbanded."));
                PartyManager.removeFromParty(leader, member);
            }
        } else {
            PartyManager.removeFromParty(leader, player);
            player.sendMessage(ColorChat.chat("&aYou have left the party."));
            // Notify other party members
            for (Player member : PartyManager.getPartyMembers(leader)) {
                member.sendMessage(ColorChat.chat("&e" + player.getName() + " has left the party."));
            }
            leader.sendMessage(ColorChat.chat("&e" + player.getName() + " has left the party."));
        }
    }

    private void kickFromParty(Player kicker, String kickeeName) {
        Player kickee = Bukkit.getPlayer(kickeeName);
        if (kickee == null) {
            kicker.sendMessage(ColorChat.chat("&cPlayer not found."));
            return;
        }
        if (!PartyManager.isInParty(kicker)) {
            kicker.sendMessage(ColorChat.chat("&cYou are not in a party."));
            return;
        }
        if (PartyManager.getPartyLeader(kicker) != kicker) {
            kicker.sendMessage(ColorChat.chat("&cOnly the party leader can kick players."));
            return;
        }
        if (!PartyManager.isInParty(kickee) || PartyManager.getPartyLeader(kickee) != kicker) {
            kicker.sendMessage(ColorChat.chat("&cThis player is not in your party."));
            return;
        }
        PartyManager.removeFromParty(kicker, kickee);
        kicker.sendMessage(ColorChat.chat("&aYou have kicked " + kickee.getName() + " from the party."));
        kickee.sendMessage(ColorChat.chat("&cYou have been kicked from the party."));

        // Notify other party members
        for (Player member : PartyManager.getPartyMembers(kicker)) {
            if (member != kicker && member != kickee) {
                member.sendMessage(ColorChat.chat("&e" + kicker.getName() + " has kicked " + kickee.getName() + " from the party."));
            }
        }
    }
    private void listPartyMembers(Player player) {
        if (!PartyManager.isInParty(player)) {
            player.sendMessage(ColorChat.chat("&cYou are not in a party."));
            return;
        }

        Player leader = PartyManager.getPartyLeader(player);

        player.sendMessage(ColorChat.chat("&6===== Party Members ====="));
        player.sendMessage(ColorChat.chat("&eLeader: &a" + leader.getName()));
        player.sendMessage(ColorChat.chat("&eMembers:"));
        for (Player member : PartyManager.getPartyMembers(leader)) {
            if (member != leader) {
                player.sendMessage(ColorChat.chat("&7- &a" + member.getName()));
            }
        }
        player.sendMessage(ColorChat.chat("&6========================"));
    }
}
