package me.diu.gachafight.gacha.managers;

import me.diu.gachafight.GachaFight;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GachaLootTableManager {

    private final GachaFight plugin;
    public static Map<Integer, List<ItemStack>> lootTables = new HashMap<>();
    private final File lootTableFile;
    private FileConfiguration lootTableConfig;

    public GachaLootTableManager(GachaFight plugin) {
        this.plugin = plugin;
        this.lootTableFile = new File(plugin.getDataFolder(), "loottables.yml");

        // Create the directory if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Initialize empty lists for each rarity
        for (int i = 0; i < 8; i++) {
            lootTables.put(i, new ArrayList<>());
        }

        // Load the loot tables from the file
        loadLootTables();
    }

    public static List<ItemStack> getLootTable(int rarityIndex) {
        return lootTables.get(rarityIndex);
    }

    public void addItemToLootTable(int rarityIndex, ItemStack item) {
        List<ItemStack> lootTable = lootTables.get(rarityIndex);
        lootTable.add(item);
        saveLootTables(); // Save the loot table after adding an item
    }

    public void removeItemFromLootTable(int rarityIndex, int slot) {
        List<ItemStack> lootTable = lootTables.get(rarityIndex);
        if (slot >= 0 && slot < lootTable.size()) {
            lootTable.remove(slot);
            saveLootTables(); // Save the loot table after removing an item
        }
    }

    public static ItemStack getRandomItem(int rarityIndex) {
        List<ItemStack> lootTable = lootTables.get(rarityIndex);
        if (lootTable.isEmpty()) return null;
        return lootTable.get((int) (Math.random() * lootTable.size()));
    }

    private void saveLootTables() {
        lootTableConfig = YamlConfiguration.loadConfiguration(lootTableFile);

        for (int rarityIndex : lootTables.keySet()) {
            List<ItemStack> lootTable = lootTables.get(rarityIndex);
            lootTableConfig.set("rarity" + rarityIndex, lootTable);
        }

        try {
            lootTableConfig.save(lootTableFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLootTables() {
        if (!lootTableFile.exists()) return;

        lootTableConfig = YamlConfiguration.loadConfiguration(lootTableFile);

        for (int i = 0; i < 8; i++) {
            List<ItemStack> lootTable = (List<ItemStack>) lootTableConfig.getList("rarity" + i, new ArrayList<>());
            lootTables.put(i, lootTable);
        }
    }
}
