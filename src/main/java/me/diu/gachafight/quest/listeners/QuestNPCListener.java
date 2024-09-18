package me.diu.gachafight.quest.listeners;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.quest.Quest;
import me.diu.gachafight.quest.QuestManager;
import me.diu.gachafight.quest.gui.QuestGUI;
import me.diu.gachafight.quest.objectives.DailyCooldownObjective;
import me.diu.gachafight.quest.utils.QuestFactory;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@Getter
@Setter
public class QuestNPCListener implements Listener {
    private final QuestManager questManager;

    public QuestNPCListener(GachaFight plugin, QuestManager questManager) {
        this.questManager = questManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        new QuestKillListener(questManager);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getName().equalsIgnoreCase("Quest")) {
            Player player = event.getPlayer();
            QuestGUI.openQuestSelection(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase("Select a Quest")) { // Match the GUI title
            event.setCancelled(true);  // Prevent taking items from the GUI

            ItemStack clickedItem = event.getCurrentItem();
            Player player = (Player) event.getWhoClicked();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            ItemMeta meta = clickedItem.getItemMeta();
            String questName = meta.getDisplayName();

            if (questName.equalsIgnoreCase(ColorChat.chat("&bDaily Quest"))) {
                Random random = new Random();
                int dailyQuestId = random.nextInt(4); //id 1-3
                if (questManager.canStartQuest(player, dailyQuestId)) {
                    assignDailyQuest(player);
                } else {
                    player.sendMessage("§cYou need to complete prerequisite quests before starting this one.");
                }
            } else {
                try {
                    int questId = questManager.getQuestIdFromName(questName);
                    if (questManager.canStartQuest(player, questId)) {
                        // Assign the quest here
                        Quest quest = questManager.createQuestFromConfig(questId);
                        questManager.assignQuest(player, quest, event.getSlot());
                        player.sendMessage("§aYou have accepted the quest: " + quest.getName());
                    } else {
                        player.sendMessage("§cYou need to complete prerequisite quests before starting this one.");
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cThis quest cannot be found.");
                }
            }
        }
    }


    private void assignDailyQuest(Player player) {
        // Check for existing cooldown quest
        Integer cooldownProgress = questManager.loadQuestProgress(player, -1);
        if (cooldownProgress != null) {
            // Cooldown quest exists, check if 24 hours have passed
            Timestamp cooldownStartTime = questManager.loadQuestStartTime(player, -1);
            if (!isCooldownExpired(cooldownStartTime, 24)) {
                player.sendMessage("§cYou must wait 24 hours before starting a new daily quest.");
                return;
            }
        }

        // Assign the new daily quest
        Quest dailyQuest = questManager.getQuestFactory().createRandomDailyQuest(player);
        questManager.assignQuest(player, dailyQuest, 0); // Assign to slot 0 in GUI
        player.sendMessage("§aYou have accepted the daily quest: " + dailyQuest.getName());

        // Assign the cooldown quest with ID -1 to store the start time, passing null for slot
        DailyCooldownObjective dailyCooldownObjective = new DailyCooldownObjective("Cooldown 24 hour", 24);
        Quest cooldownQuest = new Quest("Daily Quest Cooldown", "Wait 24 hours to start a new daily quest.", dailyCooldownObjective, -1, -1);
        questManager.assignQuest(player, cooldownQuest, null);

        // Update the inventory slot to show the quest as a book
        QuestGUI.updateQuestItem(player, dailyQuest, 0);
    }



    private boolean isCooldownExpired(Timestamp startTime, int hours) {
        if (startTime == null) {
            return true; // If no start time is found, treat it as expired.
        }

        // Calculate the end time based on the start time plus the cooldown duration (in hours)
        Instant endTime = startTime.toInstant().plus(Duration.ofHours(hours));
        Instant currentTime = Instant.now();

        // Check if the current time is after the end time (meaning the cooldown has expired)
        return currentTime.isAfter(endTime);
    }

}
