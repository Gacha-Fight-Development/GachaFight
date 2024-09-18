package me.diu.gachafight.quest.objectives;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.quest.QuestManager;
import me.diu.gachafight.quest.utils.QuestObjective;
import org.bukkit.entity.Player;

import java.sql.Timestamp;

@Getter
@Setter
public class KeyOpenObjective  extends QuestObjective {
    private Integer currentKeyOpen;
    private QuestManager questManager;

    public KeyOpenObjective(String description, int targetKeyOpen, Integer currentKeyOpen, QuestManager questManager) {
        super(description, targetKeyOpen);
        this.questManager = questManager;
        if (currentKeyOpen != null) {
            this.currentKeyOpen = currentKeyOpen;
        } else {
            this.currentKeyOpen = 0;
        }
    }

    public void incrementKeyOpen(Player player, int questId, Timestamp timeStamp, int slot) {
        currentKeyOpen++;
        questManager.saveQuestProgress(player, questId, currentKeyOpen, timeStamp, slot);
    }

    @Override
    public boolean isCompleted() {
        return currentKeyOpen >= getRequiredAmount();
    }
}
