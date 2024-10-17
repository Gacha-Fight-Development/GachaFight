package me.diu.gachafight.shop.potion.listeners;

import me.diu.gachafight.GachaFight;
import me.diu.gachafight.playerstats.PlayerStats;
import me.diu.gachafight.shop.potion.managers.PotionConfig;
import me.diu.gachafight.utils.ColorChat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PotionUseListener implements Listener {

    private final GachaFight plugin;
    private final Map<String, PotionConfig> potionConfigs = new HashMap<>();
    private File potionsFile;

    public PotionUseListener(GachaFight plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadPotionConfigs();
    }

    private void loadPotionConfigs() {
        potionsFile = new File(plugin.getDataFolder(), "potions.yml");
        plugin.saveResource("potions.yml", true);

        FileConfiguration config = YamlConfiguration.loadConfiguration(potionsFile);

        for (String key : config.getConfigurationSection("potions").getKeys(false)) {
            String name = config.getString("potions." + key + ".name");
            String type = config.getString("potions." + key + ".type");
            int value = config.getInt("potions." + key + ".value");
            int cooldown = config.getInt("potions." + key + ".cooldown");
            int duration = config.getInt("potions." + key + ".duration");

            potionConfigs.put(name, new PotionConfig(type, value, cooldown, duration));
        }
    }

    @EventHandler
    public void onPlayerUsePotion(PlayerInteractEvent event) {
        if (event.getItem() == null || !event.getAction().name().contains("RIGHT_CLICK")) {
            return;
        }

        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (item.getType() != Material.LEATHER_HORSE_ARMOR || !item.hasItemMeta() || player.getCooldown(Material.LEATHER_HORSE_ARMOR) > 0) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) {
            return;
        }

        String displayName = ChatColor.stripColor(meta.getDisplayName());
        PotionConfig potionConfig = potionConfigs.get(displayName);

        if (potionConfig != null) {
            usePotion(player, potionConfig);
            item.setAmount(item.getAmount() - 1);
            player.setCooldown(Material.LEATHER_HORSE_ARMOR, potionConfig.getCooldown());
            event.setCancelled(true);
        }
    }

    private void usePotion(Player player, PotionConfig config) {
        switch (config.getType()) {
            case "hp":
                useHPPotion(player, config.getValue());
                break;
            case "speed":
                useSpeedPotion(player, config.getValue(), config.getDuration());
                break;
            // Add more potion types here as needed
        }
    }

    private void useHPPotion(Player player, int heal) {
        PlayerStats playerStats = PlayerStats.getPlayerStats(player);
        double newHp = Math.min(playerStats.getHp() + heal, playerStats.getMaxhp());
        if (newHp > playerStats.getMaxhp()+playerStats.getGearStats().getTotalMaxHp()) {
            playerStats.setHp(playerStats.getMaxhp()+playerStats.getGearStats().getTotalMaxHp());
        }
        playerStats.setHp(newHp);
        playerStats.updateActionbar(player);
        player.sendMessage(ColorChat.chat("&a+ &c"+ heal+"❤"));
    }

    private void useSpeedPotion(Player player, float speed, int duration) {
        PlayerStats playerStats = PlayerStats.getPlayerStats(player);
        double originalSpeed = playerStats.getSpeed() * 0.1;
        double newSpeed = originalSpeed + (speed * 0.01);

        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(newSpeed);
        player.sendMessage(ColorChat.chat("&a+ &b" + (speed * 0.1) + " Speed"));

        new BukkitRunnable() {
            @Override
            public void run() {
                player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(originalSpeed);
                player.sendMessage(ColorChat.chat("&cSpeed boost has worn off!"));
            }
        }.runTaskLater(plugin, duration * 20L);
    }

    public void reloadConfig() {
        loadPotionConfigs();
    }
}
