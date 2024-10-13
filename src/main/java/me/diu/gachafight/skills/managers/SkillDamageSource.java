package me.diu.gachafight.skills.managers;

import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

public class SkillDamageSource {

    private final Plugin plugin;

    public SkillDamageSource(Plugin plugin) {
        this.plugin = plugin;
    }

    // Create a new custom damage source
    public static DamageSource damageSource(Player player) {
        return DamageSource.builder(DamageType.CACTUS)
                .withDirectEntity(player)
                .build();
    }
}
