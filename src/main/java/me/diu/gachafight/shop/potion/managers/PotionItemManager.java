package me.diu.gachafight.shop.potion.managers;

import me.diu.gachafight.GachaFight;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PotionItemManager {
    private final GachaFight plugin;
    private final Map<Integer, List<ItemStack>> potions;
    private final File potionFile;
    private FileConfiguration potionConfig;

    public PotionItemManager(GachaFight plugin) {
        this.plugin = plugin;
        this.potions = new HashMap<>();
        this.potionFile = new File(plugin.getDataFolder(), "potion_shop.yml");

        // Register ItemStack for serialization
        ConfigurationSerialization.registerClass(ItemStack.class);

        // Create the directory if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Initialize empty lists for each rarity
        for (int i = 0; i < 7; i++) {
            potions.put(i, new ArrayList<>());
        }

        // Load the loot tables from the file
        loadLootTables();
    }

    public List<ItemStack> getLootTable(int rarityIndex) {
        return potions.get(rarityIndex);
    }

    public void addItemToLootTable(int rarityIndex, ItemStack item) {
        List<ItemStack> lootTable = potions.get(rarityIndex);
        lootTable.add(item);
        saveLootTables(); // Save the loot table after adding an item
    }

    public void removeItemFromLootTable(int rarityIndex, int slot) {
        List<ItemStack> lootTable = potions.get(rarityIndex);
        if (slot >= 0 && slot < lootTable.size()) {
            lootTable.remove(slot);
            saveLootTables(); // Save the loot table after removing an item
        }
    }

    private void saveLootTables() {
        // Ensure potionConfig is initialized before using it
        if (potionConfig == null) {
            potionConfig = YamlConfiguration.loadConfiguration(potionFile);
        }

        for (int rarityIndex : potions.keySet()) {
            List<ItemStack> lootTable = potions.get(rarityIndex);
            potionConfig.set("rarity" + rarityIndex, lootTable);
        }

        try {
            potionConfig.save(potionFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save loot table data!");
            e.printStackTrace();
        }
    }

    private void loadLootTables() {
        // Initialize potionConfig even if the file doesn't exist
        if (!potionFile.exists()) {
            potionConfig = YamlConfiguration.loadConfiguration(potionFile);
            return;
        }

        potionConfig = YamlConfiguration.loadConfiguration(potionFile);

        for (int i = 0; i < 7; i++) {
            List<ItemStack> lootTable = (List<ItemStack>) potionConfig.getList("rarity" + i, new ArrayList<>());
            potions.put(i, lootTable);
        }
    }
}
