package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.party.PartyManager;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PartyCommand implements CommandExecutor {

    private final GachaFight plugin;
    private final Map<UUID, UUID> partyInvitations = new HashMap<>();

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
            player.sendMessage(ColorChat.chat("&a/party "));
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
                    player.sendMessage(ColorChat.chat("&6/party create &eCreates Party."));
                    player.sendMessage(ColorChat.chat("&6/party invite <player> &eInvites a player to your party."));
                    player.sendMessage(ColorChat.chat("&6/party accept &eAccepts an invitation."));
                    player.sendMessage(ColorChat.chat("&6/party leave &eLeaves the current party."));
                    player.sendMessage(ColorChat.chat("&6/party kick <player> &eKicks a player from the party."));
                    player.sendMessage(ColorChat.chat("&6/party list &eLists all party members."));
                    player.sendMessage(ColorChat.chat("&6Party MAX: &e4 Players"));
                    player.sendMessage(ColorChat.chat("&6Party Bonus: &e5% Extra Gold/EXP Per Player"));
                    player.sendMessage(ColorChat.chat("&6Rank Bonus: &eVIP: +5%, VIP+: +10%, MVP: +15%, MVP+: 20%"));
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
        OfflinePlayer invitee = PartyManager.getOfflinePlayer(inviteeName);
        OfflinePlayer partyLeader = PartyManager.getPartyLeader(inviter);

        if (invitee == null || !invitee.isOnline()) {
            inviter.sendMessage(ColorChat.chat("&cPlayer not found or not online."));
            return;
        }
        if (!PartyManager.isInParty(inviter)) {
            inviter.sendMessage(ColorChat.chat("&cYou are not in a party. Create one first."));
            return;
        }
        if (!partyLeader.getUniqueId().equals(inviter.getUniqueId())) {
            inviter.sendMessage(ColorChat.chat("&cOnly the party leader can invite players."));
            return;
        }
        if (PartyManager.isPartyFull(partyLeader)) {
            inviter.sendMessage(ColorChat.chat("&cYour party is already full (max 4 players)."));
            return;
        }

        // Store the invitation in the HashMap
        partyInvitations.put(invitee.getUniqueId(), inviter.getUniqueId());

        inviter.sendMessage(ColorChat.chat("&aInvitation sent to " + invitee.getName()));
        invitee.getPlayer().sendMessage(ColorChat.chat("&a" + inviter.getName() + " has invited you to their party. Use /party accept to join."));

        // Set a timeout for the invitation (e.g., 60 seconds)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (partyInvitations.remove(invitee.getUniqueId()) != null) {
                inviter.sendMessage(ColorChat.chat("&cInvitation to " + invitee.getName() + " has expired."));
                if (invitee.isOnline()) {
                    invitee.getPlayer().sendMessage(ColorChat.chat("&cInvitation from " + inviter.getName() + " has expired."));
                }
            }
        }, 20 * 60); // 20 ticks * 60 seconds = 60 seconds
    }
    private void acceptInvite(Player player) {
        UUID inviterUUID = partyInvitations.remove(player.getUniqueId());

        if (inviterUUID == null) {
            player.sendMessage(ColorChat.chat("&cYou don't have any pending party invitations."));
            return;
        }

        OfflinePlayer inviter = Bukkit.getOfflinePlayer(inviterUUID);
        if (!inviter.isOnline()) {
            player.sendMessage(ColorChat.chat("&cThe party leader is no longer online."));
            return;
        }

        OfflinePlayer partyLeader = PartyManager.getPartyLeader(inviter);
        if (partyLeader == null || !partyLeader.getUniqueId().equals(inviterUUID)) {
            player.sendMessage(ColorChat.chat("&cThe invitation is no longer valid."));
            return;
        }

        if (PartyManager.isPartyFull(partyLeader)) {
            player.sendMessage(ColorChat.chat("&cThe party is already full."));
            return;
        }

        if (PartyManager.addToParty(partyLeader, player)) {
            player.sendMessage(ColorChat.chat("&aYou have joined " + inviter.getName() + "'s party."));
            if (inviter.isOnline()) {
                inviter.getPlayer().sendMessage(ColorChat.chat("&a" + player.getName() + " has joined your party."));
            }

            // Notify other party members
            for (OfflinePlayer member : PartyManager.getPartyMembers(partyLeader)) {
                if (member.isOnline() && !member.getUniqueId().equals(inviterUUID) && !member.getUniqueId().equals(player.getUniqueId())) {
                    member.getPlayer().sendMessage(ColorChat.chat("&a" + player.getName() + " has joined the party."));
                }
            }
        } else {
            player.sendMessage(ColorChat.chat("&cFailed to join the party. Please try again."));
        }
    }


    private void leaveParty(Player player) {
        if (!PartyManager.isInParty(player)) {
            player.sendMessage(ColorChat.chat("&cYou are not in a party."));
            return;
        }

        OfflinePlayer leader = PartyManager.getPartyLeader(player);
        if (leader == null) {
            player.sendMessage(ColorChat.chat("&cError: Unable to determine party leader. Please contact an administrator."));
            return;
        }

        if (leader.getUniqueId().equals(player.getUniqueId())) {
            // If the leader leaves, disband the party
            Set<OfflinePlayer> members = PartyManager.getPartyMembers(leader);
            for (OfflinePlayer member : members) {
                if (!member.getUniqueId().equals(player.getUniqueId()) && member.isOnline()) {
                    member.getPlayer().sendMessage(ColorChat.chat("&cThe party has been disbanded as the leader left."));
                }
                PartyManager.removeFromParty(leader, member);
            }
            player.sendMessage(ColorChat.chat("&aYou have disbanded your party."));
        } else {
            PartyManager.removeFromParty(leader, player);
            player.sendMessage(ColorChat.chat("&aYou have left the party."));
            // Notify other party members
            for (OfflinePlayer member : PartyManager.getPartyMembers(leader)) {
                if (member.isOnline()) {
                    member.getPlayer().sendMessage(ColorChat.chat("&e" + player.getName() + " has left the party."));
                }
            }
        }
    }

    private void kickFromParty(Player kicker, String kickeeName) {
        OfflinePlayer kickee = PartyManager.getOfflinePlayer(kickeeName);
        if (kickee == null) {
            kicker.sendMessage(ColorChat.chat("&cPlayer not found."));
            return;
        }
        if (!PartyManager.isInParty(kicker)) {
            kicker.sendMessage(ColorChat.chat("&cYou are not in a party."));
            return;
        }
        OfflinePlayer leader = PartyManager.getPartyLeader(kicker);
        if (!leader.getUniqueId().equals(kicker.getUniqueId())) {
            kicker.sendMessage(ColorChat.chat("&cOnly the party leader can kick players."));
            return;
        }
        if (!PartyManager.isInParty(kickee) || !PartyManager.getPartyLeader(kickee).equals(leader)) {
            kicker.sendMessage(ColorChat.chat("&cThis player is not in your party."));
            return;
        }
        PartyManager.removeFromParty(leader, kickee);
        kicker.sendMessage(ColorChat.chat("&aYou have kicked " + kickee.getName() + " from the party."));
        if (kickee.isOnline()) {
            kickee.getPlayer().sendMessage(ColorChat.chat("&cYou have been kicked from the party."));
        }

        // Notify other party members
        for (OfflinePlayer member : PartyManager.getPartyMembers(leader)) {
            member.getPlayer().sendMessage(ColorChat.chat("&e" + kicker.getName() + " has kicked " + kickee.getName() + " from the party."));
        }
    }

    private void listPartyMembers(Player player) {
        if (!PartyManager.isInParty(player)) {
            player.sendMessage(ColorChat.chat("&cYou are not in a party."));
            return;
        }

        OfflinePlayer leader = PartyManager.getPartyLeader(player);
        if (leader == null) {
            player.sendMessage(ColorChat.chat("&cError: Unable to determine party leader. Please contact an administrator."));
            return;
        }

        player.sendMessage(ColorChat.chat("&6===== Party Members ====="));
        player.sendMessage(ColorChat.chat("&eLeader: &a" + leader.getName()));
        player.sendMessage(ColorChat.chat("&eMembers:"));
        for (OfflinePlayer member : PartyManager.getPartyMembers(leader)) {
            if (!member.equals(leader)) {
                player.sendMessage(ColorChat.chat("&7- &a" + member.getName() + (member.isOnline() ? " (Online)" : " (Offline)")));
            }
        }
        player.sendMessage(ColorChat.chat("&6========================"));
    }
}
