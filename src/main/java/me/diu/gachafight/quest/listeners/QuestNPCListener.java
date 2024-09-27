package me.diu.gachafight.quest.listeners;

import lombok.Getter;
import lombok.Setter;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.quest.managers.QuestManager;
import me.diu.gachafight.quest.gui.QuestGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

@Getter
@Setter
public class QuestNPCListener implements Listener {
    private final QuestManager questManager;

    public QuestNPCListener(GachaFight plugin, QuestManager questManager) {
        this.questManager = questManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        new QuestKillListener(questManager);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (entity.getName().equals("Quest")) {
            // Open the quest GUI (daily quests or side quests) for the player
            QuestGUI questGUI = new QuestGUI(questManager); // Assuming this is your QuestGUI class
            questGUI.openQuestGUI(player); // Open the GUI with daily/side quests
        }
    }
}
