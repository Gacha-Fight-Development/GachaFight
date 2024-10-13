package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.combat.DamageListener;
import me.diu.gachafight.guides.TutorialGuideSystem;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GuideCommand implements CommandExecutor {

    private final GachaFight plugin;
    public static final Map<String, Location> preSetLocations = new HashMap<>(); // Pre-set locations by name

    public GuideCommand(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getCommand("guide").setExecutor(this);

        // Define pre-set locations (you can load these from a config file instead)
        preSetLocations.put("dungeon", new Location(Bukkit.getWorld("Spawn"), 15.5, 99.5, 196.5));
        preSetLocations.put("healer", new Location(Bukkit.getWorld("Spawn"), 22.5, 100.5, 188.5));
        preSetLocations.put("potion", new Location(Bukkit.getWorld("Spawn"), 35.5, 101.5, 210.5));
        preSetLocations.put("buyshop", new Location(Bukkit.getWorld("Spawn"), -5.5, 99.5, 179.5));
        preSetLocations.put("bank", new Location(Bukkit.getWorld("Spawn"), 32.5, 101.5, 214.5));
        preSetLocations.put("quest", new Location(Bukkit.getWorld("Spawn"), 35.5, 99.5, 189.5));
        preSetLocations.put("overseer", new Location(Bukkit.getWorld("Spawn"), -3.5, 100.5, 202.5));
        preSetLocations.put("equipment", new Location(Bukkit.getWorld("Spawn"), 15.5, 98.5, 152.5));
        preSetLocations.put("tutorialgacha", new Location(Bukkit.getWorld("Spawn"), -632.5, 4.5, 69.5));
        preSetLocations.put("tutorialmushroom", new Location(Bukkit.getWorld("Spawn"), -687.5, 4.5, 53.5));
        preSetLocations.put("tutorialexit", new Location(Bukkit.getWorld("Spawn"), -737.5, 4.5, 72.5));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Command format: /guide <player> <location>
        if (args.length < 1) {
            sender.sendMessage(ColorChat.chat("&6Locations:"));
            sender.sendMessage(ColorChat.chat(" &e/guide dungeon &7| &dEnter Dungeon"));
            sender.sendMessage(ColorChat.chat(" &e/guide healer &7| &dHeal Yourself"));
            sender.sendMessage(ColorChat.chat(" &e/guide potion &7| &dBuy Potion"));
            sender.sendMessage(ColorChat.chat(" &e/guide buyshop &7| &dBuy Using Gold/Gem"));
            sender.sendMessage(ColorChat.chat(" &e/guide overseer &7| &dIncrease Stats"));
            sender.sendMessage(ColorChat.chat(" &e/guide quest &7| &dQuests"));
            sender.sendMessage(ColorChat.chat(" &e/guide equipment &7| &dLevel Up/Reroll Items"));
            return true;
        }
        Player player = (Player) sender;
        String locationName = args[0];

        // Find the pre-set location by name
        Location destination = preSetLocations.get(locationName.toLowerCase());
        if (destination == null) {
            sender.sendMessage("Location '" + locationName + "' not found. Available locations: " + preSetLocations.keySet());
            return true;
        }
        if (!DamageListener.isSafezone(player.getLocation())) {
            player.sendMessage(ColorChat.chat("&cNot in Safe Zone!"));
        }

        // Start guiding the target player to the destination
        plugin.getGuideSystem().guidePlayerToLocation(player, destination);
        sender.sendMessage("Guiding " + player.getName() + " to " + locationName);
        return true;
    }
}
