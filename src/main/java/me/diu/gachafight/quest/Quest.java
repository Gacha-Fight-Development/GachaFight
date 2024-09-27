package me.diu.gachafight.quest;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.quest.utils.QuestObjective;

import java.util.Map;

@Getter
@Setter
public class Quest {
    private final int id;
    private final String name;
    private final String description;
    private final QuestObjective objective;
    private final Map<String, Object> rewards;
    private final boolean repeatable;

    public Quest(int id, String name, String description, QuestObjective objective, Map<String, Object> rewards, boolean repeatable) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.objective = objective;
        this.rewards = rewards;
        this.repeatable = repeatable;
    }

    // Getters for each field
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public QuestObjective getObjective() {
        return objective;
    }

    public Map<String, Object> getRewards() {
        return rewards;
    }

    public boolean isRepeatable() {
        return repeatable;
    }
}

