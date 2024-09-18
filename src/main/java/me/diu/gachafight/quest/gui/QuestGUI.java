package me.diu.gachafight.quest.gui;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.quest.Quest;
import me.diu.gachafight.quest.QuestManager;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QuestGUI {
    private final QuestManager questManager;

    public QuestGUI(QuestManager questManager) {
        this.questManager = questManager;
    }

    public static void openQuestSelection(Player player) {
        Inventory questInventory = Bukkit.createInventory(null, 9, "Select a Quest");
        // Load player's quests from the database
        List<Quest> playerQuests = GachaFight.getInstance().getQuestManager().getPlayerQuests(player);
        playerQuests.removeIf(quest -> GachaFight.getInstance().getQuestManager().hasCompletedQuest(player, quest.getId()));

        // Create an array to track if a slot is filled by a quest
        boolean[] filledSlots = new boolean[9];
        // Place player's quests in their respective slots
        for (Quest quest : playerQuests) {
            int slot = quest.getSlot(); // Get the slot from the quest
            if (slot == -1) {
            } else {
                ItemStack questItem = createQuestItem(player, quest);
                questInventory.setItem(slot, questItem);
                filledSlots[slot] = true;
            }
        }

        // Populate the remaining slots with placeholders if they are not already filled
        for (int i = 0; i < 8; i++) { // Slots 0-7 for quests
            if (!filledSlots[i]) {
                if (i == 0) {
                    questInventory.setItem(i, createDailyQuestItem(player, GachaFight.getInstance().getQuestManager().getPlayerQuests(player)));
                } else {
                    questInventory.setItem(i, createPlaceholderQuestItem(i + 1));
                }
            }
        }

        // Set the compass in slot 8
        questInventory.setItem(8, createQuestExplanationCompass());

        player.openInventory(questInventory);
    }

    private static ItemStack createQuestItem(Player player, Quest quest) {
        ItemStack questItem = new ItemStack(Material.BOOK); // Default to book
        ItemMeta meta = questItem.getItemMeta();

        // Set the display name to the quest's name with color
        meta.setDisplayName(ColorChat.chat("&a" + quest.getName()));

        // Show progress and time left in the lore
        List<String> lore = new ArrayList<>();
        int progress = GachaFight.getInstance().getQuestManager().loadQuestProgress(player, quest.getId());

        // Get the required amount from the Quest's objective
        int requiredAmount = quest.getObjective().getRequiredAmount();
        lore.add(ColorChat.chat("&6Progress: &e" + progress + "/" + requiredAmount));
        // Add rewards to the lore
        lore.add(ColorChat.chat("&6Rewards: &e1 Gem"));
        // Calculate the time left for the quest
        String timeLeft = calculateTimeLeft(player, quest);
        lore.add(ColorChat.chat("&6Time Left: &e" + timeLeft));

        meta.setLore(lore);

        questItem.setItemMeta(meta);
        return questItem;
    }


    private static String calculateTimeLeft(Player player, Quest quest) {
        // Load the quest start time from the database
        Instant startTime = GachaFight.getInstance().getQuestManager().loadQuestStartTime(player, quest.getId()).toInstant();

        // Define the quest duration (e.g., 24 hours for daily quests)
        Duration questDuration = Duration.ofHours(24);
        Instant endTime = startTime.plus(questDuration);

        // Calculate the remaining time
        Duration timeLeft = Duration.between(Instant.now(), endTime);

        // Convert to hours and minutes for display
        long hoursLeft = timeLeft.toHours();
        long minutesLeft = timeLeft.toMinutes() % 60;

        // Return formatted time left
        return hoursLeft + "h " + minutesLeft + "m";
    }

    private static ItemStack createDailyQuestItem(Player player, List<Quest> questList) {
        ItemStack dailyQuestItem = new ItemStack(Material.PAPER);
        ItemMeta meta = dailyQuestItem.getItemMeta();

        meta.setDisplayName(ColorChat.chat("&bDaily Quest"));

        List<String> lore = new ArrayList<>();
        lore.add(ColorChat.chat("&7A new quest every day!"));
        for (Quest quest : questList) {
            int slot = quest.getSlot(); // Get the slot from the quest
            if (slot == -1) {
                String timeLeft = calculateTimeLeft(player, quest);
                lore.add(ColorChat.chat("&7Cooldown: &e" + timeLeft));
            } else {
            }
        }
        meta.setLore(lore);

        dailyQuestItem.setItemMeta(meta);
        return dailyQuestItem;
    }

    private static ItemStack createPlaceholderQuestItem(int questNumber) {
        ItemStack placeholderQuestItem = new ItemStack(Material.PAPER);
        ItemMeta meta = placeholderQuestItem.getItemMeta();

        meta.setDisplayName(ColorChat.chat("&cQuest " + questNumber));

        List<String> lore = new ArrayList<>();
        lore.add(ColorChat.chat("&7Complete this quest to earn rewards."));
        meta.setLore(lore);

        placeholderQuestItem.setItemMeta(meta);
        return placeholderQuestItem;
    }

    private static ItemStack createQuestExplanationCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        meta.setDisplayName(ColorChat.chat("&dQuest System Guide"));

        List<String> lore = new ArrayList<>();
        lore.add(ColorChat.chat("&61. Select a quest to start."));
        lore.add(ColorChat.chat("&62. Complete the objectives to earn rewards."));
        lore.add(ColorChat.chat("&63. Check back daily for new quests!"));
        meta.setLore(lore);

        compass.setItemMeta(meta);
        return compass;
    }

    public static void updateQuestItem(Player player, Quest quest, int slot) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        if (inventory == null) {
            Bukkit.getLogger().severe("Inventory is null when trying to update quest item.");
            return;
        }

        // Log the inventory size and slot to be updated
        Bukkit.getLogger().info("Updating quest item in slot: " + slot + " of inventory size: " + inventory.getSize());

        // Ensure the slot is within inventory bounds
        if (slot >= 0 && slot < inventory.getSize()) {
            // Create a book item to represent the quest
            ItemStack questItem = createQuestItem(player, quest);

            // Update the specific slot with the new quest item
            inventory.setItem(slot, questItem);
        } else {
            Bukkit.getLogger().warning("Attempted to update inventory slot out of bounds: " + slot);
        }
    }

    private void createCustomQuest(Player player, int questId) {
        // Check if the player has already completed this custom quest
        if (questManager.hasCompletedQuest(player, questId)) {
            player.sendMessage("§cYou have already completed this quest and cannot accept it again.");
            return;
        }

        // Create and assign the custom quest
        Quest customQuest = questManager.getQuestFactory().createQuest(questId, player);
        questManager.assignQuest(player, customQuest, customQuest.getSlot()); // No slot assignment needed for this logic
        player.sendMessage("§aYou have accepted a custom quest: " + customQuest.getName());

        // Update the inventory to show the new quest (if needed in the GUI)
        QuestGUI.updateQuestItem(player, customQuest, customQuest.getSlot()); // Assuming event.getSlot() is the slot clicked
    }


}
