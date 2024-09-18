// OnlineTimeObjective.java
package me.diu.gachafight.quest.objectives;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.quest.utils.QuestObjective;

@Getter
@Setter
public class OnlineTimeObjective extends QuestObjective {
    private int currentTime;

    public OnlineTimeObjective(String description, int requiredTime) {
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
