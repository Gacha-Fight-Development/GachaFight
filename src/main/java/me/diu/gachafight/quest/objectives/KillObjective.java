package me.diu.gachafight.quest.objectives;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.quest.QuestManager;
import me.diu.gachafight.quest.utils.QuestObjective;
import org.bukkit.entity.Player;

import java.sql.Timestamp;


@Getter
@Setter
public class KillObjective extends QuestObjective {
    private Integer currentMobKills;
    private QuestManager questManager;

    public KillObjective(String description, String mobName, int targetMobKills, Integer currentMobKills, QuestManager questManager) {
        super(description, targetMobKills);
        this.questManager = questManager;
        if (currentMobKills != null) {
            this.currentMobKills = currentMobKills;
        } else {
            this.currentMobKills = 0;
        }
    }

    public void incrementMobKills(Player player, int questId, Timestamp timeStamp, int slot) {
        currentMobKills++;
        questManager.saveQuestProgress(player, questId, currentMobKills, timeStamp, slot);
    }

    @Override
    public boolean isCompleted() {
        return currentMobKills >= getRequiredAmount();
    }

}

