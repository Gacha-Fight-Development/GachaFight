package me.diu.gachafight.quest.objectives;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.quest.utils.QuestObjective;

@Getter
@Setter
public class KeyOpenObjective extends QuestObjective {
    private final int amount;

    public KeyOpenObjective(String description, int amount) {
        super(description);
        this.amount = amount;
    }
}