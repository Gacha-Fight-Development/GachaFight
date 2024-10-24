package me.diu.gachafight.siege;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Set;
import java.util.UUID;
@Getter
public abstract class Arena {
    private final Location location;
    private final int radius;
    private final List<SiegeMob> siegeMobs;
    private final int waves;
    private final int rewardGold;
    private final int rewardXp;
    private final String permissionReward;
    private final int arenaId;

    public Arena(int arenaId, Location location, int radius, List<SiegeMob> siegeMobs, int waves, int rewardGold, int rewardXp, String permissionReward) {
        this.arenaId = arenaId;
        this.location = location;
        this.radius = radius;
        this.siegeMobs = siegeMobs;
        this.waves = waves;
        this.rewardGold = rewardGold;
        this.rewardXp = rewardXp;
        this.permissionReward = permissionReward;
    }

    public abstract void start(OfflinePlayer player);
    public abstract void leave(OfflinePlayer player);
}
