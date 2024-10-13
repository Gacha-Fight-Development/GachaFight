package me.diu.gachafight.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.TextDisplayUtils;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

import java.util.UUID;

public class ChunkUnloadListener implements Listener {

    public ChunkUnloadListener(GachaFight plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();

        // Iterate through all entities in the chunk and remove TextDisplay entities
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof TextDisplay) {
                UUID entityUUID = entity.getUniqueId();

                // Check if the TextDisplay is stored in the activeDisplays map
                if (TextDisplayUtils.activeDisplays.containsKey(entityUUID)) {
                    // Remove the display and remove it from the HashMap
                    entity.remove();
                    TextDisplayUtils.activeDisplays.remove(entityUUID);
                }
            }
        }
    }
}
