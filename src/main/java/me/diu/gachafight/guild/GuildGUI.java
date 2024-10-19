package me.diu.gachafight.guild;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.guild.GuildManager;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class GuildGUI implements Listener {

    private final GachaFight plugin;

    public GuildGUI(GachaFight plugin) {
        this.plugin = plugin;
        new GuildRequestManager(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

    }

    public static void openMainMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, ColorChat.chat("&8Guild Menu"));

        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) {
            // Player is not in a guild
            inventory.setItem(12, createGuiItem(Material.DIAMOND, "&bJoin Guild", "&7Click to join an existing guild"));
            inventory.setItem(14, createGuiItem(Material.EMERALD_BLOCK, "&aCreate Guild", "&6/guild create <Name>"));

        } else {
            // Player is in a guild
            inventory.setItem(10, createGuiItem(Material.BOOK, "&eList Members", "&7Click to view guild members"));
            inventory.setItem(16, createGuiItem(Material.REDSTONE, "&cLeave Guild", "&7Click to leave the guild"));
            if (GuildManager.isGuildLeader(player, guildId) || GuildManager.isCoLeader(player, guildId)) {
                inventory.setItem(25, createGuiItem(Material.BOOK, "&6Manage Requests", "&7Click to view and manage guild join requests"));
                inventory.setItem(26, createGuiItem(Material.COMPARATOR, "&6Guild Settings", "&7Click to change guild settings"));
            }
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;
        int slot = event.getSlot();
        if (event.getClickedInventory().equals(event.getView().getTopInventory())) {
            if (ColorChat.strip(event.getView().getTitle()).equalsIgnoreCase("Guild Menu")) {
                event.setCancelled(true);
                handleClick(player, slot);
            }
            if (ColorChat.strip(event.getView().getTitle()).equalsIgnoreCase("Join a Guild")) {
                event.setCancelled(true);

                NamespacedKey key = new NamespacedKey(GachaFight.getInstance(), "guildId");
                String guildId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

                if (guildId != null) {
                    handleJoinGuildClick(player, guildId);
                }
            }
            if (ColorChat.strip(event.getView().getTitle()).equalsIgnoreCase("Guild Requests")) {
                event.setCancelled(true);
                handleGuildRequestClick(event);
            }
        }
    }


    public static void handleClick(Player player, int slot) {
        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) {
            // Player is not in a guild
            switch (slot) {
                case 12:
                    openJoinGuildMenu(player);
                    break;
            }
        } else {
            // Player is in a guild
            switch (slot) {
                case 10:
                    listGuildMembers(player);
                    break;
                case 12:
                    //guild upgrades
                    break;
                case 14:
                    showGuildInfo(player);
                    break;
                case 16:
                    leaveGuild(player);
                    break;
                case 25:
                    if (GuildManager.isGuildLeader(player, guildId) || GuildManager.isCoLeader(player, guildId)) {
                        openGuildRequestsMenu(player);
                    }
                    break;
                case 26:
                    if (GuildManager.isGuildLeader(player, guildId) || GuildManager.isCoLeader(player, guildId)) {
                        openGuildSettings(player);
                    }
                    break;
            }
        }
    }


    private static void leaveGuild(Player player) {
        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) {
            player.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        GuildManager.removeFromGuild(guildId, player);
        player.sendMessage(ColorChat.chat("&aYou have left the guild."));
    }

    private static void listGuildMembers(Player player) {
        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) {
            player.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        String guildName = GuildManager.getGuildName(guildId);
        int guildLevel = GuildManager.getGuildLevel(guildId);

        player.sendMessage(ColorChat.chat("&6=== " + guildName + " (Level " + guildLevel + ") ==="));
        player.sendMessage(ColorChat.chat("&eMembers (" + GuildManager.getGuildMembers(guildId).size() + "/" + GuildManager.MAX_GUILD_SIZE + "):"));

        for (OfflinePlayer member : GuildManager.getGuildMembers(guildId)) {
            String status = member.isOnline() ? "&a[Online]" : "&c[Offline]";
            String role = GuildManager.isGuildLeader(member, guildId) ? "&6[Leader]" :
                    GuildManager.isCoLeader(member, guildId) ? "&e[Co-Leader]" : "&7[Member]";
            player.sendMessage(ColorChat.chat(status + " " + role + " &f" + member.getName()));
        }
    }

    private static void showGuildInfo(Player player) {
        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) {
            player.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        String guildName = GuildManager.getGuildName(guildId);
        int guildLevel = GuildManager.getGuildLevel(guildId);
        int memberCount = GuildManager.getGuildMembers(guildId).size();

        player.sendMessage(ColorChat.chat("&6=== Guild Information ==="));
        player.sendMessage(ColorChat.chat("&eName: &f" + guildName));
        player.sendMessage(ColorChat.chat("&eLevel: &f" + guildLevel));
        player.sendMessage(ColorChat.chat("&eMembers: &f" + memberCount + "/" + GuildManager.MAX_GUILD_SIZE));
        // Add more guild information as needed
    }

    public static void openJoinGuildMenu(Player player) {
        List<String> guildIds = GuildManager.getAllGuildIds();
        int inventorySize = (int) Math.ceil(guildIds.size() / 9.0) * 9; // Round up to nearest multiple of 9
        Inventory inventory = Bukkit.createInventory(null, inventorySize, ColorChat.chat("&8Join a Guild"));

        for (int i = 0; i < guildIds.size(); i++) {
            String guildId = guildIds.get(i);
            String guildName = GuildManager.getGuildName(guildId);
            String logoMaterial = GuildManager.getGuildLogo(guildId);
            OfflinePlayer coLeader = GuildManager.getCoLeader(guildId);
            String coLeaderName = (coLeader != null) ? coLeader.getName() : "N/A";
            ItemStack logo = createJoinGuildItem(Material.valueOf(logoMaterial), "&b" + guildName, guildId,
                    "&6Leader: " + GuildManager.getGuildLeader(guildId).getName(),
                    "&eCo-Leader: " + coLeaderName ,
                    "&7Members: " + GuildManager.getGuildMembers(guildId).size() + "/" + GuildManager.MAX_GUILD_SIZE,
                    "&7Level: " + GuildManager.getGuildLevel(guildId),
                    "&eClick to join!");
            inventory.setItem(i, logo);
        }
        player.openInventory(inventory);
    }
    public void handleJoinGuildClick(Player player, String guildId) {
        UUID playerUUID = player.getUniqueId();
        GuildRequest request = new GuildRequest(playerUUID, player.getName(), guildId);

        GuildRequestManager.addRequest(request,
                () -> player.sendMessage(ColorChat.chat("&aYour request to join the guild has been sent!")),
                () -> player.sendMessage(ColorChat.chat("&cYou already have a pending request for this guild."))
        );
    }

    private static ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorChat.chat(name));
        meta.setLore(Arrays.stream(lore).map(ColorChat::chat).collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }
    public static void openGuildSettings(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, ColorChat.chat("&8Guild Settings"));

        inventory.setItem(11, createGuiItem(Material.NAME_TAG, "&6/guild changeicon <icon>", "&7Cost: 50,000",
                "&6Info: &7Chat Icon is 1 character Only!"));
        inventory.setItem(15, createGuiItem(Material.PAINTING, "&6/guild changelogo <logo>", "&7Cost: 50,000",
                "&6Info: &7Logo is a Minecraft Item Type, You can", "&7use Tab Complete for command. Custom Logo: /buy"));

        player.openInventory(inventory);
    }

    private static ItemStack createJoinGuildItem(Material material, String name, String guildId, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorChat.chat(name));

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ColorChat.chat(line));
        }
        meta.setLore(coloredLore);

        // Store the guildId in the item's persistent data container
        NamespacedKey key = new NamespacedKey(GachaFight.getInstance(), "guildId");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, guildId);

        item.setItemMeta(meta);
        return item;
    }

    public static void openGuildRequestsMenu(Player player) {
        String guildId = GuildManager.getGuildId(player);
        if (guildId == null || (!GuildManager.isGuildLeader(player, guildId) && !GuildManager.isCoLeader(player, guildId))) {
            player.sendMessage(ColorChat.chat("&cYou don't have permission to manage guild requests."));
            return;
        }

        Map<UUID, GuildRequest> requests = GuildRequestManager.getGuildRequests(guildId);
        int inventorySize = (int) Math.ceil(requests.size() / 9.0) * 9; // Round up to nearest multiple of 9
        Inventory inventory = Bukkit.createInventory(null, inventorySize, ColorChat.chat("&8Guild Requests"));

        int i = 0;
        for (GuildRequest request : requests.values()) {
            ItemStack item = createRequestItem(request);
            inventory.setItem(i++, item);
        }

        player.openInventory(inventory);
    }

    private static ItemStack createRequestItem(GuildRequest request) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorChat.chat("&b" + request.getPlayerName()));
        List<String> lore = new ArrayList<>();
        lore.add(ColorChat.chat("&7Requested: " + request.getRequestTime().toString()));
        lore.add(ColorChat.chat("&aLeft-click to accept"));
        lore.add(ColorChat.chat("&cRight-click to reject"));
        meta.setLore(lore);

        // Store the player UUID in the item's persistent data container
        NamespacedKey key = new NamespacedKey(GachaFight.getInstance(), "playerUUID");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, request.getPlayerUUID().toString());

        item.setItemMeta(meta);
        return item;
    }
    private void handleGuildRequestClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(GachaFight.getInstance(), "playerUUID");
        String uuidString = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (uuidString == null) return;

        UUID playerUUID = UUID.fromString(uuidString);
        String guildId = GuildManager.getGuildId(player);

        if (event.isLeftClick()) {
            // Accept request
            GuildRequestManager.removeRequest(guildId, playerUUID,
                    () -> {
                        GuildManager.addToGuild(guildId, Bukkit.getOfflinePlayer(playerUUID));
                        player.sendMessage(ColorChat.chat("&aAccepted " + meta.getDisplayName() + "'s request to join the guild."));
                        updateGuildRequestsMenu(player);
                    },
                    () -> player.sendMessage(ColorChat.chat("&cFailed to accept the request. Please try again."))
            );
        } else if (event.isRightClick()) {
            // Reject request
            GuildRequestManager.removeRequest(guildId, playerUUID,
                    () -> {
                        player.sendMessage(ColorChat.chat("&cRejected " + meta.getDisplayName() + "'s request to join the guild."));
                        updateGuildRequestsMenu(player);
                    },
                    () -> player.sendMessage(ColorChat.chat("&cFailed to reject the request. Please try again."))
            );
        }
    }

    private void updateGuildRequestsMenu(Player player) {
        Bukkit.getScheduler().runTask(GachaFight.getInstance(), () -> openGuildRequestsMenu(player));
    }

    //todo: make a player head system for managing requests. and also make a player head system for viewing guild member
    //todo: upgrades. Quest give guild exp, Guild Contribution Records (clears every monday), Guild Managing Player GUI.
}


