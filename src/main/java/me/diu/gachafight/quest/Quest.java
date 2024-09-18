package me.diu.gachafight.quest;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.quest.objectives.KillObjective;
import me.diu.gachafight.quest.objectives.OnlineTimeObjective;
import me.diu.gachafight.quest.utils.QuestObjective;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Quest {
    private final String name;
    private final String description;
    private final QuestObjective objective;
    private final int id;
    private int slot;

    private List<Integer> dependencies = new ArrayList<>();

    public Quest(String name, String description, QuestObjective objective, int id, int slot) {
        this.name = name;
        this.description = description;
        this.objective = objective;
        this.id = id;
        this.slot = slot;
    }




    public void setProgress(int progress) {
        if (objective instanceof KillObjective) {
            ((KillObjective) objective).setCurrentMobKills(progress);
        } else if (objective instanceof OnlineTimeObjective) {
            ((OnlineTimeObjective) objective).setCurrentTime(progress);
        }
    }
}
