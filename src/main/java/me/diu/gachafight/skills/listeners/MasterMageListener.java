package me.diu.gachafight.skills.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.hooks.VaultHook;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.skills.managers.MobDropSelector;
import me.diu.gachafight.utils.ColorChat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MasterMageListener implements Listener {
    private final GachaFight plugin;

    public MasterMageListener(GachaFight plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractNPC(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked().getName().equalsIgnoreCase("master mage"))) {
            return; // Ensure only the Master Mage is interacted with
        }
        Player player = event.getPlayer();
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        if (stats.getLevel() < 10) {
            player.sendMessage(ColorChat.chat("&cYou need at least level 10!"));
        }
        openGUI(player);
    }

    public static void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize("<!i><light_purple>Master Mage"));
        ItemStack filler = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta cyanPaneMeta = filler.getItemMeta();
        cyanPaneMeta.setHideTooltip(true);
        filler.setItemMeta(cyanPaneMeta);
        // Set the layout for the GUI
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        ColorChat.createItem(inv, Material.SEA_LANTERN, 1, 11, "&d&lMagical Orb (Private)", "&6Cost Per Use: &e$10k",
                "&6Info: &7This Magical Orb will give you a private hint on", "&7which mob will drop a Rarity of Rare Or Higher",
                "&7Skill Book for a High Price! You will only have", "&c30 Minutes &7To Obtain it after revealed", "&7Ends when a Player gets Skill Book.");

        ColorChat.createItem(inv, Material.ENDER_EYE, 1, 15, "&5&lForesee (Broadcast)", "&6Cost Per Use: &e$5k",
                "&6Info: &7This Ender Eye will broadcast to all players", "&7which mob will drop a Rarity of Rare Or Higher",
                "&7Skill Book! Everyone will have", "&c30 Minutes &7To Obtain it after revealed.", "&7Ends when a Player gets Skill Book.");
        ColorChat.createItem(inv, Material.NETHER_STAR, 1, 13, "&6&lReroll Mob", "&6Cost: &e$2.5k",
                "&6Info: &7Reroll the current mob for a new one.", "&cOnly works if you revealed the current mob.");
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = ChatColor.stripColor(event.getView().getTitle());
        Player player = (Player) event.getWhoClicked();
        PlayerStats stats = PlayerStats.getPlayerStats(player);
        if (title.equalsIgnoreCase("master mage")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            if (event.getCurrentItem().getType().equals(Material.SEA_LANTERN)) {
                handlePrivateOrb(player);
            } else if (event.getCurrentItem().getType().equals(Material.ENDER_EYE)) {
                handleBroadcastOrb(player);
            } else if (event.getCurrentItem().getType().equals(Material.NETHER_STAR)) {
                handleRerollMob(player);
            }
        }
    }

    private void handlePrivateOrb(Player player) {
        if (MobDropSelector.getMob() != null) {
            player.sendMessage(ColorChat.chat("&cAnother player has already revealed a Mob!"));
            return;
        }
        if (VaultHook.getBalance(player) < 10000) {
            player.sendMessage(ColorChat.chat("&cYou need at least 10k to use the Private Magical Orb!"));
            return;
        }
        VaultHook.withdraw(player, 10000);
        MobDropSelector.changeMobs(player, false);
        player.sendMessage(ColorChat.chat("&7[&dMagical Orb&7] &a" + MobDropSelector.getMob() + " &7will now drop Rare+ Skill Book"));
    }

    private void handleBroadcastOrb(Player player) {
        if (MobDropSelector.getMob() != null) {
            player.sendMessage(ColorChat.chat("&cAnother player has already revealed a Mob!"));
            return;
        }
        if (VaultHook.getBalance(player) < 5000) {
            player.sendMessage(ColorChat.chat("&cYou need at least 5k to use the Broadcast Magical Orb!"));
            return;
        }
        VaultHook.withdraw(player, 5000);
        MobDropSelector.changeMobs(player, true);
        String message = ColorChat.chat("&7[&5Foresee&7] &a" + MobDropSelector.getMob() + " &7will now drop Rare+ Skill Book");
        Bukkit.broadcastMessage(message);
    }
    private void handleRerollMob(Player player) {
        if (MobDropSelector.getMob() == null) {
            player.sendMessage(ColorChat.chat("&cThere is no mob currently revealed to reroll!"));
            return;
        }
        if (!MobDropSelector.getPlayername().equals(player.getName())) {
            player.sendMessage(ColorChat.chat("&cYou can only reroll the mob if you revealed it!"));
            return;
        }

        if (VaultHook.getBalance(player) < 2500) {
            player.sendMessage(ColorChat.chat("&cYou need at least 2.5k to reroll the mob!"));
            return;
        }

        VaultHook.withdraw(player, 2500);
        MobDropSelector.changeMobs(player, MobDropSelector.isBroadcast());
        if (MobDropSelector.isBroadcast()) {
            String message = ColorChat.chat("&7[&5Foresee&7] &a" + MobDropSelector.getMob() + " &7will now drop Rare+ Skill Book");
            Bukkit.broadcastMessage(message);
        } else {
            player.sendMessage(ColorChat.chat("&7[&dMagical Orb&7] &a" + MobDropSelector.getMob() + " &7will now drop Rare+ Skill Book"));
        }
        player.sendMessage(ColorChat.chat("&aYou have successfully rerolled the mob for 2.5k!"));
    }
}
