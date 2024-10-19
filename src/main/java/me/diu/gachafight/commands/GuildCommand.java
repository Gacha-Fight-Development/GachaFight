package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.guild.GuildGUI;
import me.diu.gachafight.guild.GuildManager;
import me.diu.gachafight.hooks.VaultHook;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GuildCommand implements CommandExecutor {

    private final GachaFight plugin;
    private final Map<UUID, String> guildInvitations = new HashMap<>();

    public GuildCommand(GachaFight plugin) {
        this.plugin = plugin;
        new GuildGUI(plugin);
        plugin.getCommand("guild").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            GuildGUI.openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                player.sendMessage(ColorChat.chat("&cUsage: /guild <create|invite|accept|leave|kick|list|info|upgrade>"));
                player.sendMessage(ColorChat.chat("&6/guild create <name> &eCreates Guild."));
                player.sendMessage(ColorChat.chat("&6/guild invite <player> &eInvites a Specific Player."));
                player.sendMessage(ColorChat.chat("&6/guild accept &eAccepts a Guild Invite"));
                player.sendMessage(ColorChat.chat("&6/guild leave &eleaves a Guild"));
                player.sendMessage(ColorChat.chat("&6/guild kick &eKicks a Specific Player out of Guild."));
                player.sendMessage(ColorChat.chat("&6/guild list &eOpens Guild Member List Menu"));
                player.sendMessage(ColorChat.chat("&6/guild info &eOpens Guild Information."));
                player.sendMessage(ColorChat.chat("&6/guild upgrade &eOpens Guild Upgrade Menu"));
                player.sendMessage(ColorChat.chat("&6/guild settings &eOpens Guilds Setting"));
                break;
            case "create":
                if (args.length < 2) {
                    player.sendMessage(ColorChat.chat("&cUsage: /guild create <name>"));
                    return true;
                }
                createGuild(player, args[1]);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ColorChat.chat("&cUsage: /guild invite <player>"));
                    return true;
                }
                inviteToGuild(player, args[1]);
                break;
            case "accept":
                acceptInvite(player);
                break;
            case "leave":
                leaveGuild(player);
                break;
            case "kick":
                if (args.length < 2) {
                    player.sendMessage(ColorChat.chat("&cUsage: /guild kick <player>"));
                    return true;
                }
                kickFromGuild(player, args[1]);
                break;
            case "list":
                listGuildMembers(player);
                break;
            case "info":
                showGuildInfo(player);
                break;
            case "upgrade":
                if (args.length < 2) {
                    player.sendMessage(ColorChat.chat("&cUsage: /guild upgrade <upgrade-name>"));
                    return true;
                }
                upgradeGuild(player, args[1]);
                break;
            case "promote":
                if (args.length < 2) {
                    player.sendMessage(ColorChat.chat("&cUsage: /guild promote <player>"));
                    return true;
                }
                promoteToCoLeader(player, args[1]);
                break;
            case "makeleader":
                if (args.length < 2) {
                    player.sendMessage(ColorChat.chat("&cUsage: /guild makeleader <player>"));
                    return true;
                }
                promoteToLeader(player, args[1]);
                break;
            case "demote":
                if (args.length < 2) {
                    player.sendMessage(ColorChat.chat("&cUsage: /guild demote <player>"));
                    return true;
                }
                demoteCoLeader(player, args[1]);
                break;
            case "changelogo":
                if (args.length < 2) {
                    player.sendMessage(ColorChat.chat("&cUsage: /guild changelogo <material>"));
                    return true;
                }
                changeGuildLogo(player, args[1]);
                break;
            case "changeicon":
                if (args.length < 2) {
                    player.sendMessage(ColorChat.chat("&cUsage: /guild changeicon <icon>"));
                    return true;
                }
                changeGuildIcon(player, args[1]);
                break;
            default:
                player.sendMessage(ColorChat.chat("&cUnknown subcommand. Use /guild for help."));
        }

        return true;
    }

    private void createGuild(Player leader, String guildName) {
        if (GuildManager.isInGuild(leader)) {
            leader.sendMessage(ColorChat.chat("&cYou are already in a guild."));
            return;
        }
        GuildManager.createGuild(leader, guildName);
        leader.sendMessage(ColorChat.chat("&aYou have created a new guild named " + guildName + "."));
    }

    private void inviteToGuild(Player inviter, String inviteeName) {
        OfflinePlayer invitee = Bukkit.getOfflinePlayer(inviteeName);
        String guildId = GuildManager.getGuildId(inviter);

        if (invitee == null || !invitee.isOnline()) {
            inviter.sendMessage(ColorChat.chat("&cPlayer not found or not online."));
            return;
        }
        if (guildId == null) {
            inviter.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }
        if (!GuildManager.isGuildLeader(inviter, guildId) && !GuildManager.isCoLeader(inviter, guildId)) {
            inviter.sendMessage(ColorChat.chat("&cOnly the guild leader or co-leader can invite players."));
            return;
        }
        if (GuildManager.isGuildFull(guildId)) {
            inviter.sendMessage(ColorChat.chat("&cYour guild is already full."));
            return;
        }

        guildInvitations.put(invitee.getUniqueId(), guildId);

        inviter.sendMessage(ColorChat.chat("&aInvitation sent to " + invitee.getName()));
        invitee.getPlayer().sendMessage(ColorChat.chat("&a" + inviter.getName() + " has invited you to their guild. Use /guild accept to join."));

        // Set a timeout for the invitation (e.g., 60 seconds)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (guildInvitations.remove(invitee.getUniqueId()) != null) {
                inviter.sendMessage(ColorChat.chat("&cInvitation to " + invitee.getName() + " has expired."));
                if (invitee.isOnline()) {
                    invitee.getPlayer().sendMessage(ColorChat.chat("&cInvitation from " + inviter.getName() + " has expired."));
                }
            }
        }, 20 * 60); // 20 ticks * 60 seconds = 60 seconds
    }

    private void acceptInvite(Player player) {
        String guildId = guildInvitations.remove(player.getUniqueId());

        if (guildId == null) {
            player.sendMessage(ColorChat.chat("&cYou don't have any pending guild invitations."));
            return;
        }

        if (GuildManager.addToGuild(guildId, player)) {
            player.sendMessage(ColorChat.chat("&aYou have joined the guild."));
            // Notify other guild members
            for (OfflinePlayer member : GuildManager.getGuildMembers(guildId)) {
                if (member.isOnline() && !member.getUniqueId().equals(player.getUniqueId())) {
                    member.getPlayer().sendMessage(ColorChat.chat("&a" + player.getName() + " has joined the guild."));
                }
            }
        } else {
            player.sendMessage(ColorChat.chat("&cFailed to join the guild. It might be full or no longer exist."));
        }
    }

    private void leaveGuild(Player player) {
        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) {
            player.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        GuildManager.removeFromGuild(guildId, player);
        player.sendMessage(ColorChat.chat("&aYou have left the guild."));
    }

    private void kickFromGuild(Player kicker, String kickeeName) {
        String guildId = GuildManager.getGuildId(kicker);
        if (guildId == null) {
            kicker.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        if (!GuildManager.isGuildLeader(kicker, guildId) && !GuildManager.isCoLeader(kicker, guildId)) {
            kicker.sendMessage(ColorChat.chat("&cOnly the guild leader or co-leader can kick players."));
            return;
        }

        OfflinePlayer kickee = Bukkit.getOfflinePlayer(kickeeName);
        if (kickee == null || !GuildManager.isInGuild(kickee) || !GuildManager.getGuildId(kickee).equals(guildId)) {
            kicker.sendMessage(ColorChat.chat("&cPlayer not found in your guild."));
            return;
        }

        GuildManager.removeFromGuild(guildId, kickee);
        kicker.sendMessage(ColorChat.chat("&a" + kickee.getName() + " has been kicked from the guild."));
        if (kickee.isOnline()) {
            kickee.getPlayer().sendMessage(ColorChat.chat("&cYou have been kicked from the guild."));
        }
    }

    private void listGuildMembers(Player player) {
        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) {
            player.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        String guildName = GuildManager.getGuildName(guildId);
        int guildLevel = GuildManager.getGuildLevel(guildId);

        player.sendMessage(ColorChat.chat("&6=== " + guildName + " (Level " + guildLevel + ") ==="));
        player.sendMessage(ColorChat.chat("&eMembers (" + GuildManager.getGuildMembers(guildId).size() + "/" + GuildManager.MAX_GUILD_SIZE + "):"));

        for (OfflinePlayer member : GuildManager.getGuildMembers(guildId)) {
            String status = member.isOnline() ? "&a[Online]" : "&c[Offline]";
            String role = GuildManager.isGuildLeader(member, guildId) ? "&6[Leader]" :
                    GuildManager.isCoLeader(member, guildId) ? "&e[Co-Leader]" : "&7[Member]";
            player.sendMessage(ColorChat.chat(status + " " + role + " &f" + member.getName()));
        }
    }

    private void promoteToLeader(Player promoter, String newLeaderName) {
        String guildId = GuildManager.getGuildId(promoter);
        if (guildId == null) {
            promoter.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        if (!GuildManager.isGuildLeader(promoter, guildId)) {
            promoter.sendMessage(ColorChat.chat("&cOnly the guild leader can promote a new leader."));
            return;
        }

        Player newLeader = Bukkit.getPlayer(newLeaderName);
        if (newLeader == null || !GuildManager.isInGuild(newLeader) || !GuildManager.getGuildId(newLeader).equals(guildId)) {
            promoter.sendMessage(ColorChat.chat("&cPlayer not found in your guild."));
            return;
        }

        // Store the current leader (promoter) before changing leadership
        Player currentLeader = promoter;

        // Set the new leader
        GuildManager.setGuildLeader(guildId, newLeader);
        // If successful, set the previous leader (promoter) as co-leader
        GuildManager.setCoLeader(guildId, currentLeader);

        promoter.sendMessage(ColorChat.chat("&a" + newLeader.getName() + " is now the guild leader."));
        newLeader.sendMessage(ColorChat.chat("&aYou are now the leader of the guild."));
        promoter.sendMessage(ColorChat.chat("&aYou are now the co-leader of the guild."));

        // Notify other guild members
        for (OfflinePlayer member : GuildManager.getGuildMembers(guildId)) {
            if (member.isOnline() && !member.getUniqueId().equals(promoter.getUniqueId()) && !member.getUniqueId().equals(newLeader.getUniqueId())) {
                member.getPlayer().sendMessage(ColorChat.chat("&a" + newLeader.getName() + " is now the guild leader."));
                member.getPlayer().sendMessage(ColorChat.chat("&a" + promoter.getName() + " is now the guild co-leader."));
            }
        }
    }

    private void showGuildInfo(Player player) {
        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) {
            player.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        String guildName = GuildManager.getGuildName(guildId);
        int guildLevel = GuildManager.getGuildLevel(guildId);
        int guildExp = GuildManager.getGuildExp(guildId);
        String guildIcon = GuildManager.getChatIcon(guildId);
        OfflinePlayer leader = GuildManager.getGuildLeader(guildId);
        OfflinePlayer coLeader = GuildManager.getCoLeader(guildId);
        int memberCount = GuildManager.getGuildMembers(guildId).size();


        player.sendMessage(ColorChat.chat("&6=== Guild Information ==="));
        player.sendMessage(ColorChat.chat("&eName: &f" + guildName));
        player.sendMessage(ColorChat.chat("&eIcon: &f" + guildIcon));
        player.sendMessage(ColorChat.chat("&eLevel: &f" + guildLevel));
        player.sendMessage(ColorChat.chat("&eExp: &f" + guildExp + "/" + GuildManager.getExpForNextLevel(guildLevel)));
        player.sendMessage(ColorChat.chat("&eLeader: &f" + (leader != null ? leader.getName() : "N/A")));
        player.sendMessage(ColorChat.chat("&eCo-Leader: &f" + (coLeader != null ? coLeader.getName() : "N/A")));
        player.sendMessage(ColorChat.chat("&eMembers: &f" + memberCount + "/" + GuildManager.MAX_GUILD_SIZE));
    }
    private void promoteToCoLeader(Player promoter, String promotedName) {
        String guildId = GuildManager.getGuildId(promoter);
        if (guildId == null) {
            promoter.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        if (!GuildManager.isGuildLeader(promoter, guildId)) {
            promoter.sendMessage(ColorChat.chat("&cOnly the guild leader can promote members to co-leader."));
            return;
        }

        OfflinePlayer promoted = Bukkit.getOfflinePlayer(promotedName);
        if (promoted == null || !GuildManager.isInGuild(promoted) || !GuildManager.getGuildId(promoted).equals(guildId)) {
            promoter.sendMessage(ColorChat.chat("&cPlayer not found in your guild."));
            return;
        }

        if (GuildManager.isCoLeader(promoted, guildId)) {
            promoter.sendMessage(ColorChat.chat("&cThis player is already a co-leader."));
            return;
        }

        GuildManager.setCoLeader(guildId, promoted);
        promoter.sendMessage(ColorChat.chat("&a" + promoted.getName() + " has been promoted to co-leader."));
        if (promoted.isOnline()) {
            promoted.getPlayer().sendMessage(ColorChat.chat("&aYou have been promoted to co-leader!"));
        }
    }

    private void demoteCoLeader(Player demoter, String demotedName) {
        String guildId = GuildManager.getGuildId(demoter);
        if (guildId == null) {
            demoter.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        if (!GuildManager.isGuildLeader(demoter, guildId)) {
            demoter.sendMessage(ColorChat.chat("&cOnly the guild leader can demote co-leaders."));
            return;
        }

        OfflinePlayer demoted = Bukkit.getOfflinePlayer(demotedName);
        if (demoted == null || !GuildManager.isInGuild(demoted) || !GuildManager.getGuildId(demoted).equals(guildId)) {
            demoter.sendMessage(ColorChat.chat("&cPlayer not found in your guild."));
            return;
        }

        if (!GuildManager.isCoLeader(demoted, guildId)) {
            demoter.sendMessage(ColorChat.chat("&cThis player is not a co-leader."));
            return;
        }

        GuildManager.removeCoLeader(guildId);
        demoter.sendMessage(ColorChat.chat("&a" + demoted.getName() + " has been demoted from co-leader."));
        if (demoted.isOnline()) {
            demoted.getPlayer().sendMessage(ColorChat.chat("&cYou have been demoted from co-leader."));
        }
    }
    private void upgradeGuild(Player player, String upgradeName) {
        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) {
            player.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        if (!GuildManager.isGuildLeader(player, guildId) && !GuildManager.isCoLeader(player, guildId)) {
            player.sendMessage(ColorChat.chat("&cOnly the guild leader or co-leader can upgrade the guild."));
            return;
        }

        // You might want to add a check here to see if the upgrade is valid and if the guild has enough resources

        if (GuildManager.upgradeGuild(guildId, upgradeName)) {
            player.sendMessage(ColorChat.chat("&aSuccessfully upgraded " + upgradeName + " for your guild."));
        } else {
            player.sendMessage(ColorChat.chat("&cFailed to upgrade. Make sure you have enough resources and the upgrade is valid."));
        }
    }
    private void changeGuildLogo(Player player, String materialName) {
        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) {
            player.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        if (!GuildManager.isGuildLeader(player, guildId) && !GuildManager.isCoLeader(player, guildId)) {
            player.sendMessage(ColorChat.chat("&cOnly the guild leader or co-leader can change the guild logo."));
            return;
        }

        // Check if the player has enough money
        if (!(VaultHook.getBalance(player) > 50000)) {
            player.sendMessage(ColorChat.chat("&cChanging the guild logo costs 50,000. You don't have enough money."));
            return;
        }

        try {
            Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ColorChat.chat("&cInvalid material name. Please use a valid Minecraft material."));
            return;
        }

        // Deduct the money and change the logo
        VaultHook.withdraw(player, 50000);
        GuildManager.setGuildLogo(guildId, materialName.toUpperCase());
        player.sendMessage(ColorChat.chat("&aGuild logo has been changed to " + materialName + ". 50,000 has been deducted from your account."));
    }

    private void changeGuildIcon(Player player, String icon) {
        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) {
            player.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        if (!GuildManager.isGuildLeader(player, guildId) && !GuildManager.isCoLeader(player, guildId)) {
            player.sendMessage(ColorChat.chat("&cOnly the guild leader or co-leader can change the guild icon."));
            return;
        }

        // Check if the player has enough money
        if (!(VaultHook.getBalance(player) > 50000)) {
            player.sendMessage(ColorChat.chat("&cChanging the guild icon costs 50,000. You don't have enough money."));
            return;
        }

        if (ColorChat.strip(ColorChat.chat(icon)).length() != 1) {
            player.sendMessage(ColorChat.chat("&cThe guild icon must be a single character."));
            return;
        }

        // Deduct the money and change the icon
        VaultHook.withdraw(player, 50000);
        GuildManager.setChatIcon(guildId, icon);
        player.sendMessage(ColorChat.chat("&aGuild icon has been changed to " + icon + ". 50,000 has been deducted from your account."));
    }
}
