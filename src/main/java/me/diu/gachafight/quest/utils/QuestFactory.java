package me.diu.gachafight.quest.utils;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.quest.Quest;
import me.diu.gachafight.quest.QuestManager;
import me.diu.gachafight.quest.objectives.DailyCooldownObjective;
import me.diu.gachafight.quest.objectives.KeyOpenObjective;
import me.diu.gachafight.quest.objectives.KillObjective;
import me.diu.gachafight.quest.objectives.OnlineTimeObjective;
import org.bukkit.entity.Player;

import java.util.Random;

@Getter
@Setter
public class QuestFactory {
    private final QuestManager questManager;

    // Constructor to initialize QuestManager
    public QuestFactory(QuestManager questManager) {
        this.questManager = questManager;
    }

    public Quest createQuest(int questId, Player player) {
        Quest quest = questManager.createQuestFromConfig(questId);
        if (quest != null) {
            return quest;
        }
        throw new IllegalArgumentException("Unknown quest ID: " + questId);
    }

    public Quest createRandomDailyQuest(Player player) {
        Random random = new Random();
        int questType = random.nextInt(3); // 0 for kill quest, 1 for online time quest
        if (questType == 0) {
            KillObjective mobKillObjective = new KillObjective("Kill 10 Goblin Warriors", "Goblin Warrior", 10, questManager.loadQuestProgress(player, 1), questManager);
            return new Quest("Kill Goblin Warriors", "Kill 10 Goblin Warriors.", mobKillObjective, 1, 0);
        } else if (questType == 1) {
            OnlineTimeObjective onlineObjective = new OnlineTimeObjective("Stay online for 1 hour", 3600);
            return new Quest("Online for 1 Hour", "Stay online for 1 hour.", onlineObjective, 2, 0);
        } else {
            KeyOpenObjective keyOpenObjective = new KeyOpenObjective("Open 15 Common Key", 15, questManager.loadQuestProgress(player, 3), questManager);
            return new Quest("Open 15 Common Key", "Common Key", keyOpenObjective, 3, 0);
        }
    }
}

