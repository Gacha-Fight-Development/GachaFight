package me.diu.gachafight.quest.listeners;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.quest.Quest;
import me.diu.gachafight.quest.managers.QuestManager;
import me.diu.gachafight.quest.objectives.KillMobObjective;
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
        // Loop through all active quests for the player
        for (Quest quest : questManager.getActiveQuestsForPlayer(player)) {
            if (quest.getObjective() instanceof KillMobObjective) {
                KillMobObjective objective = (KillMobObjective) quest.getObjective();
                if (entity.getName().contains(objective.getTarget())) {
                    // Increment quest progress, passing "killMob" as the objective type
                    QuestUtils.incrementQuestProgress(player, quest, "killMob", 1);
                }
            }
        }
    }


}
