package me.diu.gachafight.quest.listeners;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.quest.Quest;
import me.diu.gachafight.quest.QuestManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


@Getter
@Setter
public class QuestKillListener {
    private static QuestManager questManager;

    public QuestKillListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    public static void questKill(Player player, Entity entity) {
        if (entity.getName().contains("Goblin Warrior")) {
            // Increment quest progress for the "Kill Goblin Warriors" quest
            Quest goblinQuest = questManager.getQuestById(1, player);

            // Check if the player has this quest
            Integer currentProgress = questManager.loadQuestProgress(player, goblinQuest.getId());
            if (currentProgress == null) { // Player doesn't have the quest
                return;
            }

            if (goblinQuest != null) {
                questManager.incrementQuestProgress(player, goblinQuest.getId());

                // Get the required amount from the Quest's objective
                int requiredAmount = goblinQuest.getObjective().getRequiredAmount();
                player.sendMessage("§aGoblin Warrior killed! Progress: " +
                        (currentProgress + 1) + "/" + requiredAmount);
            }
        } else {
        }
    }


}
