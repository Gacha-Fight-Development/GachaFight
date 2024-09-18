package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HelpCommand implements CommandExecutor {

    private final GachaFight plugin;

    public HelpCommand(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getCommand("help").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 0) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Available /help commands:"));
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/help Dungeon <gray>- Explains the <gold>Dungeon</gold> Master feature."));
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/help Sell <gray>- Explains the <gold>Sell</gold> feature."));
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/help Gacha <gray>- Explains the <gold>Gacha</gold> feature."));
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/help Mobs <gray>- Explains the mobs feature."));
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/help Stats <gray>- Explains how stats work."));
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/help Level <gray>- Explains how leveling works."));
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/help Calculations <gray>- Explains how calculations are made."));
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/help Potions <gray>- Learn about potions and their effects."));
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/help Keys <gray>- Information on how keys work and their importance."));
            } else {
                String helpTopic = args[0].toLowerCase();

                switch (helpTopic) {
                    case "dungeon":
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Dungeon Master Help:"));
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>The <gold>Dungeon Master</gold> allows you to challenge dungeons and defeat bosses. " +
                                        "Completing dungeons grants rewards and increases your reputation."));
                        break;
                    case "sell":
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Sell Help:"));
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>You can <gold>sell</gold> items in your inventory using the Sell feature. " +
                                        "Visit the shop and select the items you want to sell for in-game currency."));
                        break;
                    case "gacha":
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Gacha Help:"));
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>The <gold>Gacha</gold> system allows you to spend Gacha keys to receive random items. " +
                                        "Higher rarity items have better stats and abilities."));
                        break;
                    case "mobs":
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Mobs Help:"));
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>Fight different types of mobs, including MythicMobs. " +
                                        "Defeating mobs grants experience and sometimes special drops."));
                        break;
                    case "stats":
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Stats Help:"));
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>Your <gold>stats</gold>, such as damage, armor, and luck, affect your performance in battles. " +
                                        "Improve your stats by leveling up or equipping better gear."));
                        break;
                    case "level":
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Level Help:"));
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>Your <gold>level</gold> increases as you gain experience. " +
                                        "Higher levels improve your stats and unlock new abilities."));
                        break;
                    case "calculations":
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Calculations Help:"));
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray><gold>Calculations</gold> determine the outcome of battles, such as damage dealt and received. " +
                                        "Factors include your stats and enemy defenses."));
                        break;
                    case "potions":
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Potions Help:"));
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>Potions provide temporary buffs to your stats, " +
                                    "like increased damage or health regeneration. Note that potions will <red>drop on death</red>."));
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>Combine potions with the <gold>Dungeon</gold> or " +
                                        "<gold>Gacha</gold> systems to gain an advantage."));
                        break;
                    case "keys":
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Keys Help:"));
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>Keys are used to unlock <gold>Gacha</gold> rewards and special chests. " +
                                        "Be cautious, as keys will <red>drop on death</red>."));
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<gray>Collect keys through various activities like <gold>Dungeon</gold> runs and <gold>Mobs</gold> battles."));
                        break;
                    default:
                        player.sendMessage(MiniMessage.miniMessage().deserialize(
                                "<red>Unknown help topic. Please use /help to see available topics."));
                        break;
                }
            }
        } else {
            sender.sendMessage("This command can only be used by players.");
        }
        return true;
    }
}
