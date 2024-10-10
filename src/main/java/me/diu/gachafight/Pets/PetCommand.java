package me.diu.gachafight.Pets;


import me.diu.gachafight.GachaFight;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PetCommand implements CommandExecutor, Listener {
    private final GachaFight plugin;
    public PetCommand(GachaFight plugin){
        this.plugin = plugin;
        plugin.getCommand("pet").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        // make the pet a passenger item display to the player so we dont need to update it
        ItemStack pet = player.getActiveItem();



        return false;
    }

    public void setPet(Player player){
        // check if there is currently a pet attached to the player
        String currentPet = null;
        if(getCurrentPet(player) != null){
            currentPet = getCurrentPet(player);
        }
        // get the players held pets ID and effects from lore
        ItemStack heldPet = player.getInventory().getItemInMainHand();
        ItemMeta meta = heldPet.getItemMeta();
        List<Component> lore = meta.lore();

        String petID = PlainTextComponentSerializer.plainText().serialize(meta.displayName());


        for(Component loreStr : lore){
            String loreReadable = PlainTextComponentSerializer.plainText().serialize(loreStr);
            String effectName = null;
            double effectMod;

            if(!loreReadable.contains("Pet")){
                effectName = loreReadable.split( " ")[0];
                //effectMod = loreReadable.split(" ")[1];
            }

        }

        NamespacedKey namespacedKey = new NamespacedKey(this.plugin, "pet");
        PersistentDataContainer dataContainer = player.getPersistentDataContainer();
        dataContainer.set(namespacedKey, PersistentDataType.STRING, petID);


        // give previously equip pet back to player
        if (currentPet != null){
            ItemStack newPetItem = createPetItem(currentPet);
            player.getInventory().setItemInMainHand(newPetItem);
        }
    }

    public static String getCurrentPet(Player player){
        NamespacedKey namespacedKey = new NamespacedKey(this.plugin, "pet");
        PersistentDataContainer dataContainer = player.getPersistentDataContainer();
        if (dataContainer.has(namespacedKey, PersistentDataType.STRING)) {
            return dataContainer.get(namespacedKey, PersistentDataType.STRING);
        }
        // Default return value if no data is found
        return null;
    }

    public ItemStack createPetItem(String petID){
        //Create pet item via string.  will need a list of models and shit, diu gimme plz
        switch(petID) {
            case "1":
                //do lore?
                break;
            case "2":
                // do lore?
                int a = 1+1;
                break;
        }
        // Default return value if no valid pet is found
        return null;
    }





}
