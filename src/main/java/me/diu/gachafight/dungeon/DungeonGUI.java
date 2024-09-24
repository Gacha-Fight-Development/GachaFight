package me.diu.gachafight.dungeon;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.playerstats.PlayerStatsListener;
import me.diu.gachafight.utils.ColorChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DungeonGUI implements Listener {

    private final GachaFight plugin;
    private final Map<String, Dungeon> dungeons = new HashMap<>();
    private final Map<String, Integer> dungeonSlots = new HashMap<>();  // New map to track dungeon slots

    public DungeonGUI(GachaFight plugin) {
        this.plugin = plugin;
        initializeDungeons(); // Initialize dungeon data
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // Initialize dungeons with their spawn points, description, and slot position
    private void initializeDungeons() {
        List<Location> dragonNestSpawns = Arrays.asList(
                new Location(Bukkit.getWorld("Spawn"), -838.5, 5, 264.5),
                new Location(Bukkit.getWorld("Spawn"), -967.5, 5, 264.5),
                new Location(Bukkit.getWorld("Spawn"), -838.5, 5, 468.5),
                new Location(Bukkit.getWorld("Spawn"), -967.5, 5, 468.5)
        );
        dungeons.put("Underground City", new Dungeon("Underground City", "Level: <aqua>1-10, <red>PvP Enabled", dragonNestSpawns));
        dungeonSlots.put("Underground City", 0);  // Assign "Underground City" to the first slot (index 0)

        List<Location> goblinCampSpawns = Arrays.asList(
                new Location(Bukkit.getWorld("Spawn"), -632.5, 4, 452.5),
                new Location(Bukkit.getWorld("Spawn"), -773.5, 4, 442.5),
                new Location(Bukkit.getWorld("Spawn"), -773.5, 4, 286.5),
                new Location(Bukkit.getWorld("Spawn"), -622.5, 4, 289.5)
        );
        dungeons.put("Goblin Camp", new Dungeon("Goblin Camp", "Level: <aqua>10-20, <red>PvP Enabled", goblinCampSpawns));
        dungeonSlots.put("Goblin Camp", 1);  // Assign "Goblin Camp" to the second slot (index 1)
    }

    // Open the Dungeon Selection GUI
    public void openDungeonGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ColorChat.chat("&6Dungeon Selector"));

        for (Map.Entry<String, Dungeon> entry : dungeons.entrySet()) {
            Dungeon dungeon = entry.getValue();
            String dungeonName = entry.getKey();
            int slot = dungeonSlots.get(dungeonName);  // Get the slot from the dungeonSlots map

            ItemStack dungeonItem = new ItemStack(Material.DIAMOND_SWORD);  // Customize item as necessary
            ItemMeta meta = dungeonItem.getItemMeta();
            meta.setDisplayName(ColorChat.chat("&a" + dungeon.getName()));
            List<Component> lore = Arrays.asList(
                    MiniMessage.miniMessage().deserialize("<!i><dark_aqua>" + dungeon.getDescription())
            );
            meta.lore(lore);
            dungeonItem.setItemMeta(meta);

            gui.setItem(slot, dungeonItem);  // Set the item in the correct slot
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getName() != null && event.getRightClicked().getName().contains("Dungeon Master")) {
            openDungeonGUI(event.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ColorChat.chat("&6Dungeon Selector"))) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();

            if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()) {
                String dungeonName = ColorChat.strip(event.getCurrentItem().getItemMeta().getDisplayName());
                Dungeon selectedDungeon = dungeons.get(dungeonName);

                if (selectedDungeon != null) {
                    handleDungeonSelection(player, selectedDungeon);
                }

                player.closeInventory();
            }
        }
    }

    private void handleDungeonSelection(Player player, Dungeon dungeon) {
        Location spawnLocation = dungeon.getNextAvailableSpawn();
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
            player.sendMessage(ColorChat.chat("&aTeleported to " + dungeon.getName() + "!"));
            player.sendMessage(ColorChat.chat("&cItems obtained inside dungeon drop on death!"));
            player.sendMessage(ColorChat.chat("&6Find 4 exits to teleport back to spawn."));
        } else {
            player.sendMessage(ColorChat.chat("&cNo available spawn points. Please try again later."));
        }
        player.setNoDamageTicks(30);

        // Example of adding delayed action after teleportation
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerStatsListener.updateWeaponStats(PlayerStats.getPlayerStats(player), player.getItemInHand());
            }
        }.runTaskLater(plugin, 20L); // Delay of 20 ticks (1 second)
    }
}
