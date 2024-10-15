package me.diu.gachafight.Pets;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.Prefix;
import net.kyori.adventure.text.Component;
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

    // Call this when you equip a pet.
    public void checkPetEffect(Player player, ItemStack petItem){
        

    }

    public static ItemStack createPetStats(ItemStack petItem){
        ItemStack petItemModified = petItem;
        ItemMeta meta = petItem.getItemMeta();
        String petName = ChatColor.stripColor(petItem.getItemMeta().getDisplayName().toLowerCase());
        List<Component> lore = new ArrayList<>();

        switch (petName){
            case "turtle" :
                addLore(lore, petName,"Lore");
                addLore(lore, petName, "HP");
                addLore(lore, petName, "Defence");
                break;
            case "octopus" :
                addLore(lore, petName,"Lore");
                addLore(lore, petName, "HP");
                addLore(lore, petName, "Damage");
                break;
            case "snowy owl" :
                petName = "snowy_owl";
                addLore(lore, petName,"Lore");
                addLore(lore, petName, "Damage");
                addLore(lore, petName, "Armor");
                break;






        }
        meta.lore(lore);
        petItemModified.setItemMeta(meta);



        return petItemModified;
    }

    // this method cleans up the pet creation method.  It will allow for pets to be added easier instead of copying the long line over and over.
    // example, you want to add the HP stat to a pet, call addLore(lore, petName, HP) and the lore for the hp of the pet will be calculated and added
    public static void addLore(List<Component> lore, String petName, String type){
        switch (type){
            case "Lore":
                lore.add(MiniMessage.miniMessage().deserialize("<!i><light_purple>" + config.getString(petName +".lore")));
                break;
            case "Damage":
                lore.add(MiniMessage.miniMessage().deserialize(Prefix.getDamagePrefix() + randInt(config.getInt(petName +".minDamage"), config.getInt(petName +".maxDamage"))));
                break;
            case "Armor":
                lore.add(MiniMessage.miniMessage().deserialize(Prefix.getArmorPrefix() + randInt(config.getInt(petName +".minDefence"), config.getInt(petName +".maxDefence"))));
                break;
            case "HP":
                lore.add(MiniMessage.miniMessage().deserialize(Prefix.getHealthPrefix() + randInt(config.getInt(petName +".minHP"), config.getInt(petName +".maxHP"))));
                break;
            case "Speed":
                lore.add(MiniMessage.miniMessage().deserialize(Prefix.getSpeedPrefix() + randInt(config.getInt(petName +".minSpeed"), config.getInt(petName +".maxSpeed"))));
                break;
            case "Luck":
                lore.add(MiniMessage.miniMessage().deserialize(Prefix.getSpeedPrefix() + randInt(config.getInt(petName +".minSpeed"), config.getInt(petName +".maxSpeed"))));
                break;
            case "Crit Chance":
                lore.add(MiniMessage.miniMessage().deserialize(Prefix.getCritChancePrefix() + randInt(config.getInt(petName +".minCritChance"), config.getInt(petName +".maxCritChance"))));
                break;
            case "Crit Damage":
                lore.add(MiniMessage.miniMessage().deserialize(Prefix.getCritDmgPrefix() + randInt(config.getInt(petName +".minCritDamage"), config.getInt(petName +".maxCritDamage"))));
                break;
            case "Dodge":
                lore.add(MiniMessage.miniMessage().deserialize(Prefix.getDodgePrefix() + randInt(config.getInt(petName +".minDodge"), config.getInt(petName +".maxDodge"))));
                break;
        }


    }

    public static int randInt(int min, int max){
        return min + (int)(Math.random() * ((max - min) + 1));
    }
}
