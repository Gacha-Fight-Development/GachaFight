package me.diu.gachafight.quest.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.quest.Quest;
import me.diu.gachafight.quest.gui.QuestGUI;
import me.diu.gachafight.quest.managers.DailyQuestManager;
import me.diu.gachafight.quest.managers.QuestManager;
import me.diu.gachafight.quest.managers.SideQuestManager;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class QuestClickListener implements Listener {
    private final GachaFight plugin;

    public QuestClickListener(GachaFight plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory questInventory = event.getClickedInventory();

        // Handle quest clicks (daily or side quests)
        if (event.getView().getTitle().equals("Quest Selection") && event.getClickedInventory() != event.getView().getBottomInventory()) {
            event.setCancelled(true); // Prevent moving items in the GUI

            // Handle daily quest click (slot 10)
            if (event.getSlot() == 10) {
                if (!DailyQuestManager.hasCompletedDailyQuestToday(player)) {
                    Quest dailyQuest = DailyQuestManager.assignRandomDailyQuest(player);
                    player.sendMessage(ChatColor.GREEN + "You have been assigned a new daily quest!");
                } else {
                    player.sendMessage(ChatColor.RED + "You have already completed your daily quest. Wait for the next refresh.");
                }
            }

            // Update the quest GUI (if necessary)
            QuestGUI questGUI = new QuestGUI(plugin.getQuestManager());
            questGUI.updateQuestGUI(player, questInventory);
        }
    }


    // Handle what happens when the player clicks a daily quest
    private void handleDailyQuestClick(Player player) {
        // Logic to assign or show the daily quest
        // You can fetch the daily quest from QuestManager and give the quest to the player
        Quest dailyQuest = QuestManager.getDailyQuestForPlayer(player);
        if (dailyQuest != null) {
            player.sendMessage(ColorChat.chat( "&eDaily Quest: " + dailyQuest.getName()));
            // Additional logic to start the quest or show progress
        } else {
            player.sendMessage(ColorChat.chat( "&cNo daily quests available!"));
        }
    }

    // Handle what happens when the player clicks a side quest
    private void handleSideQuestClick(Player player, int slot) {
        // Fetch the side quest corresponding to the clicked slot
        int[] sideQuestIds = SideQuestManager.getSideQuests(player);
        int questIndex = slot - 12; // Slot 12 corresponds to index 0 in the side quest list

        if (questIndex >= 0 && questIndex < sideQuestIds.length) {
            int questId = sideQuestIds[questIndex];
            Quest sideQuest = QuestManager.getQuestById(questId);

            if (sideQuest != null) {
                player.sendMessage(ColorChat.chat("&eSide Quest: " + sideQuest.getName()));
                // Additional logic to start the quest or show progress
            } else {
                player.sendMessage(ColorChat.chat("&cNo side quests available!"));
            }
        }
    }
}
