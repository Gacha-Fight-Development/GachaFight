package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.di.ServiceLocator;
import me.diu.gachafight.services.MongoService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminPlayerDataCommand implements CommandExecutor {
    private final MongoService mongoService;

    public AdminPlayerDataCommand(GachaFight plugin, ServiceLocator serviceLocator) {
        plugin.getCommand("adminplayerdata").setExecutor(this);
        this.mongoService = serviceLocator.getService(MongoService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("gacha.dev")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /adminplayerdata <player> <action> [value]");
            return true;
        }

        String targetPlayerName = args[0].toLowerCase();
        String action = args[1];
        String value = args.length > 2 ? args[2] : null;

        Player targetPlayer = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(targetPlayerName)) {
                targetPlayer = p;
                break;
            }
        }

        if (targetPlayer == null) {
            sender.sendMessage("Player not found online: " + targetPlayerName);
            return true;
        }

        PlayerStats stats = PlayerStats.getPlayerStats(targetPlayer);

        switch (action.toLowerCase()) {
            case "view":
                sender.sendMessage(stats.showStats(targetPlayer));
                sender.sendMessage(String.valueOf(stats.getGearStats().getTotalArmor()));
                sender.sendMessage(String.valueOf(stats.getWeaponStats().getDamage()));
                break;
            case "add":
                if (args.length < 4) {
                    sender.sendMessage("Usage: /adminplayerdata <player> add <stat> <value>");
                    return true;
                }
                try {
                    double doubleValue = Double.parseDouble(args[3]);
                    int intValue = (int) doubleValue;

                    switch (args[2].toLowerCase()) {
                        case "damage":
                            stats.setDamage(stats.getDamage() + doubleValue);
                            break;
                        case "armor":
                            stats.setArmor(stats.getArmor() + doubleValue);
                            break;
                        case "hp":
                            stats.setMaxhp(stats.getMaxhp() + doubleValue);
                            break;
                        case "luck":
                            stats.setLuck(stats.getLuck() + intValue);
                            break;
                        case "level":
                            stats.setLevel(stats.getLevel() + intValue);
                            break;
                        case "exp":
                            stats.setExp(stats.getExp() + doubleValue);
                            break;
                        case "money":
                            stats.setMoney(stats.getMoney() + doubleValue);
                            break;
                        case "gem":
                            stats.setGem(stats.getGem() + intValue);
                            break;
                        default:
                            sender.sendMessage("Unknown stat.");
                            return true;
                    }

                    sender.sendMessage("Player stats have been updated.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid value. Please enter a number.");
                }
                break;

            case "set":
                if (args.length < 4) {
                    sender.sendMessage("Usage: /adminplayerdata <player> set <stat> <value>");
                    return true;
                }
                try {
                    double doubleValue = Double.parseDouble(args[3]);
                    int intValue = (int) doubleValue;

                    switch (args[2].toLowerCase()) {
                        case "damage":
                            stats.setDamage(doubleValue);
                            break;
                        case "armor":
                            stats.setArmor(doubleValue);
                            break;
                        case "hp":
                            stats.setMaxhp(intValue);
                            break;
                        case "luck":
                            stats.setLuck(intValue);
                            break;
                        case "level":
                            stats.setLevel(intValue);
                            break;
                        case "exp":
                            stats.setExp(doubleValue);
                            break;
                        case "money":
                            stats.setMoney(doubleValue);
                            break;
                        case "gem":
                            stats.setGem(intValue);
                            break;
                        default:
                            sender.sendMessage("Unknown stat.");
                            return true;
                    }

                    sender.sendMessage("Player stats have been updated.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid value. Please enter a number.");
                }
                break;

            default:
                sender.sendMessage("Unknown action. Use 'view' or 'set'.");
                break;
        }
        return true;
    }
    
}
