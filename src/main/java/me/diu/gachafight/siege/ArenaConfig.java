package me.diu.gachafight.siege;

import java.util.List;

public class ArenaConfig {
    private int arenaId;
    private List<SiegeMob> mobs;
    private int rewardGold;
    private int rewardXp;

    public ArenaConfig(int arenaId, List<SiegeMob> mobs, int rewardGold, int rewardXp) {
        this.arenaId = arenaId;
        this.mobs = mobs;
        this.rewardGold = rewardGold;
        this.rewardXp = rewardXp;
    }

    public int getArenaId() {
        return arenaId;
    }

    public List<SiegeMob> getMobs() {
        return mobs;
    }

    public int getRewardGold() {
        return rewardGold;
    }

    public int getRewardXp() {
        return rewardXp;
    }
}
