package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.quest.managers.DailyQuestManager;
import me.diu.gachafight.quest.managers.SideQuestManager;
import me.diu.gachafight.quest.utils.DailyQuestScheduler;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RefreshQuestCommand implements CommandExecutor {
    private final GachaFight plugin;
    public RefreshQuestCommand(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getCommand("refreshquest").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (!player.hasPermission("gacha.admin")) {
            player.sendMessage(ColorChat.chat("&cYou don't have permission to use this command!"));
            return true;
        } else {
            DailyQuestManager.clearDailyQuestCompletionData();
            SideQuestManager.clearSideQuests();
            Bukkit.broadcastMessage(ColorChat.chat("&aRefreshed Quests."));
            return true;
        }
    }
}
