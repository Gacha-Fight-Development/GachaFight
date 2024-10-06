package me.diu.gachafight.commands;

import me.diu.gachafight.guides.TutorialGuideSystem;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GuideCommand implements CommandExecutor {

    private final TutorialGuideSystem guideSystem;

    public GuideCommand(TutorialGuideSystem guideSystem) {
        this.guideSystem = guideSystem;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // If player does not provide coordinates, guide them to spawn
        Location destination = player.getWorld().getSpawnLocation();

        // If coordinates are provided, use them as the destination
        if (args.length == 3) {
            try {
                double x = Double.parseDouble(args[0]);
                double y = Double.parseDouble(args[1]);
                double z = Double.parseDouble(args[2]);
                destination = new Location(player.getWorld(), x, y, z);
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid coordinates. Usage: /guide <x> <y> <z>");
                return false;
            }
        }

        // Start guiding the player to the destination
        guideSystem.guidePlayerToLocation(player, destination);
        player.sendMessage("Starting guide to: " + destination.toString());
        return true;
    }
}
