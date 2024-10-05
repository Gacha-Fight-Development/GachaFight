package me.diu.gachafight.quest.gui;

import me.diu.gachafight.quest.Quest;
import me.diu.gachafight.quest.managers.DailyQuestManager;
import me.diu.gachafight.quest.managers.QuestManager;
import me.diu.gachafight.quest.managers.SideQuestManager;
import me.diu.gachafight.quest.utils.QuestUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class QuestGUI {

    private final QuestManager questManager; // Assuming this is passed from your plugin to manage quests

    public QuestGUI(QuestManager questManager) {
        this.questManager = questManager;
    }

    // Method to create and open the quest GUI for the player
    public void openQuestGUI(Player player) {
        // Create a 3-row (27-slot) inventory with the title "Quest Selection"
        Inventory questInventory = Bukkit.createInventory(null, 27, "Quest Selection");

        // Adding black stained glass pane as the border
        ItemStack cyanPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta cyanPaneMeta = cyanPane.getItemMeta();
        cyanPaneMeta.setHideTooltip(true);
        cyanPane.setItemMeta(cyanPaneMeta);

        // Set the layout for the GUI
        for (int i = 0; i < questInventory.getSize(); i++) {
            questInventory.setItem(i, cyanPane);  // Fill with stained glass
        }

        // Check if the player already has side quests, if not assign new ones
        SideQuestManager.assignSideQuestsIfNecessary(player);

        // Check if the player has a daily quest, if not assign a new one
        Quest dailyQuest = QuestManager.getDailyQuestForPlayer(player);

        // Paper at slot 10 (for daily quest)
        ItemStack dailyQuestItem = createPaperItem(player, dailyQuest);
        questInventory.setItem(10, dailyQuestItem);

        // Clock at slot 5 (indicating quest refresh times)
        ItemStack clockItem = createClockItem();
        questInventory.setItem(5, clockItem);

        // Books and quills at slots 12-16 (side quests)
        int[] sideQuestSlots = {12, 13, 14, 15, 16};
        setSideQuestItems(questInventory, sideQuestSlots, player);

        // Compass at slot 24
        ItemStack compassItem = createCompassItem();
        questInventory.setItem(23, compassItem);

        // Open the GUI for the player
        player.openInventory(questInventory);
    }

    // Method to create a paper item representing the daily quest
    private ItemStack createPaperItem(Player player, Quest dailyQuest) {
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();

        // Check if the player has completed their daily quest today
        if (meta != null && DailyQuestManager.hasCompletedDailyQuestToday(player)) {
            meta.setDisplayName("§eDaily Quest: Completed");
            meta.setLore(List.of(
                    "§7You have completed your daily quest for today.",
                    "§7Please wait for the next refresh to receive a new quest."
            ));
            paper.setItemMeta(meta);
            return paper; // Return early since the player has completed their daily quest
        }

        // If the player hasn't completed their daily quest, show the current quest
        if (meta != null && dailyQuest != null) {
            meta.setDisplayName("§eDaily Quest: " + dailyQuest.getName());
            // Set description, reward, and progress as lore
            meta.setLore(List.of(
                    "§7" + dailyQuest.getDescription(),
                    "§7Reward: " + formatRewards(dailyQuest),
                    "§7Progress: " + QuestUtils.getQuestProgress(player, dailyQuest)
            ));
            paper.setItemMeta(meta);
        } else if (meta != null) {
            meta.setDisplayName("§eDaily Quest");
            meta.setLore(List.of("§7You currently have no daily quest."));
            paper.setItemMeta(meta);
        }

        return paper;
    }




    private ItemStack createClockItem() {
        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta meta = clock.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§eQuest Refresh Timer");

            // Calculate the time until the next refresh
            String timeUntilNextRefresh = QuestUtils.getTimeUntilNextRefresh();

            meta.setLore(List.of(
                    "§7Daily Quests refresh at:",
                    "§7- 2 & 8 AM Central Time",
                    "§7- 2 & 8 PM Central Time",
                    "§7Time until next refresh: §e" + timeUntilNextRefresh
            ));
            clock.setItemMeta(meta);
        }

        return clock;
    }

    // Method to create book and quill items for the side quests
    private void setSideQuestItems(Inventory questInventory, int[] slots, Player player) {
        // Fetch available side quest IDs from SideQuestManager
        int[] sideQuestIds = SideQuestManager.getSideQuests(player); // Get the quest IDs stored in the player's side quests

        for (int i = 0; i < slots.length && i < sideQuestIds.length; i++) {
            int questId = sideQuestIds[i];

            // Fetch the quest object using the quest ID
            Quest quest = QuestManager.getQuestById(questId);
            if (quest == null) {
                continue; // If the quest does not exist, skip this slot
            }

            // Check if the quest is completed (i.e., not in quest_progress and non-repeatable)
            boolean isQuestCompleted = QuestUtils.isQuestCompleted(player, questId);

            // Create a book to represent the side quest
            ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta meta = book.getItemMeta();

            if (meta != null) {
                // Set the display name and lore of the book based on the quest data
                meta.setDisplayName("§b" + quest.getName());
                System.out.println(isQuestCompleted);
                if (isQuestCompleted) {
                    // Show "Progress: Completed" if the quest is completed
                    meta.setLore(List.of(
                            "§7" + quest.getDescription(),
                            "§7Reward: " + formatRewards(quest),
                            "§7Progress: Completed"
                    ));
                } else {
                    // Otherwise, show the player's current progress
                    meta.setLore(List.of(
                            "§7" + quest.getDescription(),
                            "§7Reward: " + formatRewards(quest),
                            "§7Progress: " + QuestUtils.getQuestProgress(player, quest)
                    ));
                }
                book.setItemMeta(meta);
            }

            // Set the book in the corresponding slot in the inventory
            questInventory.setItem(slots[i], book);
        }
    }


    // Method to create a compass item (for navigation or quest info)
    private ItemStack createCompassItem() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§eQuest Info");
            meta.setLore(List.of(
                    "§7Navigate to quests",
                    "§7or check your progress."
            ));
            compass.setItemMeta(meta);
        }

        return compass;
    }

    // Utility method to format rewards into a displayable string (e.g., "200 money, 1 gem")
    private String formatRewards(Quest quest) {
        Map<String, Object> rewards = quest.getRewards();
        List<String> rewardList = new ArrayList<>();

        if (rewards.containsKey("money")) {
            rewardList.add(rewards.get("money") + " money");
        }
        if (rewards.containsKey("gems")) {
            rewardList.add(rewards.get("gems") + " gems");
        }
        if (rewards.containsKey("suffix_tag")) {
            rewardList.add("Suffix: " + rewards.get("suffix_tag"));
        }

        return String.join(", ", rewardList);
    }

    public void updateQuestGUI(Player player, Inventory questInventory) {
        // Fetch the player's daily quest
        Quest dailyQuest = QuestManager.getDailyQuestForPlayer(player);
        ItemStack dailyQuestItem = createPaperItem(player, dailyQuest);
        questInventory.setItem(10, dailyQuestItem);

        // Fetch side quests and update the GUI
        int[] sideQuestSlots = {12, 13, 14, 15, 16};
        setSideQuestItems(questInventory, sideQuestSlots, player);

        // Open the updated inventory for the player
        player.updateInventory();
    }
}
