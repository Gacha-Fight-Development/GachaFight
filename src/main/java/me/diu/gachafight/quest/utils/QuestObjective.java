// QuestObjective.java
package me.diu.gachafight.quest.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class QuestObjective {
    private final String description;
    private final int requiredAmount; // Add this field

    public QuestObjective(String description, int requiredAmount) {
        this.description = description;
        this.requiredAmount = requiredAmount; // Initialize the required amount
    }

    public abstract boolean isCompleted();
}
