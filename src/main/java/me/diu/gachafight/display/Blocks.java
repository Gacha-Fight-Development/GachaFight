package me.diu.gachafight.display;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.diu.gachafight.GachaFight;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

public class Blocks {
    private static final Location gachaChestLocation = new Location(Bukkit.getWorld("Spawn"), 3.5, 101, 211.5);
    private static final Location tutorialGachaChestLocation = new Location(Bukkit.getWorld("Spawn"), -632.5, 4, 70.5);
    public static ActiveMob gachaChest;
    public static ActiveMob tutorialGachaChest;
    public static void spawnGachaChest() {
        MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob("GachaChest").orElse(null);
        if (mob != null) {
            // spawns mob
            gachaChest = mob.spawn(BukkitAdapter.adapt(gachaChestLocation), 1);

            // get mob as bukkit entity
            Entity entity = gachaChest.getEntity().getBukkitEntity();
            entity.setRotation(180, 0);
        }
    }
    public static void spawnTutorialGachaChest() {
        MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob("GachaChestSmall").orElse(null);
        if (mob != null) {
            // spawns mob
            tutorialGachaChest = mob.spawn(BukkitAdapter.adapt(tutorialGachaChestLocation), 1);

            // get mob as bukkit entity
            Entity entity = tutorialGachaChest.getEntity().getBukkitEntity();
            entity.setRotation(-130,0 );
        }
    }
}
