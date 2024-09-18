package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.gacha.gui.RaritySelectionGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EditGacha implements CommandExecutor {
    private final GachaFight plugin;

    public EditGacha(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getCommand("editgacha").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        RaritySelectionGUI.openGachaRarity(player, plugin);
        return true;
    }
}
