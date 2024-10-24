package me.diu.gachafight.siege;

public class SiegeMob {
    private final String mobId;
    private final int wave;

    public SiegeMob(String mobId, int wave) {
        this.mobId = mobId;
        this.wave = wave;
    }

    public String getMobId() {
        return mobId;
    }

    public int getWave() {
        return wave;
    }
}
