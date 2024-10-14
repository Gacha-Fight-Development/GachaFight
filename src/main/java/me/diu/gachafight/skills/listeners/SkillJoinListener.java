package me.diu.gachafight.skills.listeners;

import me.diu.gachafight.skills.SkillSystem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class SkillJoinListener implements Listener {


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Setup the skill slots when the player joins
        SkillSystem.setupSkillSlots(player);
    }
}
