package me.diu.gachafight.shop.potion.managers;

import lombok.Getter;
@Getter
public class PotionConfig {

    String type;
    int value;
    int cooldown;
    int duration;

    public PotionConfig(String type,int value,int cooldown,int duration) {
        this.type = type;
        this.value = value;
        this.cooldown = cooldown;
        this.duration = duration;
    }
}
