package me.diu.gachafight.shop.buy;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.shop.buy.listener.BuyShopEditorListener;
import me.diu.gachafight.shop.buy.listener.BuyShopListener;
import me.diu.gachafight.shop.buy.listener.BuyShopSelectionListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BuyItemManager {
    private final GachaFight plugin;
    private final List<ItemStack> goldShopItems;  // Gold items
    private final List<ItemStack> gemShopItems;   // Gem items
    private final File buyShopFile;
    private FileConfiguration buyShopConfig;

    public BuyItemManager(GachaFight plugin) {
        this.plugin = plugin;
        this.goldShopItems = new ArrayList<>();
        this.gemShopItems = new ArrayList<>();
        this.buyShopFile = new File(plugin.getDataFolder(), "buy_shop.yml");
        new BuyShopEditorListener(plugin);
        new BuyShopSelectionListener(plugin);
        new BuyShopListener(plugin);

        // Register ItemStack for serialization
        ConfigurationSerialization.registerClass(ItemStack.class);

        // Create data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Load the shop items from the file
        loadShopItems();
    }

    // Add items to each shop
    public void addItemToGoldShop(ItemStack item) {
        goldShopItems.add(item);
        saveShopItems();
    }

    public void addItemToGemShop(ItemStack item) {
        gemShopItems.add(item);
        saveShopItems();
    }

    // Remove items from the shop by slot
    public void removeItemFromGoldShop(int slot) {
        if (slot >= 0 && slot < goldShopItems.size()) {
            goldShopItems.remove(slot);
            saveShopItems();
        }
    }

    public void removeItemFromGemShop(int slot) {
        if (slot >= 0 && slot < gemShopItems.size()) {
            gemShopItems.remove(slot);
            saveShopItems();
        }
    }

    // Load and save methods (modify as needed for your YAML structure)
    private void saveShopItems() {
        if (buyShopConfig == null) {
            buyShopConfig = YamlConfiguration.loadConfiguration(buyShopFile);
        }

        buyShopConfig.set("goldShopItems", goldShopItems);
        buyShopConfig.set("gemShopItems", gemShopItems);

        try {
            buyShopConfig.save(buyShopFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save buy shop data!");
            e.printStackTrace();
        }
    }

    private void loadShopItems() {
        if (!buyShopFile.exists()) {
            buyShopConfig = YamlConfiguration.loadConfiguration(buyShopFile);
            return;
        }

        buyShopConfig = YamlConfiguration.loadConfiguration(buyShopFile);

        List<ItemStack> loadedGoldItems = (List<ItemStack>) buyShopConfig.getList("goldShopItems", new ArrayList<>());
        List<ItemStack> loadedGemItems = (List<ItemStack>) buyShopConfig.getList("gemShopItems", new ArrayList<>());

        goldShopItems.addAll(loadedGoldItems);
        gemShopItems.addAll(loadedGemItems);
    }
}
