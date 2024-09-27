// OnlineTimeObjective.java
package me.diu.gachafight.quest.objectives;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.quest.utils.QuestObjective;

@Getter
@Setter
public class OnlineTimeObjective extends QuestObjective {
    private final int timeInSeconds;

    public OnlineTimeObjective(String description, int timeInSeconds) {
        super(description);
        this.timeInSeconds = timeInSeconds;
    }
}
