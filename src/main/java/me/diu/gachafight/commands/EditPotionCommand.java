package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.shop.potion.gui.PotionRaritySelectionGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EditPotionCommand implements CommandExecutor {
    private final GachaFight plugin;

    public EditPotionCommand(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getCommand("editpotion").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        PotionRaritySelectionGUI.openSelectionEditor(player, plugin);
        return true;
    }
}
