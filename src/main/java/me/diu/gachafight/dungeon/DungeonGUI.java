
package me.diu.gachafight.dungeon;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.combat.DamageListener;
import me.diu.gachafight.dungeon.Dungeon;
import me.diu.gachafight.party.PartyManager;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.playerstats.PlayerStatsListener;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.DungeonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
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
import java.util.stream.Collectors;

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
                    OfflinePlayer partyLeader = PartyManager.getPartyLeader(player);

                    if (partyLeader == null) {
                        // Solo player - teleport instantly
                        System.out.println("Player is not in a party, teleporting alone");
                        Location spawnLocation = selectedDungeon.getNextAvailableSpawn();
                        if (spawnLocation != null) {
                            teleportPlayerToDungeon(player, selectedDungeon, spawnLocation);
                        } else {
                            player.sendMessage(ColorChat.chat("&cNo available spawn points. Please try again later."));
                        }
                    } else if (partyLeader.equals(player)) {
                        // Party leader - show confirmation GUI
                        openPartyTeleportConfirmation(player, selectedDungeon);
                    } else {
                        // Party member (not leader)
                        Location spawnLocation = selectedDungeon.getNextAvailableSpawn();
                        teleportPlayerToDungeon(player, selectedDungeon, spawnLocation);
                    }
                } else {
                    System.out.println("Selected dungeon is null");
                }
            }
        }
    }


    public void openPartyTeleportConfirmation(Player player, Dungeon dungeon) {
        Inventory confirmGui = Bukkit.createInventory(null, 9, ColorChat.chat("&6Confirm Party Teleport"));

        // Create and set confirm item
        ItemStack confirmItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName(ColorChat.chat("&aConfirm Teleport Party"));
        confirmItem.setItemMeta(confirmMeta);

        // Create and set cancel item
        ItemStack cancelItem = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName(ColorChat.chat("&cCancel"));
        cancelItem.setItemMeta(cancelMeta);

        // Place items in the inventory
        confirmGui.setItem(3, confirmItem);
        confirmGui.setItem(5, cancelItem);
        // Store metadata for the selected dungeon
        player.setMetadata("selectedDungeon", new FixedMetadataValue(plugin, dungeon));
        // Now explicitly open the inventory for the player
        player.openInventory(confirmGui);
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
        OfflinePlayer partyLeader = PartyManager.getPartyLeader(player);
        Set<OfflinePlayer> partyMembers;

        if (partyLeader != null) {
            partyMembers = PartyManager.getPartyMembers(partyLeader);
        } else {
            partyMembers = new HashSet<>();
            partyMembers.add(player); // Solo player
        }

        // Get a single spawn location for the entire party
        Location spawnLocation = dungeon.getNextAvailableSpawn();
        if (spawnLocation != null) {
            for (OfflinePlayer member : partyMembers) {
                if (member.isOnline()) {
                    Player onlineMember = member.getPlayer();
                    if (DungeonUtils.isSafezone(onlineMember.getLocation())) {
                        teleportPlayerToDungeon(onlineMember, dungeon, spawnLocation);
                    } else {
                        onlineMember.sendMessage(ColorChat.chat("&cNot in Safezone, Party Leader left you."));
                    }
                }
            }
        } else {
            System.out.println("No available spawn point for the party");
            for (OfflinePlayer member : partyMembers) {
                if (member.isOnline()) {
                    member.getPlayer().sendMessage(ColorChat.chat("&cNo available spawn points. Please try again later."));
                }
            }
        }
    }

    private void teleportPlayerToDungeon(Player player, Dungeon dungeon, Location spawnLocation) {
        OfflinePlayer partyLeader = PartyManager.getPartyLeader(player);
        boolean isPartyLeader = partyLeader == null || partyLeader.equals(player);

        System.out.println("Teleporting " + player.getName() + " to " + dungeon.getName() + " at " + spawnLocation);

        player.stopAllSounds();
        player.teleport(spawnLocation);
        player.sendMessage(ColorChat.chat("&aTeleported to " + dungeon.getName() + "!"));
        player.sendMessage(ColorChat.chat("&cItems obtained inside dungeon drop on death!"));
        player.sendMessage(ColorChat.chat("&6Find 4 exits to teleport back to spawn."));
        player.setNoDamageTicks(30);

        Bukkit.getScheduler().runTaskLater(plugin, () ->
                PlayerStatsListener.updateWeaponStats(PlayerStats.getPlayerStats(player), player.getItemInHand()), 20L);

        if (isPartyLeader) {
            player.sendMessage(ColorChat.chat("&aYou have entered the dungeon " + (partyLeader == null ? "alone" : "with your party") + "."));
        }

        System.out.println(player.getName() + " teleported successfully");
    }
}
