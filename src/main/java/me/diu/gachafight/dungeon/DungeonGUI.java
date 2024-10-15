
package me.diu.gachafight.dungeon;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.dungeon.Dungeon;
import me.diu.gachafight.party.PartyManager;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.playerstats.PlayerStatsListener;
import me.diu.gachafight.utils.ColorChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class DungeonGUI implements Listener {

    private final GachaFight plugin;
    private final Map<String, Dungeon> dungeons = new HashMap<>();
    private final Map<String, Integer> dungeonSlots = new HashMap<>();
    private final Inventory gui;

    public DungeonGUI(GachaFight plugin) {
        this.plugin = plugin;
        initializeDungeons();
        this.gui = createDungeonGUI();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void initializeDungeons() {
        World spawnWorld = Bukkit.getWorld("Spawn");
        if (spawnWorld == null) {
            plugin.getLogger().warning("Spawn world not found!");
            return;
        }

        dungeons.put("Underground City", new Dungeon("Underground City",
                Arrays.asList(
                        "<dark_aqua>Level: <aqua>1-10",
                        "<red>PvP Enabled",
                        "<red>Damage Cap: 15, Armor Cap 18"
                ),
                Arrays.asList(
                        new Location(spawnWorld, -838.5, 5, 264.5),
                        new Location(spawnWorld, -967.5, 5, 264.5),
                        new Location(spawnWorld, -838.5, 5, 468.5),
                        new Location(spawnWorld, -967.5, 5, 468.5)
                )));
        dungeonSlots.put("Underground City", 10);

        dungeons.put("Goblin Camp", new Dungeon("Goblin Camp",
                Arrays.asList(
                        "<dark_aqua>Level: <aqua>10-20",
                        "<red>PvP Enabled",
                        "<red>Damage Cap: N/A, Armor Cap: N/A"
                ),
                Arrays.asList(
                        new Location(spawnWorld, -632.5, 4, 452.5),
                        new Location(spawnWorld, -773.5, 4, 442.5),
                        new Location(spawnWorld, -773.5, 4, 286.5),
                        new Location(spawnWorld, -622.5, 4, 289.5)
                )));
        dungeonSlots.put("Goblin Camp", 11);
    }

    private Inventory createDungeonGUI() {
        Inventory gui = Bukkit.createInventory(null, 27, ColorChat.chat("&6Dungeon Selector"));
        ItemStack cyanPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cyanPaneMeta = cyanPane.getItemMeta();
        cyanPaneMeta.setHideTooltip(true);
        cyanPane.setItemMeta(cyanPaneMeta);

        // Set the layout for the GUI
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, cyanPane);
        }

        for (Map.Entry<String, Dungeon> entry : dungeons.entrySet()) {
            Dungeon dungeon = entry.getValue();
            int slot = dungeonSlots.get(entry.getKey());

            ItemStack dungeonItem = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta meta = dungeonItem.getItemMeta();
            meta.setDisplayName(ColorChat.chat("&a" + dungeon.getName()));

            List<Component> lore = new ArrayList<>();
            for (String descLine : dungeon.getDescription()) {
                lore.add(MiniMessage.miniMessage().deserialize("<!i>" + descLine));
            }
            meta.lore(lore);

            dungeonItem.setItemMeta(meta);

            gui.setItem(slot, dungeonItem);
        }

        return gui;
    }
    public void openDungeonGUI(Player player) {
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

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.hasItemMeta()) {
                String dungeonName = ColorChat.strip(clickedItem.getItemMeta().getDisplayName());
                Dungeon selectedDungeon = dungeons.get(dungeonName);

                if (selectedDungeon != null) {
                    Player partyLeader = PartyManager.getPartyLeader(player);

                    if (partyLeader != null) {
                        // Player is in a party
                        if (partyLeader == player) {
                            // Player is the party leader
                            openPartyTeleportConfirmation(player, selectedDungeon);
                        } else {
                            openSelfTeleportConfirmation(player, selectedDungeon);}
                    } else {
                        // Player is not in a party
                        handleDungeonSelection(player, selectedDungeon);
                    }
                }
            }
            player.closeInventory();
        }
    }

    private void openPartyTeleportConfirmation(Player player, Dungeon dungeon) {
        Inventory confirmGui = Bukkit.createInventory(null, 9, ColorChat.chat("&6Confirm Party Teleport"));
        ItemStack confirmItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName(ColorChat.chat("&aConfirm Teleport Party"));
        confirmItem.setItemMeta(confirmMeta);

        ItemStack cancelItem = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName(ColorChat.chat("&cCancel"));
        cancelItem.setItemMeta(cancelMeta);

        confirmGui.setItem(3, confirmItem);
        confirmGui.setItem(5, cancelItem);

        player.openInventory(confirmGui);

        // Store the selected dungeon for the confirmation handler
        player.setMetadata("selectedDungeon", new FixedMetadataValue(plugin, dungeon));
    }

    private void openSelfTeleportConfirmation(Player player, Dungeon dungeon) {
        Inventory confirmGui = Bukkit.createInventory(null, 9, ColorChat.chat("&6Confirm Self Teleport"));

        ItemStack confirmItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName(ColorChat.chat("&aConfirm Teleport Self"));
        confirmItem.setItemMeta(confirmMeta);

        ItemStack cancelItem = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName(ColorChat.chat("&cCancel"));
        cancelItem.setItemMeta(cancelMeta);

        confirmGui.setItem(3, confirmItem);
        confirmGui.setItem(5, cancelItem);

        player.openInventory(confirmGui);

        // Store the selected dungeon for the confirmation handler
        player.setMetadata("selectedDungeon", new FixedMetadataValue(plugin, dungeon));
    }

    @EventHandler
    public void onConfirmationInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title.equals(ColorChat.chat("&6Confirm Party Teleport")) || title.equals(ColorChat.chat("&6Confirm Self Teleport"))) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.hasItemMeta()) {
                String itemName = clickedItem.getItemMeta().getDisplayName();
                if (itemName.contains("Confirm")) {
                    if (player.hasMetadata("selectedDungeon")) {
                        Dungeon dungeon = (Dungeon) player.getMetadata("selectedDungeon").get(0).value();
                        if (title.equals(ColorChat.chat("&6Confirm Party Teleport"))) {
                            handleDungeonSelection(player, dungeon);
                        } else {
                            teleportPlayerToDungeon(player, dungeon, dungeon.getNextAvailableSpawn());
                        }
                        player.removeMetadata("selectedDungeon", plugin);
                    }
                }
            }
            player.closeInventory();
        }
    }


    private void handleDungeonSelection(Player player, Dungeon dungeon) {
        Player partyLeader = PartyManager.getPartyLeader(player);
        Set<Player> partyMembers = new HashSet<>();

        if (partyLeader != null) {
            partyMembers = PartyManager.getPartyMembers(partyLeader);
            partyMembers.add(partyLeader); // Include the leader
        } else {
            partyMembers.add(player); // Solo player
        }

        for (Player member : partyMembers) {
            Location spawnLocation = dungeon.getNextAvailableSpawn();
            if (spawnLocation != null) {
                teleportPlayerToDungeon(member, dungeon, spawnLocation);
            } else {
                member.sendMessage(ColorChat.chat("&cNo available spawn points. Please try again later."));
                return; // Stop if there's no available spawn point
            }
        }
    }

    private void teleportPlayerToDungeon(Player player, Dungeon dungeon, Location spawnLocation) {
        Player partyLeader = PartyManager.getPartyLeader(player);
        boolean isPartyLeader = partyLeader == null || partyLeader == player;

        if (isPartyLeader || player.hasPermission("gachafight.dungeon.teleport.self")) {
            player.stopAllSounds();
            player.teleport(spawnLocation);
            player.sendMessage(ColorChat.chat("&aTeleported to " + dungeon.getName() + "!"));
            player.sendMessage(ColorChat.chat("&cItems obtained inside dungeon drop on death!"));
            player.sendMessage(ColorChat.chat("&6Find 4 exits to teleport back to spawn."));
            player.setNoDamageTicks(30);

            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    PlayerStatsListener.updateWeaponStats(PlayerStats.getPlayerStats(player), player.getItemInHand()), 20L);
        } else {
            player.sendMessage(ColorChat.chat("&cYou don't have permission to teleport yourself to a dungeon."));
        }
    }

}
