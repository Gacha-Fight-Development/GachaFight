package me.diu.gachafight.siege;

import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.party.PartyManager;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.DungeonUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class ArenaGateListener implements Listener {

    private final GachaFight plugin;
    public static Map<UUID, Arena> playerUsedKey = new HashMap<>();
    public static ActiveMob portal;

    public ArenaGateListener(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() != null && event.getClickedBlock().getX() == 1 && event.getClickedBlock().getZ() == 243) {
            String displayName = item.getItemMeta().getDisplayName();
            displayName = ColorChat.strip(displayName);
            if (displayName.startsWith("Arena Key")) {
                String romanNumeral = displayName.substring(10); // extract the Roman numeral from the display name
                int arenaNumber = romanNumeralToNumber(romanNumeral); // convert the Roman numeral to a number
                Arena arena = getArena(arenaNumber); // get the corresponding arena
                if (arena != null) {
                    // open the arena
                    Location location = event.getClickedBlock().getLocation().add(0.5, 0, 0);
                    MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob("ancient_portal-stone").orElse(null);

                    if (mob != null) {
                        AbstractLocation abstractLocation = BukkitAdapter.adapt(location);
                        ActiveMob activeMob = mob.spawn(abstractLocation, 1);
                        portal = activeMob;
                        // Launch player towards north with explosion effects
                        Location targetLocation = new Location(location.getWorld(), 0, 105, 231);
                        Vector direction = targetLocation.toVector().subtract(player.getLocation().toVector()).normalize();
                        direction.multiply(2); // adjust the speed
                        player.setVelocity(direction);
                        player.sendMessage(ColorChat.chat("&aYou have summoned the Gate of the Arena!"));
                        item.setAmount(item.getAmount() - 1);
                        location.getWorld().createExplosion(location, 2, false, false);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            playerUsedKey.put(player.getUniqueId(), arena);
                        }, 150);
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();
        if (playerUsedKey.containsKey(player.getUniqueId()) && playerLocation.getX() >= -3 && playerLocation.getX() <= 6 && playerLocation.getZ() <= 243 && playerLocation.getZ() >= 242) {
            Arena arena = playerUsedKey.get(player.getUniqueId());
            portal.remove();
            playerUsedKey.remove(player.getUniqueId());
            player.teleportAsync(arena.getLocation());
            SiegeGameMode siegeGameMode = new SiegeGameMode(GachaFight.getInstance(), player, arena, 1);
            siegeGameMode.start();
            player.sendMessage("&aSiege game started!");

            // Teleport party members to the player's location
            if (PartyManager.isInParty(player)) {
                Set<OfflinePlayer> partyMembers = PartyManager.getPartyMembers(PartyManager.getPartyLeader(PartyManager.getPartyLeader(player)));
                for (OfflinePlayer member : partyMembers) {
                    if (member.isOnline()) {
                        Player memberPlayer = member.getPlayer();
                        if (!DungeonUtils.isSafezone(memberPlayer.getLocation())) {
                            memberPlayer.sendMessage(ColorChat.chat("&cNot in Safezone, Party Leader left you."));
                        } else {
                            memberPlayer.teleport(playerLocation);
                        }
                    }
                }
            }
        }
    }
    // helper method to convert Roman numeral to number
    private int romanNumeralToNumber(String romanNumeral) {
        switch (romanNumeral) {
            case "I":
                return 1;
            case "II":
                return 2;
            case "III":
                return 3;
            // add more cases for other Roman numerals
            default:
                return -1; // return -1 if the Roman numeral is not recognized
        }
    }

    // helper method to get the corresponding arena
    private Arena getArena(int arenaNumber) {
        switch (arenaNumber) {
            case 1:
                return new Arena1();
            case 2:
            // add more cases for other arenas
            default:
                return null; // return null if the arena number is not recognized
        }
    }
}
