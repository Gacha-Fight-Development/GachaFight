package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.scoreboard.Board;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UpdateScoreboard implements CommandExecutor {
    private final GachaFight plugin;
    private final Board scoreboard;

    public UpdateScoreboard(GachaFight plugin, Board scoreboard) {
        this.plugin = plugin;
        this.scoreboard = scoreboard;
        plugin.getCommand("updatescoreboard").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> scoreboard.setScoreBoard(player), 20, 60);
            return true;
        } else {
            sender.sendMessage("This command can only be used by players.");
            return false;
        }
    }
}
