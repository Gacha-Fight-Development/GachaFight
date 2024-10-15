package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.hooks.VaultHook;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class PayCommand implements CommandExecutor {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");

    public PayCommand(GachaFight plugin) {
        plugin.getCommand("pay").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ColorChat.chat("&cYou must be a player to use this command!"));
                return true;
            }

            Player player = (Player) sender;

            if (args.length < 2) {
                player.sendMessage(ColorChat.chat("&cUsage: /pay <player> <amount>"));
                return true;
            }

            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                player.sendMessage(ColorChat.chat("&cPlayer not found online: " + args[0]));
                return true;
            }

            double amount;
            try {
                amount = Double.parseDouble(args[1]);
                if (amount <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ColorChat.chat("&cInvalid amount. Please enter a positive number."));
                return true;
            }

            if (!VaultHook.isEconomySetup()) {
                player.sendMessage(ColorChat.chat("&cEconomy system is not set up properly."));
                return true;
            }

            double balance = VaultHook.getBalance(player);
            if (balance < amount) {
                return true;
            }

            String withdrawResult = VaultHook.withdraw(player, amount);
            if (withdrawResult == null || !withdrawResult.isEmpty()) {
                player.sendMessage(ColorChat.chat("&cFailed to withdraw money: " + (withdrawResult != null ? withdrawResult : "Unknown error")));
                return true;
            }

            String depositResult = VaultHook.deposit(targetPlayer, amount);
            if (depositResult == null || !depositResult.isEmpty()) {
                // Refund the money to the sender if deposit fails
                VaultHook.deposit(player, amount);
                player.sendMessage(ColorChat.chat("&cFailed to send money: " + (depositResult != null ? depositResult : "Unknown error")));
                return true;
            }

            String formattedAmount = DECIMAL_FORMAT.format(amount);
            player.sendMessage(ColorChat.chat("&aYou have paid $" + formattedAmount + " to " + targetPlayer.getName()));
            targetPlayer.sendMessage(ColorChat.chat("&a" + player.getName() + " has sent you $" + formattedAmount));

            return true;
        } catch (Exception e) {
            sender.sendMessage(ColorChat.chat("&cAn error occurred while processing the command."));
            e.printStackTrace();
            return true;
        }
    }
}
