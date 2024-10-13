package me.diu.gachafight.utils;

import me.diu.gachafight.GachaFight;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TextDisplayUtils {

    public static final Map<UUID, TextDisplay> activeDisplays = new HashMap<>();

    public static void summonDamageTextDisplay(Entity target, double damage, boolean isCrit) {
        // Get the world and location near the entity (slightly to the right of the head)
        Location loc = target.getLocation().add(target.getLocation().getDirection().normalize().multiply(0.5)).add(0.5, 1.8, 0);

        // Summon the TextDisplay entity at the location
        TextDisplay textDisplay = target.getWorld().spawn(loc, TextDisplay.class, display -> {
            display.setBillboard(Display.Billboard.VERTICAL); // Make it face the player naturally
            display.setSeeThrough(true); // Make the background transparent
            display.setBackgroundColor(Color.fromARGB(0,1,1,1));

            // Set the appropriate text based on whether it's a critical hit or not
            if (isCrit) {
                display.text(MiniMessage.miniMessage().deserialize("<!i><color:#9B870C><b>💥 <gold>" + String.format("%.1f", damage)));
            } else {
                display.text(MiniMessage.miniMessage().deserialize("<!i><red>🗡 <gold>" + String.format("%.1f", damage)));
            }
            activeDisplays.put(display.getUniqueId(), display);

            new BukkitRunnable() {
                @Override
                public void run() {
                    display.setInterpolationDuration(10); // Interpolate the movement for smoothness
                    display.setInterpolationDelay(1); // No delay in movement
                    display.setTransformation(new Transformation(
                            new Vector3f(1, 0, 0),     // Initial position (no offset at the start)
                            new AxisAngle4f(0, 0, 0, 0),  // Initial rotation (no rotation)
                            new Vector3f(1, 1, 1),        // Scale (normal size)
                            new AxisAngle4f(0, 0, 0, 0)   // Final rotation (no rotation)
                    ));
                }
            }.runTaskLater(GachaFight.getInstance(), 1);
            new BukkitRunnable() {
                @Override
                public void run() {
                    display.setInterpolationDuration(5); // Interpolate the movement for smoothness
                    display.setInterpolationDelay(1); // No delay in movement
                    display.setTransformation(new Transformation(
                            new Vector3f(1, 0.7F, 0),     // Initial position (no offset at the start)
                            new AxisAngle4f(0, 0, 0, 0),  // Initial rotation (no rotation)
                            new Vector3f(1, 1, 1),        // Scale (normal size)
                            new AxisAngle4f(0, 0, 0, 0)   // Final rotation (no rotation)
                    ));
                    // Apply new transformation with a slight hover effect
                }
            }.runTaskLater(GachaFight.getInstance(),11);
        });


        // Remove the TextDisplay after 1.5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                textDisplay.remove();
                activeDisplays.remove(textDisplay.getUniqueId());
                // Apply new transformation with a slight hover effect
            }
        }.runTaskLater(GachaFight.getInstance(),30);
    }
    public static void removeAllDisplays() {
        for (TextDisplay display : activeDisplays.values()) {
            display.remove();
        }
        activeDisplays.clear(); // Clear the HashMap after removing all displays
    }
}
