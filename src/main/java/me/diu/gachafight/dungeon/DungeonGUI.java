package me.diu.gachafight.dungeon;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.playerstats.PlayerStatsListener;
import me.diu.gachafight.utils.ColorChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Array;
import java.util.*;

public class DungeonGUI implements Listener {

    private final GachaFight plugin;
    private final Map<String, DungeonInstance> dungeonInstances = new HashMap<>();

    public DungeonGUI(GachaFight plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // Open the Dungeon Selection GUI
    public void openDungeonGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ColorChat.chat("&6Dungeon Selector"));

        // Example dungeon options
        ItemStack testDungeonItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta testDungeonMeta = testDungeonItem.getItemMeta();
        testDungeonMeta.setDisplayName(ColorChat.chat("&aGoblin Camp"));
        List<Component> newLore = new ArrayList<>();
        newLore.add(MiniMessage.miniMessage().deserialize("<!i><dark_aqua>Level: <aqua>1-10"));
        newLore.add(MiniMessage.miniMessage().deserialize("<!i><red>PvP Enabled"));
        testDungeonMeta.lore(newLore);
        testDungeonItem.setItemMeta(testDungeonMeta);
        gui.setItem(0, testDungeonItem);

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
                String dungeonName = event.getCurrentItem().getItemMeta().getDisplayName();

                // Handle different dungeon options
                if (dungeonName.equals(ColorChat.chat("&aGoblin Camp"))) {
                    handleDungeonSelection(player, "Goblin Camp");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            PlayerStatsListener.updateWeaponStats(PlayerStats.getPlayerStats(player), player.getItemInHand());
                        }
                    }.runTaskLater(plugin, 20L);
                }

                player.closeInventory();
            }
        }
    }

    private void handleDungeonSelection(Player player, String dungeonName) {
        DungeonInstance instance = getAvailableDungeonInstance(dungeonName);
        Location spawnLocation = instance.getNextAvailableSpawn();
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
            player.sendMessage(ColorChat.chat("&aTeleported to " + dungeonName + "!"));
            player.sendMessage(ColorChat.chat("&cItems Obtained inside dungeon are normally drop on death!"));
            player.sendMessage(ColorChat.chat("&6Locate the 4 exits inside the dungeon to teleport back to spawn."));
        } else {
            player.sendMessage(ColorChat.chat("&cNo available spawn points. Please try again later."));
        }
    }

    private DungeonInstance getAvailableDungeonInstance(String dungeonName) {
        DungeonInstance instance = dungeonInstances.computeIfAbsent(dungeonName, k -> new DungeonInstance(dungeonName));
        return instance;
    }
}
