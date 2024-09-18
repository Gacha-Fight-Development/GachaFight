package me.diu.gachafight.quest.objectives;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.quest.utils.QuestObjective;
import org.bukkit.scoreboard.Objective;
@Getter
@Setter
public class DailyCooldownObjective extends QuestObjective {
    private int currentTime;

    public DailyCooldownObjective(String description, int requiredTime) {
        super(description, requiredTime); // Pass to superclass
    }

    public void incrementTime(int time) {
        currentTime += time;
    }

    @Override
    public boolean isCompleted() {
        return currentTime >= getRequiredAmount();
    }
}
