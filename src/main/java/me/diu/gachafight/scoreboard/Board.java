package me.diu.gachafight.scoreboard;

import me.clip.placeholderapi.PlaceholderAPI;
import me.diu.gachafight.GachaFight;
import me.diu.gachafight.party.PartyManager;
import me.diu.gachafight.playerstats.PlayerDataManager;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.di.ServiceLocator;
import me.diu.gachafight.siege.Arena;
import me.diu.gachafight.siege.SiegeGameMode;
import me.diu.gachafight.utils.ColorChat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.Map;
import java.util.Set;

public class Board {
    private static final String SCOREBOARD_TITLE = "&x&F&F&0&0&D&9G&x&F&F&1&C&C&1a&x&F&F&3&7&A&9c&x&F&F&5&3&9&1h&x&F&F&6&E&7&9a&x&F&F&8&A&6&0F&x&F&F&A&5&4&8i&x&F&F&C&1&3&0g&x&F&F&D&C&1&8h&x&F&F&F&8&0&0t";
    private static final String SERVER_ADDRESS = "&bGachaFight.minehut.gg";

    private final PlayerDataManager playerDataManager;

    public Board(ServiceLocator serviceLocator) {
        this.playerDataManager = serviceLocator.getService(PlayerDataManager.class);
    }

    public void setScoreBoard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("Scoreboard", "dummy", ColorChat.chat(SCOREBOARD_TITLE));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        PlayerStats stats = getOrLoadPlayerStats(player);
        String formattedMoney = getFormattedMoney(player);

        OfflinePlayer partyLeader = PartyManager.getPartyLeader(player);
        if (partyLeader != null) {
            setPartyScoreboard(obj, player, stats, formattedMoney, partyLeader);
        } else {
            setSoloScoreboard(obj, formattedMoney, stats);
        }
        if (SiegeGameMode.playerWave.containsKey(player.getUniqueId())) {
            System.out.println("Player Wave: " + SiegeGameMode.playerWave.get(player.getUniqueId()));
            Map<Integer, Integer> arenaWaves = SiegeGameMode.playerWave.get(player.getUniqueId());
            for (Integer arenaId : arenaWaves.keySet()) {
                System.out.println("Arena ID: " + arenaId);
                if (SiegeGameMode.activeMonsters.containsKey(player.getUniqueId())) {
                    System.out.println("true");
                    obj.getScore("").setScore(8);
                    obj.getScore(ColorChat.chat("&b&l| &bArena ID: &7" + arenaId)).setScore(7);
                    obj.getScore(ColorChat.chat("&b&l| &bWave: &7" + arenaWaves.get(arenaId))).setScore(7);
                    obj.getScore(ColorChat.chat("&b&l| &bRemaining Mobs: &7" + SiegeGameMode.activeMonsters.get(player.getUniqueId()).size())).setScore(7);
                    obj.getScore("").setScore(6);
                }
            }
        } else {
            if (partyLeader != null) {
                for (OfflinePlayer member : PartyManager.getPartyMembers(partyLeader)) {
                    if (SiegeGameMode.playerWave.containsKey(member.getUniqueId())) {
                        Map<Integer, Integer> memberArenaWaves = SiegeGameMode.playerWave.get(member.getUniqueId());
                        for (Integer arenaId : memberArenaWaves.keySet()) {
                            if (SiegeGameMode.activeMonsters.containsKey(player.getUniqueId())) {
                                obj.getScore("").setScore(8);
                                obj.getScore(ColorChat.chat("&b&l| &bArena ID: &7" + arenaId)).setScore(7);
                                obj.getScore(ColorChat.chat("&b&l| &bWave: &7" + memberArenaWaves.get(arenaId))).setScore(7);
                                obj.getScore(ColorChat.chat("&b&l| &bRemaining Mobs: &7" + SiegeGameMode.activeMonsters.get(player.getUniqueId()).size())).setScore(7);
                                obj.getScore("").setScore(6);
                            }
                        }
                    }
                }
            }
        }

        player.setScoreboard(board);
    }

    private PlayerStats getOrLoadPlayerStats(Player player) {
        PlayerStats stats = playerDataManager.getPlayerStats(player.getUniqueId());
        if (stats == null) {
            playerDataManager.loadPlayerData(player);
            stats = playerDataManager.getPlayerStats(player.getUniqueId());
        }
        return stats;
    }

    private String getFormattedMoney(Player player) {
        String rawMoneyText = PlaceholderAPI.setPlaceholders(player, "%vault_eco_balance%");
        double balance = 0.0;
        try {
            balance = Double.parseDouble(rawMoneyText);
        } catch (NumberFormatException ignored) {}
        return String.format("&e%.2f", Math.round(balance * 100.0) / 100.0);
    }

    private void setPartyScoreboard(Objective obj, Player player, PlayerStats stats, String formattedMoney, OfflinePlayer partyLeader) {
        obj.getScore(ColorChat.chat("&3&l| &7Statistics")).setScore(10);
        obj.getScore(ColorChat.chat("&3&l| &6⛃ &e" + formattedMoney) + ColorChat.chat(" &6Gold")).setScore(9);
        obj.getScore(ColorChat.chat("&3&l| &b❖ &e" + stats.getGem() +" &bGems")).setScore(9);
        obj.getScore(ColorChat.chat("")).setScore(7);
        obj.getScore(ColorChat.chat("&6&l| &7Party")).setScore(6);

        int scoreValue = 5;
        for (OfflinePlayer member : PartyManager.getPartyMembers(partyLeader)) {
            PlayerStats memberStats = PlayerStats.getPlayerStats(member.getUniqueId());
            if (memberStats != null) {
                String hpDisplay = String.format("&6&l|  &8- &e%s &c%.0f/%.0f HP", member.getName(),
                        memberStats.getHp()+memberStats.getGearStats().getTotalMaxHp(),
                        memberStats.getMaxhp()+memberStats.getGearStats().getTotalMaxHp());
                obj.getScore(ColorChat.chat(hpDisplay)).setScore(scoreValue--);
            } else {
                continue;
            }
        }
    }

    private void setSoloScoreboard(Objective obj, String formattedMoney, PlayerStats stats) {
        obj.getScore(ColorChat.chat("&3&l| &7Statistics")).setScore(9);
        obj.getScore(ColorChat.chat("&3&l| &6⛃ &e" + formattedMoney) + ColorChat.chat(" &6Gold")).setScore(9);
        obj.getScore(ColorChat.chat("&3&l| &b❖ &e" + stats.getGem() +" &bGems")).setScore(9);
        obj.getScore(ColorChat.chat("")).setScore(2);
        obj.getScore(ColorChat.chat("&3Found Bugs? &b/Discord")).setScore(1);
        obj.getScore(ColorChat.chat(SERVER_ADDRESS)).setScore(1);
    }
}
