package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.shop.buy.gui.BuyShopEditorGUI;
import me.diu.gachafight.shop.buy.gui.BuyShopSelectionGUI;
import me.diu.gachafight.shop.potion.gui.PotionRaritySelectionGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EditShopCommand implements CommandExecutor {
    private GachaFight plugin;
    public EditShopCommand(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getCommand("editshop").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Open the Gold Shop Editor
            if (args.length == 1 && args[0].equalsIgnoreCase("gold")) {
                BuyShopEditorGUI.openGoldShopEditor(player, plugin);
                return true;
            }

            // Open the Gem Shop Editor
            if (args.length == 1 && args[0].equalsIgnoreCase("gem")) {
                BuyShopEditorGUI.openGemShopEditor(player, plugin);
                return true;
            }

            player.sendMessage("Usage: /buyshopeditor <gold|gem>");
        }
        return false;
    }
}

