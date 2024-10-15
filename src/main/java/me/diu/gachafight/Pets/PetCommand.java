package me.diu.gachafight.Pets;


import me.diu.gachafight.GachaFight;
import me.diu.gachafight.utils.ColorChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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

import java.util.ArrayList;
import java.util.List;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static net.kyori.adventure.text.Component.text;
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
        if(!sender.hasPermission("gacha.petlicense") || !sender.hasPermission("gacha.dev")){
            sender.sendMessage(ColorChat.chat("&cPurchase a pet license before you try and use your pets!"));
        }

        Player player = (Player) sender;

        switch (args[0]){
            case "help":
                player.sendMessage(ColorChat.chat("&cIncrease the number of pets you have equip at once with Pet Licenses! (3 Max)"));
                player.sendMessage(ColorChat.chat("&cHold your pet in your main hand and type /pet equip <slot number> to equip it!"));
                player.sendMessage(ColorChat.chat("&cCheck your current pets with /pet list!"));
                player.sendMessage(ColorChat.chat("&cRemove a pet with /pet remove <slot>!"));
                player.sendMessage(ColorChat.chat("&cCheck a pets buffs/de-buffs with /pet check <slot>!"));
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
            case "check":
                getCurrentPet(player, args[1]);
                break;
            case "admin":
                admin(player);
                break;
            case "create":
                createPetItem(player);
        }





        return true;

    }

    // for testing
    private void admin(Player player) {
        int currentSlots = checkPetSlots(player);
        if(currentSlots == 0){
            ArmorStand petHolder = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
            TextComponent armorStandName = text("Pet Holder[3]");
            petHolder.customName(armorStandName);
            petHolder.setVisible(true);
            petHolder.setInvulnerable(false);
            player.addPassenger(petHolder);
        }
    }

    private void removePet(Player player, String petSlotString) {
        // Check if a valid slot was specified
        int petSlot = Integer.parseInt(petSlotString);
        if(petSlot > 3 || petSlot < 0){
            player.sendMessage(ColorChat.chat("&cThat is not a valid slot!  Please choose slots 1 up to 3!"));
        }

        // Check if player has the slot unlocked
        if(petSlot > checkPetSlots(player)){
            player.sendMessage(ColorChat.chat("&cYou do not have this slot unlocked!  Purchase a Pet License to upgrade your slot count!"));
            return;
        }

        // Check if player has a free main hand
        if((player.getInventory().getItemInMainHand()) == empty()){
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
            }
        }
    }

    // Call this when a player uses a pet license.  If they are not at the max slot count, it will increase their slots each time its called
    private void addPetSlot(Player player){
        int currentSlots = checkPetSlots(player);
        // Check if all slots have been unlocked already
        if(currentSlots == 3){
            player.sendMessage(ColorChat.chat("&cYou have already increased your pet count to the max! (3)"));
            return;
        }

        // If this is the first slot bought, create the armor stand
        if(currentSlots == 0){
            ArmorStand petHolder = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
            TextComponent armorStandName = text("Pet Holder[1]");
            petHolder.customName(armorStandName);
            petHolder.setVisible(false);
            petHolder.setInvulnerable(true);
            player.addPassenger(petHolder);
        }

        // If player is adding a second or third slot, update armor stand to reflect increase
        if(currentSlots < 3){
            List<Entity> passengerList = player.getPassengers();
            for(Entity passenger : passengerList){
                if(passenger.getType() == EntityType.ARMOR_STAND && passenger.getName().contains("Pet Holder")){
                    TextComponent newStandName = text("Pet Holder[" + (currentSlots+1) + "]");
                    passenger.customName(newStandName);;
                }
            }

        }

        // Remove the stack of Pet Licenses, or decrement stack size by one
        ItemStack license = player.getInventory().getItemInMainHand();
        int itemCount = license.getAmount();
        if(itemCount == 1){
            player.getInventory().setItemInMainHand(empty());
        }
        if(itemCount > 1){
            license.setAmount(itemCount-1);
            player.getInventory().setItemInMainHand(license);
        }

    }

    // Returns the number of current pet slots a player has unlocked
    private int checkPetSlots(Player player) {
        List<Entity> passengerList = player.getPassengers();
        int petCount = 0;
        for(Entity passenger : passengerList){
            if(passenger.getType() == EntityType.ARMOR_STAND && passenger.getName().contains("Pet Holder")){
                petCount = Integer.parseInt(passenger.getName().replace("Pet Holder[","").replace("]", ""));
            }
        }
        return petCount;
    }

    // Used to set a pet into a given slot.  Should the slot be filled, the player will get the equip pet back, and equip the one in their main hand
    // Will also check to see if the slot has been unlocked
    private void setPet(Player player, String petSlotString){
        // Check if a valid slot was specified
        int petSlot = Integer.parseInt(petSlotString);
        if(petSlot > 3 || petSlot < 0){
            player.sendMessage(ColorChat.chat("&cThat is not a valid slot!  Please choose slots 1 up to 3!"));
        }

        // Check if player has the slot unlocked
        if(petSlot > checkPetSlots(player)){
            player.sendMessage(ColorChat.chat("&cYou do not have this slot unlocked!  Purchase a Pet License to upgrade your slot count!"));
            return;
        }

        // Check if player is holding a valid pet item
        if(!isPet(player.getInventory().getItemInMainHand())){
            player.sendMessage(ColorChat.chat("&cHold your pet in your main hand and type /pet to equip it!"));
            return;
        }

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
            }
        }
        // give previously equip pet back to player
        if (currentPet != null){
            player.getInventory().setItemInMainHand(currentPet);
        }
    }

    // Get information on the pet in the specified slot
    public void getCurrentPet(Player player, String petSlotString){
        // Check if a valid slot was specified
        int petSlot = Integer.parseInt(petSlotString);
        if(petSlot > 3 || petSlot < 0){
            player.sendMessage(ColorChat.chat("&cThat is not a valid slot!  Please choose slots 1 up to 3!"));
        }

        // Check if player has the slot unlocked
        if(petSlot > checkPetSlots(player)){
            player.sendMessage(ColorChat.chat("&cYou do not have this slot unlocked!  Purchase a Pet License to upgrade your slot count!"));
            return;
        }

        // Get pet ItemStack of specified slot
        ItemStack pet = null;
        List<Entity> passengerList = player.getPassengers();
        for(Entity passenger : passengerList){
            if(passenger.getType() == EntityType.ARMOR_STAND && passenger.getName().contains("Pet Holder")){
                ArmorStand armorStand = (ArmorStand) passenger;
                switch(petSlot){
                    case 1:
                        pet = armorStand.getEquipment().getHelmet();
                        break;
                    case 2:
                        pet = armorStand.getEquipment().getItemInMainHand();
                        break;
                    case 3:
                        pet = armorStand.getEquipment().getItemInOffHand();
                        break;
                }
            }
        }

        // Check if returned pet ItemStack is valid
        if(pet == null){
            player.sendMessage(ColorChat.chat("&cYou do not have a pet equip in slot" + petSlot + "!"));
            return;
        }

        // Call petToString to read off pet meta
        petToString(player, pet);
    }

    public void getCurrentPets(Player player){
        // Check if player can even equip pets
        if(checkPetSlots(player) == 0){
            player.sendMessage(ColorChat.chat("&cYou currently have no pets equip"));
            return;
        }

        // Check for armor stand holding pets, and build string for output
        List<Entity> passengerList = player.getPassengers();
        for(Entity passenger : passengerList){
            if(passenger.getType() == EntityType.ARMOR_STAND && passenger.getName().equals("Pet Holder")){
                ArmorStand armorStand = (ArmorStand) passenger;
                List<ItemStack> petList = new ArrayList<>();
                petList.add(armorStand.getEquipment().getHelmet());
                petList.add(armorStand.getEquipment().getItemInMainHand());
                petList.add(armorStand.getEquipment().getItemInOffHand());
                player.sendMessage(ColorChat.chat("&cCurrent equip pets:"));
                int total = 0;
                for(ItemStack petItem : petList){
                    if(petItem.hasItemMeta()){
                        player.sendMessage(ColorChat.chat("" +petItem.getItemMeta().getDisplayName()));
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
    public void petToString(Player player, ItemStack pet){
        ItemMeta petMeta = pet.getItemMeta();
        String petName = petMeta.displayName().toString();
        player.sendMessage(ColorChat.chat("&cPet Name:" + petName));
        player.sendMessage(ColorChat.chat("&cPet Lore:"));

        for(String meta: petMeta.getLore()){
            player.sendMessage(ColorChat.chat("&c" + meta + ""));
        }
    }





}

