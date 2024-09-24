package me.diu.gachafight.commands;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.ColorChat;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.SuffixNode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagsCommand implements CommandExecutor, Listener {

    private final GachaFight plugin;
    private FileConfiguration tagsConfig;

    public TagsCommand(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getCommand("tags").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        loadTagsConfig(); // Load tags from the config file
    }

    // Load tags.yml configuration
    private void loadTagsConfig() {
        File tagsFile = new File(plugin.getDataFolder(), "tags.yml");
        if (!tagsFile.exists()) {
            plugin.saveResource("tags.yml", false);
        }
        tagsConfig = YamlConfiguration.loadConfiguration(tagsFile);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            openTagsGUI(player);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Select a Suffix Tag")) {
            event.setCancelled(true);  // Prevent item movement in the GUI

            ItemStack clickedItem = event.getCurrentItem();
            Player player = (Player) event.getWhoClicked();

            if (clickedItem != null && clickedItem.getType() == Material.NAME_TAG) {
                ItemMeta meta = clickedItem.getItemMeta();
                if (meta != null) {
                    String tag = meta.getDisplayName();
                    setPlayerSuffix(player, tag);  // Set the player's suffix based on the tag
                    player.sendMessage("§aYour suffix has been set to: " + tag);
                }
            }
        }
    }

    // Method to set the player's suffix using LuckPerms
    private void setPlayerSuffix(Player player, String suffix) {
        User user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());

        if (user == null) {
            player.sendMessage("§cError: Could not apply suffix. LuckPerms user not found.");
            return;
        }
        user.data().clear(node -> node instanceof SuffixNode);

        // Set new suffix
        SuffixNode suffixNode = SuffixNode.builder(suffix, 1).build();
        user.data().add(suffixNode);

        plugin.getLuckPerms().getUserManager().saveUser(user);
    }

    // Open the tags GUI for the player
    private void openTagsGUI(Player player) {
        List<Map<?, ?>> tagsList = tagsConfig.getMapList("tags");

        Inventory tagGUI = Bukkit.createInventory(null, 9, "Select a Suffix Tag");

        for (Map<?, ?> tagData : tagsList) {
            String tagid = (String) tagData.get("tagid");
            String suffix = (String) tagData.get("suffix");
            String permission = (String) tagData.get("permission");

            // Check if the player has permission for the tag
            if (player.hasPermission(permission)) {
                ItemStack tagItem = new ItemStack(Material.NAME_TAG);  // Icon for each tag
                ItemMeta meta = tagItem.getItemMeta();
                suffix = parseHexColorCodes(suffix);
                meta.setDisplayName(suffix);  // Display the suffix as the item name
                tagItem.setItemMeta(meta);
                tagGUI.addItem(tagItem);  // Add item to GUI
            }
        }

        player.openInventory(tagGUI);  // Open the GUI for the player
    }
    // Method to parse both legacy & hex color codes
    private String parseHexColorCodes(String input) {
        // Create a pattern to match hex color codes in the format &#RRGGBB
        Pattern pattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        // Iterate through all the matches
        while (matcher.find()) {
            String hex = matcher.group(1);  // Get the hex color code (RRGGBB)
            // Replace it with Minecraft's hex format §x§R§R§G§G§B§B
            String replacement = "§x§" + hex.charAt(0) + "§" + hex.charAt(1) + "§" + hex.charAt(2) +
                    "§" + hex.charAt(3) + "§" + hex.charAt(4) + "§" + hex.charAt(5);
            matcher.appendReplacement(result, replacement);  // Append the replacement to the result
        }

        matcher.appendTail(result);  // Append the rest of the string
        return result.toString().replace("&", "§");  // Replace legacy color codes & with §
    }

}
