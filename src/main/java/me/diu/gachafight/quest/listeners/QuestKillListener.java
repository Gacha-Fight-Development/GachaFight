package me.diu.gachafight.quest.listeners;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.quest.Quest;
import me.diu.gachafight.quest.managers.QuestManager;
import me.diu.gachafight.quest.utils.QuestUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


@Getter
@Setter
public class QuestKillListener {
    private static QuestManager questManager;

    public QuestKillListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    public static void questKillMob(Player player, Entity entity) {
        // Check if the killed entity is a Goblin Warrior (or any quest-related mob)
        if (entity.getName().contains("Goblin Warrior")) {
            // Fetch the quest by ID (assume quest ID 1 is the Goblin Warrior quest)
            Quest goblinQuest = QuestUtils.getQuestById(1, player);

            // Increment the quest progress if the player has this quest
            QuestUtils.incrementQuestProgress(player, goblinQuest, 1); // Increment by 1 for each kill
        }
    }

}
