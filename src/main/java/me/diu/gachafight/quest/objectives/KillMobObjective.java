package me.diu.gachafight.quest.objectives;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.quest.utils.QuestObjective;


@Getter
@Setter
public class KillMobObjective extends QuestObjective {
    private final String target;
    private final int amount;

    public KillMobObjective(String description, String target, int amount) {
        super(description);
        this.target = target;
        this.amount = amount;
    }
}

