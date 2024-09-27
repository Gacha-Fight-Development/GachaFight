// QuestObjective.java
package me.diu.gachafight.quest.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class QuestObjective {
    protected String description;
    protected int amount;

    public QuestObjective(String description) {
        this.description = description;
    }
}
