package me.diu.gachafight.quest.objectives;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.quest.utils.QuestObjective;

@Getter
@Setter
public class KeyOpenObjective extends QuestObjective {
    private final int amount;
    private final String target;

    public KeyOpenObjective(String description, String target, int amount) {
        super(description);
        this.target = target;
        this.amount = amount;
    }
}