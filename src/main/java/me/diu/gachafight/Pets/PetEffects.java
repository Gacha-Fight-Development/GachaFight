package me.diu.gachafight.Pets;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.ColorChat;
import me.diu.gachafight.utils.Prefix;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class PetEffects {


    private GachaFight plugin;
    private static FileConfiguration config;




    public PetEffects(GachaFight plugin) {
        this.plugin = plugin;
        updateConfigFiles();
        loadConfig();
    }

    private void updateConfigFiles() {
        plugin.saveResource("Pets/petstats.yml", true);
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "Pets/petstats.yml");
        if (!configFile.exists()) {
            plugin.saveResource("Pets/petstats.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    // This is where pet effects are calculated based on already created pets that a player has equip.
    public double checkPetEffect(Player player, String action){






        // Default return value if no data is found
        return 0.0;
    }

    public static ItemStack createPetStats(ItemStack petItem){
        ItemStack petItemModified = petItem;
        ItemMeta meta = petItem.getItemMeta();
        String petName = ChatColor.stripColor(petItem.getItemMeta().getDisplayName().toLowerCase());

        switch (petName){
            case "turtle" :
                List<Component> lore = new ArrayList<>();
                String petLore = config.getString("lore");
                int minHp = config.getInt("turtle.minHp");
                int maxHp = config.getInt("turtle.maxHp");
                int minDef = config.getInt("turtle.minDefence");
                int maxDef = config.getInt("turtle.maxDefence");

                int hpFinal = randInt(minHp, maxHp);
                int defenceFinal = randInt(minDef, maxDef);

                lore.add(MiniMessage.miniMessage().deserialize("<!i><light-purple>" + petLore));
                lore.add(MiniMessage.miniMessage().deserialize(Prefix.getHealthPrefix() + hpFinal));
                lore.add(MiniMessage.miniMessage().deserialize(Prefix.getArmorPrefix() + defenceFinal));
                meta.lore(lore);
                petItemModified.setItemMeta(meta);





        }


        return petItemModified;
    }

    public static int randInt(int min, int max){
        return min + (int)(Math.random() * ((max - min) + 1));
    }
}
