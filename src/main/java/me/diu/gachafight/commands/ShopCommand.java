package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.combat.DamageListener;
import me.diu.gachafight.gacha.gui.RaritySelectionGUI;
import me.diu.gachafight.listeners.BankNPCListener;
import me.diu.gachafight.quest.gui.QuestGUI;
import me.diu.gachafight.shop.buy.gui.BuyShopSelectionGUI;
import me.diu.gachafight.shop.potion.gui.PotionRaritySelectionGUI;
import me.diu.gachafight.shop.sell.ShopManager;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {
    private final GachaFight plugin;

    public ShopCommand(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getCommand("shop").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        Player player = (Player) sender;
        if (player.hasPermission("gacha.shop") || player.hasPermission("gacha.vip")) {
            if (!DamageListener.isSafezone(player.getLocation())) {
                player.sendMessage(ColorChat.chat("&cYou are not in safezone."));
            }
            if (args.length == 0) {
                player.sendMessage("/shop <buy/sell/potion/quest/bank>");
                return true;
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("buy")) {
                    BuyShopSelectionGUI.open(player);
                } else if (args[0].equalsIgnoreCase("sell")) {
                    ShopManager.openShopGUI(player);
                } else if (args[0].equalsIgnoreCase("potion")) {
                    PotionRaritySelectionGUI.openShop(player, plugin); // Opens rarity selection GUI
                } else if (args[0].equalsIgnoreCase("quest")) {
                    QuestGUI.openQuestSelection(player);
                } else if (args[0].equalsIgnoreCase("bank")) {
                    BankNPCListener.redeemGold(player);
                } else {
                    player.sendMessage("/shop <buy/sell/potion/quest/bank>");
                    return true;
                }
            }
            return true;
        } else {
            player.sendMessage(ColorChat.chat("&cBuy from /Buy or with Gem at Buy Shop"));
            return true;
        }
    }
}
