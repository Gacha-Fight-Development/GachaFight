package me.diu.gachafight.Pets;


import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.utils.ColorChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static org.bukkit.inventory.ItemStack.empty;

public class PetCommand implements CommandExecutor, Listener {
    private final GachaFight plugin;

    public PetCommand(GachaFight plugin){
        this.plugin = plugin;
        plugin.getCommand("pet").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        new PetEffects(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has correct permissions
        if(!sender.hasPermission("gacha.petlicense")){
            sender.sendMessage(ColorChat.chat("&cPurchase a pet license before you try and use your pets!"));
        }

        Player player = (Player) sender;

        switch (args[0]){
            case "help":
                player.sendMessage(ColorChat.chat("&cHold your pet in your main hand and type /pet equip <slot number> to equip it!"));
                player.sendMessage(ColorChat.chat("&cCheck your current pets and their stats with /pet list!"));
                player.sendMessage(ColorChat.chat("&cRemove a pet with /pet remove <slot>!"));
                break;
            case "equip":
                setPet(player, args[1]);
                break;
            case "list":
                getCurrentPets(player);
                break;
            case "remove":
                removePet(player, args[1]);
                break;
            case "create":
                if(sender.hasPermission("gacha.dev")) {
                    createPetItem(player);
                }
        }

        return true;
    }

    private void removePet(Player player, String petSlotString) {
        // Check if a valid slot was specified
        int petSlot = Integer.parseInt(petSlotString);
        if(petSlot > 3 || petSlot < 0){
            player.sendMessage(ColorChat.chat("&cThat is not a valid slot!  Please choose a slot 1 to 3!"));
        }

        // Check if player has a free main hand
        if(!((player.getInventory().getItemInMainHand()) == empty())){
            player.sendMessage(ColorChat.chat("&cEmpty your main hand and retype /pet remove " + petSlot + " to remove it!"));
            return;
        }

        // Get passenger armor stand and remove pet
        List<Entity> passengerList = player.getPassengers();
        ItemStack removedPet = null;
        for(Entity passenger : passengerList){
            if(passenger.getType() == EntityType.ARMOR_STAND && passenger.getName().contains("Pet Holder")){
                ArmorStand armorStand = (ArmorStand) passenger;
                switch(petSlot){
                    case 1:
                        removedPet = armorStand.getEquipment().getHelmet();
                        armorStand.setItem(EquipmentSlot.HEAD, null);
                        break;
                    case 2:
                        removedPet = armorStand.getEquipment().getItemInMainHand();
                        armorStand.setItem(EquipmentSlot.HAND, null);
                        break;
                    case 3:
                        removedPet = armorStand.getEquipment().getItemInOffHand();
                        armorStand.setItem(EquipmentSlot.OFF_HAND, null);
                        break;
                }
                player.sendMessage(ColorChat.chat("&cYour " + removedPet.getItemMeta().getDisplayName() + " &chas been un-equip!"));
                player.getInventory().setItemInMainHand(removedPet);
                // Remove stats from players stats given by old pet
                updatePlayer(player, removedPet,-1);
            }
        }
    }

    // Call this to create the pet armor stand for the player.  will not trigger if player has it already
    private void addPetStand(Player player){
        List<Entity> passengerList = player.getPassengers();
        for(Entity passenger : passengerList) {
            if (passenger.getType() == EntityType.ARMOR_STAND && passenger.getName().contains("Pet Holder")) {
                return;
            }
        }
        ArmorStand petHolder = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        TextComponent armorStandName = text("Pet Holder");
        petHolder.customName(armorStandName);
        petHolder.setVisible(false);
        petHolder.setInvulnerable(true);
        player.addPassenger(petHolder);
    }



    // Used to set a pet into a given slot.  Should the slot be filled, the player will get the equip pet back, and equip the one in their main hand
    private void setPet(Player player, String petSlotString){
        // Check if a valid slot was specified
        int petSlot = Integer.parseInt(petSlotString);
        if(petSlot > 3 || petSlot < 0){
            player.sendMessage(ColorChat.chat("&cThat is not a valid slot!  Please choose slots 1 up to 3!"));
        }

        // Check if player is holding a valid pet item
        if(!isPet(player.getInventory().getItemInMainHand())){
            player.sendMessage(ColorChat.chat("&cHold your pet in your main hand and type /pet to equip it!"));
            return;
        }

        // create the pet armor stand if needed
        addPetStand(player);

        // get the players held pet, and return the one in the slot if found
        ItemStack currentPet = null;
        ItemStack heldPet = player.getInventory().getItemInMainHand();
        List<Entity> passengerList = player.getPassengers();
        for(Entity passenger : passengerList){
            if(passenger.getType() == EntityType.ARMOR_STAND && passenger.getName().contains("Pet Holder")){
                ArmorStand armorStand = (ArmorStand) passenger;
                switch(petSlot){
                    case 1:
                        currentPet = armorStand.getEquipment().getHelmet();
                        armorStand.setItem(EquipmentSlot.HEAD, heldPet);
                        break;
                    case 2:
                        currentPet = armorStand.getEquipment().getItemInMainHand();
                        armorStand.setItem(EquipmentSlot.HAND, heldPet);
                        break;
                    case 3:
                        currentPet = armorStand.getEquipment().getItemInOffHand();
                        armorStand.setItem(EquipmentSlot.OFF_HAND, heldPet);
                        break;
                }
                player.sendMessage(ColorChat.chat("&cYour " + heldPet.getItemMeta().getDisplayName() + " &chas been equip!"));
                // Remove stats from players stats given by old pet if there was one
                if(!(currentPet.isEmpty())) {
                    updatePlayer(player, currentPet, -1);
                }
                // Add new stats to player stats from newly equip pet
                updatePlayer(player, heldPet,1);
            }
        }
        // give previously equip pet back to player
        if (currentPet != null){
            player.getInventory().setItemInMainHand(currentPet);
        }
    }

    // Get information on the pet in the specified slot


    public void getCurrentPets(Player player){
                // Check for armor stand holding pets, and build string for output
        List<Entity> passengerList = player.getPassengers();
        for(Entity passenger : passengerList){
            if(passenger.getType() == EntityType.ARMOR_STAND && passenger.getName().contains("Pet Holder")){
                ArmorStand armorStand = (ArmorStand) passenger;
                List<ItemStack> petList = new ArrayList<>();
                petList.add(armorStand.getEquipment().getHelmet());
                petList.add(armorStand.getEquipment().getItemInMainHand());
                petList.add(armorStand.getEquipment().getItemInOffHand());
                int total = 0;
                for(ItemStack petItem : petList){
                    if(petItem.hasItemMeta()){
                        petToString(player, petItem);
                        total += 1;
                    }
                }

                if((total == 0)) {
                    player.sendMessage(ColorChat.chat("&cYou currently have no pets equip"));
                }
            }
        }
    }

    public void createPetItem(Player player){
        ItemStack heldPet = player.getInventory().getItemInMainHand();
        ItemStack newPetItem = PetEffects.createPetStats(heldPet);
        player.getInventory().setItemInMainHand(newPetItem);
    }

    public boolean isPet(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        List<Component> lore = meta.lore();
        // Check if the held item has lore, and return if the first line of lore contains the word 'pet'
        if(!(lore.isEmpty())){
            return lore.getFirst().toString().toLowerCase().contains("pet");
        }
        // Default return value if no valid pet is found
        return false;
    }

    // ToString method to pass pet information to player
    // unused as of right now
    public void petToString(Player player, ItemStack pet){
        String petName = pet.getItemMeta().getDisplayName();
        @NotNull HoverEvent<HoverEvent.ShowItem> itemHover = pet.asHoverEvent();
        Component message = Component.text(petName).color(LIGHT_PURPLE).hoverEvent(itemHover);
        player.sendMessage(message);
    }

    // Call this method when adding or removing a pet.  int mod should be +1 when adding a pet, and -1 when removing
    public static void updatePlayer(Player player, ItemStack petItem, int mod){
        PlayerStats stats = PlayerStats.getPlayerStats(player.getUniqueId());
        ItemMeta meta = petItem.getItemMeta();
        for (Component lore : meta.lore()) {
            if (!(lore.toString().toLowerCase().contains("pet"))) {
                String[] stat = (PlainTextComponentSerializer.plainText().serialize(lore)).split(" ");
                switch (stat[1]){
                    case "Damage:":
                        stats.setDamage(stats.getDamage() + mod*Double.parseDouble(stat[stat.length-1]));
                        break;
                    case "Armor:":
                        stats.setArmor(stats.getArmor() + mod*Double.parseDouble(stat[stat.length-1]));
                        break;
                    case "HP:":
                        stats.setMaxhp(stats.getMaxhp() + mod*Double.parseDouble(stat[stat.length-1]));
                        break;
                    case "Speed:":
                        stats.setSpeed(stats.getSpeed() + mod*Double.parseDouble(stat[stat.length-1]));
                        break;
                    case "Luck:":
                        stats.setLuck(stats.getLuck() + mod*Double.parseDouble(stat[stat.length-1]));
                        break;
                    case "Crit Chance:":
                        stats.setCritChance(stats.getCritChance() + mod*Double.parseDouble(stat[stat.length-1]));
                        break;
                    case "Crit Dmg:":
                        stats.setCritDmg(stats.getCritDmg() + mod*Double.parseDouble(stat[stat.length-1]));
                        break;
                    case "Dodge:":
                        stats.setDodge(stats.getDodge() + mod*Double.parseDouble(stat[stat.length-1]));
                        break;
                }
            }
        }
    }


    // unused as of right now
    public static List<String> getPetLore(Player player, int slot){
        // Check for armor stand holding pets, and build string for output
        List<Entity> passengerList = player.getPassengers();
        List<String> petLore = new ArrayList<>();
        for(Entity passenger : passengerList){
            if(passenger.getType() == EntityType.ARMOR_STAND && passenger.getName().contains("Pet Holder")){
                ArmorStand armorStand = (ArmorStand) passenger;
                switch (slot) {
                    case 1:
                        if (!(armorStand.getEquipment().getHelmet().isEmpty())) {
                            for (Component lore : armorStand.getEquipment().getHelmet().getItemMeta().lore()) {
                                if (!(lore.toString().toLowerCase().contains("pet"))) {
                                    petLore.add(PlainTextComponentSerializer.plainText().serialize(lore));
                                }
                            }
                        }
                        break;
                    case 2:
                        if (!(armorStand.getEquipment().getItemInMainHand().isEmpty())) {
                            for (Component lore : armorStand.getEquipment().getItemInMainHand().getItemMeta().lore()) {
                                if (!(lore.toString().toLowerCase().contains("pet"))) {
                                    petLore.add(PlainTextComponentSerializer.plainText().serialize(lore));
                                }
                            }
                        }
                        break;
                    case 3:
                        if (!(armorStand.getEquipment().getItemInOffHand().isEmpty())) {
                            for (Component lore : armorStand.getEquipment().getItemInOffHand().getItemMeta().lore()) {
                                if (!(lore.toString().toLowerCase().contains("pet"))) {
                                    petLore.add(PlainTextComponentSerializer.plainText().serialize(lore));
                                }
                            }
                        }
                        break;
                }
            }
        }
        return petLore;
    }





}

