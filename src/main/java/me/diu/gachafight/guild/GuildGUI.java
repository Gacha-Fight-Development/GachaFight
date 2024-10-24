package me.diu.gachafight.guild;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.guild.GuildManager;
import me.diu.gachafight.hooks.VaultHook;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
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
        ItemStack filler = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta fillermeta = filler.getItemMeta();
        filler.setItemMeta(fillermeta);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }
        if (guildId == null) {
            // Player is not in a guild
            inventory.setItem(12, createGuiItem(Material.DIAMOND, "&bJoin Guild", "&7Click to join an existing guild"));
            inventory.setItem(14, createGuiItem(Material.EMERALD_BLOCK, "&aCreate Guild", "&6/guild create <Name>"));

        } else {
            // Player is in a guild
            inventory.setItem(10, createGuiItem(Material.BOOK, "&eList Members", "&7Click to view guild members"));
            inventory.setItem(11, createGuiItem(Material.GOLD_INGOT, "&eGuild Bank", "&7Click to open guild bank"));
            inventory.setItem(12, createGuiItem(Material.ENCHANTING_TABLE, "&eGuild Upgrades", "&7Click to view guild upgrades"));
            inventory.setItem(13, createGuiItem(Material.BARRIER, "&c&lComing Soon"));
            inventory.setItem(14, createGuiItem(Material.BARRIER, "&c&lComing Soon"));
            inventory.setItem(15, createGuiItem(Material.BARRIER, "&c&lComing Soon"));
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
            if (ColorChat.strip(event.getView().getTitle()).equalsIgnoreCase("Guild Bank")) {
                event.setCancelled(true);
                handleBankClick(player, slot);
            }
            if (ColorChat.strip(event.getView().getTitle()).equalsIgnoreCase("Guild Upgrades")) {
                event.setCancelled(true);
                handleUpgradeMenuClick(player, slot);
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
                case 11:
                    openGuildBankGUI(player);
                    break;
                case 12:
                    openGuildUpgradeMenu(player);
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

    public static void listGuildMembers(Player player) {
        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) {
            player.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        String guildName = GuildManager.getGuildName(guildId);
        int guildLevel = GuildManager.getGuildLevel(guildId);
        Set<OfflinePlayer> memberSet = GuildManager.getGuildMembers(guildId);

        // Convert Set to List for sorting
        List<OfflinePlayer> members = new ArrayList<>(memberSet);

        // Sort members: Leader first, then Co-Leaders, then regular members
        members.sort((m1, m2) -> {
            if (GuildManager.isGuildLeader(m1, guildId)) return -1;
            if (GuildManager.isGuildLeader(m2, guildId)) return 1;
            if (GuildManager.isCoLeader(m1, guildId)) return -1;
            if (GuildManager.isCoLeader(m2, guildId)) return 1;
            return 0;
        });

        Inventory inventory = Bukkit.createInventory(null, 54, ColorChat.chat("&6" + guildName + " Members"));

        for (int i = 0; i < members.size() && i < 54; i++) {
            OfflinePlayer member = members.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(member);

            String status = member.isOnline() ? "&a[Online]" : "&c[Offline]";
            String role = GuildManager.isGuildLeader(member, guildId) ? "&6[Leader]" :
                    GuildManager.isCoLeader(member, guildId) ? "&e[Co-Leader]" : "&7[Member]";

            meta.setDisplayName(ColorChat.chat(role + " &f" + member.getName()));
            List<String> lore = new ArrayList<>();
            lore.add(ColorChat.chat(status));

            int expContribution = GuildContributionManager.getExpContribution(guildId, member.getUniqueId());
            int goldContribution = GuildContributionManager.getGoldContribution(guildId, member.getUniqueId());
            lore.add(ColorChat.chat("&bEXP Contribution: &f" + expContribution));
            lore.add(ColorChat.chat("&6Gold Contribution: &f" + goldContribution));

            meta.setLore(lore);

            head.setItemMeta(meta);
            inventory.setItem(i, head);
        }

        player.openInventory(inventory);
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
            ItemStack logo = createJoinGuildItem(Material.valueOf(logoMaterial), "&b" + guildName + " " + GuildManager.getGuildChatIcon(guildId), guildId,
                    "&bLevel : " + GuildManager.getGuildLevel(guildId),
                    "&6Leader: " + GuildManager.getGuildLeader(guildId).getName(),
                    "&eCo-Leader: " + coLeaderName ,
                    "&7Members: " + GuildManager.getGuildMembers(guildId).size() + "/" + GuildManager.MAX_GUILD_SIZE,
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
        Inventory inventory = Bukkit.createInventory(null, 54, ColorChat.chat("&8Guild Requests"));

        int i = 0;
        for (GuildRequest request : requests.values()) {
            ItemStack item = createRequestItem(request);
            inventory.setItem(i++, item);
        }

        player.openInventory(inventory);
    }

    private static ItemStack createRequestItem(GuildRequest request) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();

        // Set the player's skin
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(request.getPlayerUUID()));

        List<String> lore = new ArrayList<>();
        lore.add(ColorChat.chat("&7Requested: " + request.getRequestTime().toString()));
        lore.add(ColorChat.chat("&aLeft-click to accept"));
        lore.add(ColorChat.chat("&cRight-click to reject"));
        skullMeta.setLore(lore);

        // Store the player UUID in the item's persistent data container
        NamespacedKey key = new NamespacedKey(GachaFight.getInstance(), "playerUUID");
        skullMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, request.getPlayerUUID().toString());

        item.setItemMeta(skullMeta);
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
                        if (Bukkit.getOfflinePlayer(playerUUID).isOnline()) {
                            Player onlineRequester = Bukkit.getPlayer(playerUUID);
                            onlineRequester.sendMessage(ColorChat.chat("&aAccepted join request into " + GuildManager.getGuildName(guildId) + " Guild!"));
                        }
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
                        if (Bukkit.getOfflinePlayer(playerUUID).isOnline()) {
                            Player onlineRequester = Bukkit.getPlayer(playerUUID);
                            onlineRequester.sendMessage(ColorChat.chat("&cRejected join request from " + GuildManager.getGuildName(guildId) + " Guild"));
                        }
                    },
                    () -> player.sendMessage(ColorChat.chat("&cFailed to reject the request. Please try again."))
            );
        }
    }

    private void updateGuildRequestsMenu(Player player) {
        Bukkit.getScheduler().runTask(GachaFight.getInstance(), () -> openGuildRequestsMenu(player));
    }

    public static void openGuildUpgradeMenu(Player player) {
        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) {
            player.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        Inventory inventory = Bukkit.createInventory(null, 27, ColorChat.chat("&8Guild Upgrades"));

        // EXP Multiplier Upgrade
        int expLevel = GuildManager.getUpgradeLevel(guildId, "exp_multiplier");
        double currentExpMulti = 1 + (expLevel * 0.1); // 10% increase per level
        int expUpgradeCost = 10000 * (expLevel + 1);
        ItemStack expItem = createUpgradeItem(Material.EXPERIENCE_BOTTLE, "&bEXP Multiplier",
                "&7Current: &e" + String.format("%.1fx", currentExpMulti),
                "&7Next Level: &e" + String.format("%.1fx", currentExpMulti + 0.1),
                "&7Cost: &6" + expUpgradeCost + " gold",
                "",
                "&eClick to upgrade!");

        // Gold Multiplier Upgrade
        int goldLevel = GuildManager.getUpgradeLevel(guildId, "gold_multiplier");
        double currentGoldMulti = 1 + (goldLevel * 0.05); // 5% increase per level
        int goldUpgradeCost = 15000 * (goldLevel + 1);
        ItemStack goldItem = createUpgradeItem(Material.GOLD_INGOT, "&6Gold Multiplier",
                "&7Current: &e" + String.format("%.2fx", currentGoldMulti),
                "&7Next Level: &e" + String.format("%.2fx", currentGoldMulti + 0.05),
                "&7Cost: &6" + goldUpgradeCost + " gold",
                "",
                "&eClick to upgrade!");

        inventory.setItem(11, expItem);
        inventory.setItem(15, goldItem);

        player.openInventory(inventory);
    }

    private static ItemStack createUpgradeItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorChat.chat(name));
        meta.setLore(Arrays.stream(lore).map(ColorChat::chat).collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }



    public static void openGuildBankGUI(Player player) {
        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) {
            player.sendMessage(ColorChat.chat("&cYou are not in a guild."));
            return;
        }

        Inventory inventory = Bukkit.createInventory(null, 27, ColorChat.chat("&8Guild Bank"));

        // Display current guild balance
        int guildBalance = GuildManager.getGuildGold(guildId);
        ItemStack balanceItem = createGuiItem(Material.GOLD_BLOCK, "&6Guild Balance",
                "&7Current Balance: &e" + guildBalance + " gold", "&6/guild deposit <amount> to contribute");
        inventory.setItem(13, balanceItem);

        inventory.setItem(11, createGuiItem(Material.GOLD_INGOT, "&aDeposit 1,000 gold",
                "&7Click to deposit 1,000 gold to the guild bank"));
        inventory.setItem(12, createGuiItem(Material.GOLD_BLOCK, "&aDeposit 10,000 gold",
                "&7Click to deposit 10,000 gold to the guild bank"));

        // Withdraw options (only for leader and co-leader)
        if (GuildManager.isGuildLeader(player, guildId) || GuildManager.isCoLeader(player, guildId)) {
            inventory.setItem(14, createGuiItem(Material.GOLD_NUGGET, "&cWithdraw 1,000 gold",
                    "&7Click to withdraw 1,000 gold from the guild bank"));
            inventory.setItem(15, createGuiItem(Material.GOLD_NUGGET, "&cWithdraw 10,000 gold",
                    "&7Click to withdraw 10,000 gold from the guild bank"));
        }

        player.openInventory(inventory);
    }

    public void handleBankClick(Player player, int slot) {

        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) return;
        switch (slot) {
            case 11:
                handleDeposit(player, guildId, 1000);
                break;
            case 12:
                handleDeposit(player, guildId, 10000);
                break;
            case 14:
                if (GuildManager.isGuildLeader(player, guildId) || GuildManager.isCoLeader(player, guildId)) {
                    handleWithdraw(player, guildId, 1000);
                }
                break;
            case 15:
                if (GuildManager.isGuildLeader(player, guildId) || GuildManager.isCoLeader(player, guildId)) {
                    handleWithdraw(player, guildId, 10000);
                }
                break;
        }

        // Refresh the GUI to show updated balance
        openGuildBankGUI(player);
    }

    private void handleDeposit(Player player, String guildId, int amount) {
        if (VaultHook.getBalance(player) >= amount) {
            VaultHook.withdraw(player, amount);
            GuildManager.addGuildGold(guildId, amount);
            GuildContributionManager.addGoldContribution(guildId, player.getUniqueId(), amount);
            player.sendMessage(ColorChat.chat("&aSuccessfully deposited " + amount + " gold to the guild bank."));
        } else {
            player.sendMessage(ColorChat.chat("&cYou don't have enough gold to make this deposit."));
        }
    }

    private void handleWithdraw(Player player, String guildId, int amount) {
        if (GuildManager.getGuildGold(guildId) >= amount) {
            GuildManager.removeGuildGold(guildId, amount);
            VaultHook.deposit(player, amount);
            int currentContribution = GuildContributionManager.getGoldContribution(guildId, player.getUniqueId());
            GuildContributionManager.addGoldContribution(guildId, player.getUniqueId(), -Math.min(amount, currentContribution));
            player.sendMessage(ColorChat.chat("&aSuccessfully withdrew " + amount + " gold from the guild bank."));
        } else {
            player.sendMessage(ColorChat.chat("&cThe guild bank doesn't have enough gold for this withdrawal."));
        }
    }

    public void handleUpgradeMenuClick(Player player, int slot) {
        String guildId = GuildManager.getGuildId(player);
        if (guildId == null) return;

        switch (slot) {
            case 11:
                // EXP Multiplier Upgrade
                int expLevel = GuildManager.getUpgradeLevel(guildId, "exp_multiplier");
                int expUpgradeCost = getExpUpgradeCost(expLevel);
                if (GuildManager.getGuildGold(guildId) >= expUpgradeCost) {
                    GuildManager.incrementUpgradeLevel(guildId, "exp_multiplier");
                    player.sendMessage(ColorChat.chat("&aEXP Multiplier upgraded successfully!"));
                    openGuildUpgradeMenu(player);
                } else {
                    player.sendMessage(ColorChat.chat("&cNot enough gold to upgrade the EXP Multiplier."));
                }
                break;
            case 15:
                // Gold Multiplier Upgrade
                int goldLevel = GuildManager.getUpgradeLevel(guildId, "gold_multiplier");
                int goldUpgradeCost = getGoldUpgradeCost(goldLevel);
                if (GuildManager.getGuildGold(guildId) >= goldUpgradeCost) {
                    GuildManager.incrementUpgradeLevel(guildId, "gold_multiplier");
                    player.sendMessage(ColorChat.chat("&aGold Multiplier upgraded successfully!"));
                    openGuildUpgradeMenu(player);
                } else {
                    player.sendMessage(ColorChat.chat("&cNot enough gold to upgrade the Gold Multiplier."));
                }
                break;
        }
    }

    private int getExpUpgradeCost(int expLevel) {
        // calculate EXP upgrade cost based on level
        return (int) (10000 * Math.pow(1.1, expLevel));
    }

    private int getGoldUpgradeCost(int goldLevel) {
        // calculate Gold upgrade cost based on level
        return (int) (15000 * Math.pow(1.05, goldLevel));
    }
    //todo: upgrades. Quest give guild exp, Guild Contribution Records (clears every monday), Guild Managing Player GUI.
}


